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

// �κ� �� �̺�Ʈ ����.
public class ClientMain extends JFrame implements ActionListener, Runnable {
	JList<String> roomInfo, roomInwon, waitInfo;
	JScrollPane sp_roomInfo, sp_roomInwon, sp_waitInfo;
	JButton bt_create, bt_enter, bt_exit, bt_addtodo, bt_showtodo;
	JPanel p;

	String ID;

	// ä�� ��
	ChatRoomUI cc;
	// �α��� â
	Login login;
	// ���� ����°�ü
	BufferedReader in;
	OutputStream out;
	
	String selectedRoom;

	public ClientMain() {
		setTitle("�κ�");
		cc = new ChatRoomUI();
		login = new Login();

		roomInfo = new JList<String>();
		roomInfo.setBorder(new TitledBorder("ä�ù� ����"));
		roomInfo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String str = roomInfo.getSelectedValue();
				// "�ڹٹ�, �ο��� : x"
				if (str == null)
					return;
				System.out.println("STR=" + str);
				selectedRoom = str.substring(0, str.indexOf("-"));
				// "�ڹٹ�" <---- substring(0,3)
				// ��ȭ�� ���� �ο�����
				sendMsg(Constant.ROBBYUSERINFO + "|" + selectedRoom);
			}
		});
		roomInwon = new JList<String>();
		roomInwon.setBorder(new TitledBorder("ä�ù� �ο� ����"));
		waitInfo = new JList<String>();
		waitInfo.setBorder(new TitledBorder("���� �ο� ����"));
		sp_roomInfo = new JScrollPane(roomInfo);
		sp_roomInwon = new JScrollPane(roomInwon);
		sp_waitInfo = new JScrollPane(waitInfo);
		
		bt_create = new JButton("�游���");
		bt_enter = new JButton("�����");
		bt_exit = new JButton("������");
		
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
		connect(); // ��������õ� (in,out��ü����)

		setVisible(false);
		login.setVisible(true);

		// �����޽��� ���
		new Thread(this).start();
		// �̺�Ʈ ������ ����
		eventUp();
	}

	private void eventUp() { // �̺�Ʈ�ҽ�-�̺�Ʈó���� ����
		// ����(MainChat)
		bt_create.addActionListener(this);
		bt_enter.addActionListener(this);
		bt_exit.addActionListener(this);

		// ��ȭ��(ChatClient)
		cc.sendTF.addActionListener(this);
		cc.bt_exit.addActionListener(this);
		cc.bt_addtodo.addActionListener(this);
		cc.bt_showtodo.addActionListener(this);
		cc.bt_deltodo.addActionListener(this);

		// �α��� â
		login.bt_exit.addActionListener(this);
		login.bt_login.addActionListener(this);
		login.bt_signup.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object ob = e.getSource();
		if (ob == bt_create) { // �游��� ��û
			String title = JOptionPane.showInputDialog(this, "ä�ù� ����:");
			// �������� �������� ����
			sendMsg(Constant.CREATEROOM + "|" + title);

			// ä�� â�� ���� ����
			cc.setTitle("ä�ù�-[" + title + "]");
			// ��ȭ�泻 �ο����� ��û
			sendMsg(Constant.ROOMUSERINFO + "|");

			// ���� ȭ���� ������ �ʰ� �ϰ�, ä�� â ȭ�� ���̰� ��
			setVisible(false);
			cc.setVisible(true);

		} else if (ob == bt_enter) { // ����� ��û
			if (selectedRoom == null) { // ���� ���õ��� �ʾ�����
				JOptionPane.showMessageDialog(this, "���� �������� �ʾҽ��ϴ�");
				return;
			}
			// ������ ������ �濡 ���� ��û
			sendMsg(Constant.ENTERROOM + "|" + selectedRoom);
			// ��ȭ�泻 �ο����� ��û
			sendMsg(Constant.ROOMUSERINFO + "|");

			// ���� ȭ���� ������ �ʰ� �ϰ�, ä�� â ȭ�� ���̰� ��
			setVisible(false);
			cc.setVisible(true);

		} else if (ob == cc.bt_exit) { // ��ȭ�� ������ ��û

			sendMsg(Constant.EXITROOM + "|");
			cc.setVisible(false);
			setVisible(true);

		} else if (ob == cc.sendTF) { // (TextField�Է�)�޽��� ������ ��û
			String msg = cc.sendTF.getText();

			if (msg.length() > 0) {
				sendMsg(Constant.PRINTMSG + "|" + msg);
				cc.sendTF.setText("");
			}

		} else if (ob == bt_exit) { // ������(���α׷�����) ��û
			sendMsg(Constant.EXITROBBY + "|" + ID);
			System.exit(0); // ���� �������α׷� �����ϱ�

		} else if (ob == cc.bt_addtodo) { // ���� �߰� ��â
			String todoName = JOptionPane.showInputDialog(this, "���� ��:");
			sendMsg(Constant.ADDTODO + "|" + todoName);

		} else if (ob == cc.bt_showtodo) { // ��ü ���� ä��â�� ���
			sendMsg(Constant.PRINTTODO + "|");

		} else if (ob == cc.bt_deltodo) { // ���� ����
			String todoName = JOptionPane.showInputDialog(this, "���� ��:");
			sendMsg(Constant.DELTODO + "|" + todoName);

		} else if (ob == login.bt_login) {
			ID = login.id.getText();
			String PW = login.pw.getText();
			
			if (ID.length() == 0) {	// ID�� �����̸�
				login.showFail("ID�� �Է��Ͻÿ�");
				
			} else if (PW.length() == 0) { // PW �� �����̸�
				login.showFail("PW�� �Է��Ͻÿ�");
				
			} else {
				sendMsg(Constant.SENDIDPW + "|" + ID + "%" + PW);
				
			}

		} else if (ob == login.bt_signup) {
			String SignUpID = login.id.getText();
			String SignUpPW = login.pw.getText();
			
			if (SignUpID.length() == 0) { // ID�� �����̸�
				login.showFail("ID�� �Է��Ͻÿ�");
				
			} else if (SignUpPW.length() == 0) { // PW �� �����̸�
				login.showFail("PW�� �Է��Ͻÿ�");
				
			} else {
				sendMsg(Constant.SIGNUP + "|" + SignUpID + "%" + SignUpPW);
				login.showSignupOK();
				
			}

		} else if (ob == login.bt_exit) {
			System.exit(0); // ���� �������α׷� �����ϱ�
		}
	}

	public void connect() { // (����)�������� ��û
		try {
			// ����õ�
			Socket s = new Socket("localhost", 5000); // Socket(String host<����ip>, int port<���񽺹�ȣ>)

			// in: �����޽��� �б� ��ü, msg : ����>Ŭ���̾�Ʈ
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			// out: �޽��� ������ ���� ��ü, msg : Ŭ���̾�Ʈ>����
			out = s.getOutputStream();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMsg(String msg) { // �������� �޽��� ������
		try {
			out.write((msg + "\n").getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		// ������ ���� �޽��� �б�
		// GUI���α׷����࿡ ���� ��ġ���ʴ� run �޼ҵ�� ����.
		try {
			while (true) {
				String msg = in.readLine();
				// msg: ������ ���� �޽���
				// msg==> "300|�ȳ��ϼ���" "160|�ڹٹ�--1,����Ŭ��--1,JDBC��--1"
				String msgs[] = msg.split("\\|");
				String protocol = msgs[0];

				switch (protocol) {
				case Constant.PRINTMSG: // ä��â�� �޽��� ���
					cc.ta.append(msgs[1] + "\n");
					cc.ta.setCaretPosition(cc.ta.getText().length());
					break;

				case Constant.CLEARMSG: // ä��â �����
					cc.ta.setText(null);
					break;

				case Constant.ROOMLIST: // �� ������ List�� �Ѹ���
					// �������� List�� �Ѹ���
					if (msgs.length > 1) {
						// ������ ���� �Ѱ� �̻��̾����� ����
						// ������ ����� ----> msg="160|" ������ ����
						String roomNames[] = msgs[1].split(",");
						// "�ڹٹ�--1,����Ŭ��--1,JDBC��--1"
						roomInfo.setListData(roomNames);
					} else {
						roomInfo.removeAll();
					}
					break;

				case Constant.ROBBYUSERINFO:// (���ǿ���) ��ȭ�� �ο�����
					String roomInwons[] = msgs[1].split(",");
					roomInwon.setListData(roomInwons);
					break;

				case Constant.ROOMUSERINFO:// (��ȭ�濡��) ��ȭ�� �ο�����
					String myRoomInwons[] = msgs[1].split(",");
					cc.li_inwon.setListData(myRoomInwons);
					break;

				case Constant.ROBBYWAITINFO: // ���� �ο�����
					String waitNames[] = msgs[1].split(",");
					waitInfo.setListData(waitNames);
					break;

				case Constant.ENTERROOM:// ��ȭ�� ����
					cc.ta.append("=========[" + msgs[1] + "]�� ����=========\n");
					cc.ta.setCaretPosition(cc.ta.getText().length());
					break;

				case Constant.EXITROOM:// ��ȭ�� ����
					cc.ta.append("=========[" + msgs[1] + "]�� ����=========\n");
					cc.ta.setCaretPosition(cc.ta.getText().length());
					break;

				case Constant.GETROOMNAME:// ������ ���� Ÿ��Ʋ ���� ���
					cc.setTitle("ä�ù�-[" + msgs[1] + "]");
					break;

				case Constant.LOGINOK:
					// (����)���� �˸�
					sendMsg(Constant.ENTERROBBY + "|");

					String nickName = JOptionPane.showInputDialog(this, "�г���	:");

					// ��ȭ�� ����
					sendMsg(Constant.WRITENICKNAME + "|" + nickName + "(" + login.id.getText() + ")");
					setVisible(true);
					login.setVisible(false);
					break;
					
				case Constant.LOGINFAIL:
					login.showFail(msgs[1]);
					break;
				}
				// Ŭ���̾�Ʈ switch end
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new ClientMain();
	}
}