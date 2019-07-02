package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;

import constant.Constant;

// 접속 클라이언트마다 요청 처리를 위한 쓰레드
public class Service extends Thread {
	Room myRoom; // 클라이언트가 입장한 대화방

	BufferedReader in; // 소켓관련 입출력서비스
	OutputStream out;

	Vector<Service> allV; // 모든 사용자(대기실사용자 + 대화방사용자)
	Vector<Service> waitV; // 대기실 사용자
	Vector<Room> roomV; // 개설된 대화방
	Vector<Todo> Todos; // 방 마다의 할일 목록 저장을 위한 벡터

	Socket s;
	String nickName; // 클라이언트의 닉네임
	Server server;

	public Service(Socket s, Server server) {
		allV = server.allV;
		waitV = server.waitV;
		roomV = server.roomV;
		Todos = server.Todos;
		this.server = server;

		this.s = s;

		try {
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			out = s.getOutputStream();

			start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("service starts\n");
		
		boolean isExit=false;	// 로비를 나갔는지 판별
		try {
			while (!isExit) {
				// 클라이언트의 모든 메시지를 받기
				String msg = in.readLine();

				if (msg == null) // 비정상적인 종료
					return;

				if (msg.trim().length() > 0) {
					System.out.println("from Client: " + msg + ":" + s.getInetAddress().getHostAddress());
					String msgs[] = msg.split("\\|");
					String protocol = msgs[0];

					switch (protocol) {
					case Constant.ENTERROBBY: // 대기실 접속
						allV.add(this); // 전체사용자에 등록
						waitV.add(this); // 대기실사용자에 등록

						break;

					case Constant.WRITENICKNAME: // 대화명 입력
						nickName = msgs[1];
						// 최초 대화명 입력했을때 대기실의 정보를 출력
						messageWait(Constant.ROOMLIST + "|" + getRoomInfo());
						messageWait(Constant.ROBBYWAITINFO + "|" + getWaitInwon());

						break;

					case Constant.CREATEROOM: // 방만들기 (대화방 입장)
						messageTo(Constant.CLEARMSG + "|");

						myRoom = new Room();
						myRoom.title = msgs[1];
						myRoom.count = 1;
						myRoom.boss = nickName;
						roomV.add(myRoom);

						// 대기실 -> 대화방 이동!!
						waitV.remove(this);
						myRoom.userV.add(this);
						messageRoom(Constant.ENTERROOM + "|" + nickName);

						// 방인원에게 입장 알림
						messageWait(Constant.ROOMLIST + "|" + getRoomInfo());
						messageWait(Constant.ROBBYWAITINFO + "|" + getWaitInwon());

						// 방 목록 갱신 저장
						server.writeRoom();

						break;

					case Constant.ROBBYUSERINFO: // (대기실에서) 대화방 인원정보
						messageTo(Constant.ROBBYUSERINFO + "|" + getRoomInwon(msgs[1]));

						break;
					case Constant.ROOMUSERINFO: // (대화방에서) 대화방 인원정보
						messageRoom(Constant.ROOMUSERINFO + "|" + getRoomInwon());

						break;

					case Constant.ENTERROOM: // 방들어가기 (대화방 입장)
						messageTo(Constant.CLEARMSG + "|"); // 이전 채팅 기록 초기화

						for (int i = 0; i < roomV.size(); i++) { // 방이름 찾는다.
							Room r = roomV.get(i);
							if (r.title.equals(msgs[1])) { // 일치하는 방 찾았으면

								myRoom = r;
								myRoom.count++; // 인원수 1증가

								break;
							}
						}
						// 대기실 > 대화방 이동!!
						waitV.remove(this);
						myRoom.userV.add(this);

						messageRoom(Constant.ENTERROOM + "|" + nickName);
						messageTo(Constant.GETROOMNAME + "|" + myRoom.title);
						messageWait(Constant.ROOMLIST + "|" + getRoomInfo());
						messageWait(Constant.ROBBYWAITINFO + "|" + getWaitInwon());

						break;

					case Constant.PRINTMSG: // 메시지
						// 클라이언트에게 메시지 보내기
						messageRoom(Constant.PRINTMSG + "|" + "[" + nickName + "]▶ " + msgs[1]);

						break;

					case Constant.EXITROOM: // 대화방 퇴장
						myRoom.count--; // 인원수 감소
						// 방인원들에게 퇴장 알린다.
						messageRoom(Constant.EXITROOM + "|" + nickName);
						// 대화방 > 대기실 이동!!
						myRoom.userV.remove(this);
						waitV.add(this);

						if (myRoom.userV.size() == 0) {
							if (!myRoom.title.equals("자유채팅방")) {
								roomV.remove(myRoom);
							}
						}
						// 대화방 퇴장후 방인원 다시출력
						messageWait(Constant.ROOMLIST + "|" + getRoomInfo());
						// 대기실에 방정보 다시출력
						messageRoom(Constant.ROOMUSERINFO + "|" + getRoomInwon());
						// 대기실에 대기 인원 정보 다시 출력
						messageWait(Constant.ROBBYWAITINFO + "|" + getWaitInwon());

						// 방 목록 갱신 저장
						server.writeRoom();

						break;

					case Constant.ADDTODO: // 할일 목록 추가
						System.out.println("add Todo!");
						myRoom.todoV.add(new Todo(msgs[1], nickName));

						messageRoom(Constant.PRINTMSG + "|" + "===추가된 할일 정보====");
						messageRoom(Constant.PRINTMSG + "|" + "할일 이름: " + msgs[1]);
						messageRoom(Constant.PRINTMSG + "|" + "생성자 명: " + nickName);
						messageRoom(Constant.PRINTMSG + "|" + "=======================");

						// 방 목록 갱신(할일도 같이 갱신됨) 저장
						server.writeRoom();

						break;

					case Constant.PRINTTODO: // 전체 할일 목록 출력
						System.out.println("print all Todo!");
						messageTo(Constant.CLEARMSG + "|");
						messageTo(Constant.PRINTMSG + "|" + "==할일 목록 표시==");
						for (Todo T : myRoom.todoV) {
							messageTo(Constant.PRINTMSG + "|" + "=======================");
							messageTo(Constant.PRINTMSG + "|" + T.getTitle());
							messageTo(Constant.PRINTMSG + "|" + T.getName());
							messageTo(Constant.PRINTMSG + "|" + "=======================");
						}

						break;

					case Constant.DELTODO: // 할일 목록 지움
						System.out.println("delete Todo!" + msgs[1]);
						deleteTodo(msgs[1]);
						messageRoom(Constant.PRINTMSG + "|" + "할 일 : " + msgs[1] + "을 삭제했습니다.");

						// 방 목록 갱신(할일도 같이 갱신됨) 저장
						server.writeRoom();

						break;

					case Constant.SENDIDPW:
						boolean flag = false;
						String idpw[] = msgs[1].split("%");

						for (User U : server.Users) {
							if (U.ID.equals(idpw[0])) { // ID 구분
								if (U.PW.endsWith(idpw[1])) { // ID가 일치하면, PW도 일치하는지 확인
									if (!U.ISLOGIN) { // 이미 로그인 되있는지 확인
										messageTo(Constant.LOGINOK + "|"); // 모든 조건 만족하면 로그임
										U.ISLOGIN = true; // 로그인되어있다는 상태로 바꿈
									} else {
										messageTo(Constant.LOGINFAIL + "|" + "already Logined");
									}
								} else {
									messageTo(Constant.LOGINFAIL + "|" + "wrong PW");
								}

								flag = true;
								break;
							}
						}
						if (!flag) {
							messageTo(Constant.LOGINFAIL + "|" + "there are no match ID");
						}

						break;

					case Constant.SIGNUP:
						String idpw2[] = msgs[1].split("%");

						boolean isExist = false; // 이미 존재하는 ID인지 체크
						for (User U : server.Users) {
							if (U.ID.equals(idpw2[0])) {
								messageTo(Constant.LOGINFAIL + "|" + "already exist ID");
								isExist = true;
								break;
							}
						}

						if (!isExist) {
							server.Users.add(new User(idpw2[0], idpw2[1]));
							server.writeUser();
						}

						break;

					case Constant.EXITROBBY:
						System.out.println(msgs[1]);

						for (User U : server.Users) {
							if (U.ID.equals(msgs[1])) {
								U.ISLOGIN = false;
								break;
							}
						}
						isExit = true;
						
						break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("quit thread");
	}

	public String getRoomInfo() { // 전체 방 목록 정보를 받아옴
		String str = "";
		for (int i = 0; i < roomV.size(); i++) {
			// "자바방--1,오라클방--1,JDBC방--1"
			Room r = roomV.get(i);
			str += r.title + "--" + r.count;
			if (i < roomV.size() - 1)
				str += ",";
		}
		return str;
	}

	public String getRoomInwon() { // 같은 방의 인원정보를 받아옴
		String str = "";

		for (int i = 0; i < myRoom.userV.size(); i++) {
			// "길동,라임,주원"
			Service ser = myRoom.userV.get(i);
			str += ser.nickName;
			if (i < myRoom.userV.size() - 1)
				str += ",";
		}
		System.out.println("room Inwon : " + str);
		return str;
	}

	public String getRoomInwon(String title) { // 방제목 클릭시 방의 인원 정보를 출력
		if (title.equals("자유채팅방")) {
			return "자유채팅방은 인원을 표시하지 않습니다,";
		}

		String str = "";
		for (int i = 0; i < roomV.size(); i++) {
			Room room = roomV.get(i);

			if (room.title.equals(title)) {
				if (room.count == 0) {
					return "참여자 0명,";
				}
				for (int j = 0; j < room.userV.size(); j++) {
					Service ser = room.userV.get(j);
					str += ser.nickName;
					if (j < room.userV.size() - 1)
						str += ",";
				}
				break;
			}
		}
		return str;
	}

	public String getWaitInwon() { // 대기실에 대기 인원 정보를 얻어옴
		String str = "";
		for (int i = 0; i < waitV.size(); i++) {
			Service ser = waitV.get(i);

			str += ser.nickName;
			if (i < waitV.size() - 1)
				str += ",";
		}
		return str;
	}

	public void messageAll(String msg) { // 접속된 모든 클라이언트(대기실+대화방)에게 메시지 전달
		for (int i = 0; i < allV.size(); i++) { // 벡터 인덱스
			// 각각의 클라이언트의 서비스 얻어오기
			Service service = allV.get(i);

			try {
				service.messageTo(msg);
			} catch (IOException e) { // 에러발생, 클라이언트 접속 끊는다.
				// 접속 끊긴 클라이언트를 벡터에서 삭제!!
				allV.remove(i--);

				System.out.println("클라이언트 접속 끊음!!");
			}
		}
	}

	public void messageWait(String msg) { // 대기실 사용자에게 메시지 전달
		for (int i = 0; i < waitV.size(); i++) {
			// 각각의 클라이언트 얻어오기
			Service service = waitV.get(i);

			try {
				System.out.println("  To wait " + i + " : " + msg);
				service.messageTo(msg);
			} catch (IOException e) { // 에러발생, 클라이언트 접속 끊는다.
				// 접속 끊긴 클라이언트를 벡터에서 삭제!!
				waitV.remove(i--);

				System.out.println("클라이언트 접속 끊음!!");
			}
		}
	}

	public void messageRoom(String msg) { // 대화방사용자에게 메시지 전달
		for (int i = 0; i < myRoom.userV.size(); i++) {
			// 각각의 클라이언트 얻어오기
			Service service = myRoom.userV.get(i);

			try {
				service.messageTo(msg);
			} catch (IOException e) {
				myRoom.userV.remove(i--);

				System.out.println("클라이언트 접속 끊음!!");
			}
		}
	}

	public void deleteTodo(String msg) { // 할일 목록에서 msg 이름을 가진 것 삭제
		for (int i = 0; i < myRoom.todoV.size(); i++) {
			if (myRoom.todoV.get(i).title.equals(msg)) {
				myRoom.todoV.remove(i);
			}
		}
	}

	public void messageTo(String msg) throws IOException { // 특정 클라이언트에게 메시지 전달 (실제 서버>클라이언트 메시지 전달)
		System.out.println("  To Client: " + msg);
		out.write((msg + "\n").getBytes());
	}
}