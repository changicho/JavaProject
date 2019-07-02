package client;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import constant.Constant;
import server.User;

// 로비 및 이벤트 관리.
public class ClientMain extends JFrame implements ActionListener, Runnable {
	JList<String> roomInfo, roomInwon, waitInfo;
	JScrollPane sp_roomInfo, sp_roomInwon, sp_waitInfo;
	JButton bt_create, bt_enter, bt_exit, bt_addtodo, bt_showtodo;
	JPanel p;

	String ID;

	// 채팅 방
	ChatRoomUI cc;
	// 로그인 창
	Login login;
	// 소켓 입출력객체
	BufferedReader in;
	OutputStream out;
	
	String selectedRoom;

	public ClientMain() {
		setTitle("로비");
		cc = new ChatRoomUI();
		login = new Login();

		roomInfo = new JList<String>();
		roomInfo.setBorder(new TitledBorder("채팅방 정보"));
		roomInfo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String str = roomInfo.getSelectedValue();
				// "자바방, 인원수 : x"
				if (str == null)
					return;
				System.out.println("STR=" + str);
				selectedRoom = str.substring(0, str.indexOf("-"));
				// "자바방" <---- substring(0,3)
				// 대화방 내의 인원정보
				sendMsg(Constant.ROBBYUSERINFO + "|" + selectedRoom);
			}
		});
		roomInwon = new JList<String>();
		roomInwon.setBorder(new TitledBorder("채팅방 인원 정보"));
		waitInfo = new JList<String>();
		waitInfo.setBorder(new TitledBorder("대기실 인원 정보"));
		sp_roomInfo = new JScrollPane(roomInfo);
		sp_roomInwon = new JScrollPane(roomInwon);
		sp_waitInfo = new JScrollPane(waitInfo);
		
		bt_create = new JButton("방만들기");
		bt_enter = new JButton("방들어가기");
		bt_exit = new JButton("나가기");
		
		sp_roomInfo.setBounds(10, 10, 300, 300);
		sp_roomInwon.setBounds(320, 10, 150, 300);
		sp_waitInfo.setBounds(10, 320, 300, 130);
		bt_create.setBounds(320, 330, 150, 30);
		bt_enter.setBounds(320, 370, 150, 30);
		bt_exit.setBounds(320, 410, 150, 30);

		p = new JPanel();
		p.setLayout(null);
		p.setBackground(Color.GRAY);
		p.add(sp_roomInfo);
		p.add(sp_roomInwon);
		p.add(sp_waitInfo);
		p.add(bt_create);
		p.add(bt_enter);
		p.add(bt_exit);
		add(p);
		
		setBounds(300, 200, 500, 500);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		connect(); // 서버연결시도 (in,out객체생성)

		setVisible(false);
		login.setVisible(true);

		// 서버메시지 대기
		new Thread(this).start();
		// 이벤트 리스너 생성
		eventUp();
	}

	private void eventUp() { // 이벤트소스-이벤트처리부 연결
		// 대기실(MainChat)
		bt_create.addActionListener(this);
		bt_enter.addActionListener(this);
		bt_exit.addActionListener(this);

		// 대화방(ChatClient)
		cc.sendTF.addActionListener(this);
		cc.bt_exit.addActionListener(this);
		cc.bt_addtodo.addActionListener(this);
		cc.bt_showtodo.addActionListener(this);
		cc.bt_deltodo.addActionListener(this);

		// 로그인 창
		login.bt_exit.addActionListener(this);
		login.bt_login.addActionListener(this);
		login.bt_signup.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object ob = e.getSource();
		if (ob == bt_create) { // 방만들기 요청
			String title = JOptionPane.showInputDialog(this, "채팅방 제목:");
			// 방제목을 서버에게 전달
			sendMsg(Constant.CREATEROOM + "|" + title);

			// 채팅 창의 제목 설정
			cc.setTitle("채팅방-[" + title + "]");
			// 대화방내 인원정보 요청
			sendMsg(Constant.ROOMUSERINFO + "|");

			// 현재 화면을 보이지 않게 하고, 채팅 창 화면 보이게 함
			setVisible(false);
			cc.setVisible(true);

		} else if (ob == bt_enter) { // 방들어가기 요청
			if (selectedRoom == null) { // 방이 선택되지 않았으면
				JOptionPane.showMessageDialog(this, "방을 선택하지 않았습니다");
				return;
			}
			// 서버에 선택한 방에 들어감을 요청
			sendMsg(Constant.ENTERROOM + "|" + selectedRoom);
			// 대화방내 인원정보 요청
			sendMsg(Constant.ROOMUSERINFO + "|");

			// 현재 화면을 보이지 않게 하고, 채팅 창 화면 보이게 함
			setVisible(false);
			cc.setVisible(true);

		} else if (ob == cc.bt_exit) { // 대화방 나가기 요청

			sendMsg(Constant.EXITROOM + "|");
			cc.setVisible(false);
			setVisible(true);

		} else if (ob == cc.sendTF) { // (TextField입력)메시지 보내기 요청
			String msg = cc.sendTF.getText();

			if (msg.length() > 0) {
				sendMsg(Constant.PRINTMSG + "|" + msg);
				cc.sendTF.setText("");
			}

		} else if (ob == bt_exit) { // 나가기(프로그램종료) 요청
			sendMsg(Constant.EXITROBBY + "|" + ID);
			System.exit(0); // 현재 응용프로그램 종료하기

		} else if (ob == cc.bt_addtodo) { // 할일 추가 요창
			String todoName = JOptionPane.showInputDialog(this, "할일 명:");
			sendMsg(Constant.ADDTODO + "|" + todoName);

		} else if (ob == cc.bt_showtodo) { // 전체 할일 채팅창에 출력
			sendMsg(Constant.PRINTTODO + "|");

		} else if (ob == cc.bt_deltodo) { // 할일 삭제
			String todoName = JOptionPane.showInputDialog(this, "할일 명:");
			sendMsg(Constant.DELTODO + "|" + todoName);

		} else if (ob == login.bt_login) {
			ID = login.id.getText();
			String PW = login.pw.getText();
			
			if (ID.length() == 0) {	// ID가 공백이면
				login.showFail("ID를 입력하시오");
				
			} else if (PW.length() == 0) { // PW 가 공백이면
				login.showFail("PW를 입력하시오");
				
			} else {
				sendMsg(Constant.SENDIDPW + "|" + ID + "%" + PW);
				
			}

		} else if (ob == login.bt_signup) {
			String SignUpID = login.id.getText();
			String SignUpPW = login.pw.getText();
			
			if (SignUpID.length() == 0) { // ID가 공백이면
				login.showFail("ID를 입력하시오");
				
			} else if (SignUpPW.length() == 0) { // PW 가 공백이면
				login.showFail("PW를 입력하시오");
				
			} else {
				sendMsg(Constant.SIGNUP + "|" + SignUpID + "%" + SignUpPW);
				login.showSignupOK();
				
			}

		} else if (ob == login.bt_exit) {
			System.exit(0); // 현재 응용프로그램 종료하기
		}
	}

	public void connect() { // (소켓)서버연결 요청
		try {
			// 연결시도
			Socket s = new Socket("localhost", 5000); // Socket(String host<서버ip>, int port<서비스번호>)

			// in: 서버메시지 읽기 객체, msg : 서버>클라이언트
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			// out: 메시지 보내기 쓰기 객체, msg : 클라이언트>서버
			out = s.getOutputStream();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMsg(String msg) { // 서버에게 메시지 보내기
		try {
			out.write((msg + "\n").getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		// 서버가 보낸 메시지 읽기
		// GUI프로그램실행에 영향 미치지않는 run 메소드로 구성.
		try {
			while (true) {
				String msg = in.readLine();
				// msg: 서버가 보낸 메시지
				// msg==> "300|안녕하세요" "160|자바방--1,오라클방--1,JDBC방--1"
				String msgs[] = msg.split("\\|");
				String protocol = msgs[0];

				switch (protocol) {
				case Constant.PRINTMSG: // 채팅창에 메시지 출력
					cc.ta.append(msgs[1] + "\n");
					cc.ta.setCaretPosition(cc.ta.getText().length());
					break;

				case Constant.CLEARMSG: // 채팅창 지우기
					cc.ta.setText(null);
					break;

				case Constant.ROOMLIST: // 방 정보를 List에 뿌리기
					// 방정보를 List에 뿌리기
					if (msgs.length > 1) {
						// 개설된 방이 한개 이상이었을때 실행
						// 개설된 방없음 ----> msg="160|" 였을때 에러
						String roomNames[] = msgs[1].split(",");
						// "자바방--1,오라클방--1,JDBC방--1"
						roomInfo.setListData(roomNames);
					} else {
						roomInfo.removeAll();
					}
					break;

				case Constant.ROBBYUSERINFO:// (대기실에서) 대화방 인원정보
					String roomInwons[] = msgs[1].split(",");
					roomInwon.setListData(roomInwons);
					break;

				case Constant.ROOMUSERINFO:// (대화방에서) 대화방 인원정보
					String myRoomInwons[] = msgs[1].split(",");
					cc.li_inwon.setListData(myRoomInwons);
					break;

				case Constant.ROBBYWAITINFO: // 대기실 인원정보
					String waitNames[] = msgs[1].split(",");
					waitInfo.setListData(waitNames);
					break;

				case Constant.ENTERROOM:// 대화방 입장
					cc.ta.append("=========[" + msgs[1] + "]님 입장=========\n");
					cc.ta.setCaretPosition(cc.ta.getText().length());
					break;

				case Constant.EXITROOM:// 대화방 퇴장
					cc.ta.append("=========[" + msgs[1] + "]님 퇴장=========\n");
					cc.ta.setCaretPosition(cc.ta.getText().length());
					break;

				case Constant.GETROOMNAME:// 개설된 방의 타이틀 제목 얻기
					cc.setTitle("채팅방-[" + msgs[1] + "]");
					break;

				case Constant.LOGINOK:
					// (대기실)접속 알림
					sendMsg(Constant.ENTERROBBY + "|");

					String nickName = JOptionPane.showInputDialog(this, "닉네임	:");

					// 대화명 전달
					sendMsg(Constant.WRITENICKNAME + "|" + nickName + "(" + login.id.getText() + ")");
					setVisible(true);
					login.setVisible(false);
					break;
					
				case Constant.LOGINFAIL:
					login.showFail(msgs[1]);
					break;
				}
				// 클라이언트 switch end
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new ClientMain();
	}
}