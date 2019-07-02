package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;

import constant.Constant;

// ���� Ŭ���̾�Ʈ���� ��û ó���� ���� ������
public class Service extends Thread {
	Room myRoom; // Ŭ���̾�Ʈ�� ������ ��ȭ��

	BufferedReader in; // ���ϰ��� ����¼���
	OutputStream out;

	Vector<Service> allV; // ��� �����(���ǻ���� + ��ȭ������)
	Vector<Service> waitV; // ���� �����
	Vector<Room> roomV; // ������ ��ȭ��
	Vector<Todo> Todos; // �� ������ ���� ��� ������ ���� ����

	Socket s;
	String nickName; // Ŭ���̾�Ʈ�� �г���
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
		
		boolean isExit=false;	// �κ� �������� �Ǻ�
		try {
			while (!isExit) {
				// Ŭ���̾�Ʈ�� ��� �޽����� �ޱ�
				String msg = in.readLine();

				if (msg == null) // ���������� ����
					return;

				if (msg.trim().length() > 0) {
					System.out.println("from Client: " + msg + ":" + s.getInetAddress().getHostAddress());
					String msgs[] = msg.split("\\|");
					String protocol = msgs[0];

					switch (protocol) {
					case Constant.ENTERROBBY: // ���� ����
						allV.add(this); // ��ü����ڿ� ���
						waitV.add(this); // ���ǻ���ڿ� ���

						break;

					case Constant.WRITENICKNAME: // ��ȭ�� �Է�
						nickName = msgs[1];
						// ���� ��ȭ�� �Է������� ������ ������ ���
						messageWait(Constant.ROOMLIST + "|" + getRoomInfo());
						messageWait(Constant.ROBBYWAITINFO + "|" + getWaitInwon());

						break;

					case Constant.CREATEROOM: // �游��� (��ȭ�� ����)
						messageTo(Constant.CLEARMSG + "|");

						myRoom = new Room();
						myRoom.title = msgs[1];
						myRoom.count = 1;
						myRoom.boss = nickName;
						roomV.add(myRoom);

						// ���� -> ��ȭ�� �̵�!!
						waitV.remove(this);
						myRoom.userV.add(this);
						messageRoom(Constant.ENTERROOM + "|" + nickName);

						// ���ο����� ���� �˸�
						messageWait(Constant.ROOMLIST + "|" + getRoomInfo());
						messageWait(Constant.ROBBYWAITINFO + "|" + getWaitInwon());

						// �� ��� ���� ����
						server.writeRoom();

						break;

					case Constant.ROBBYUSERINFO: // (���ǿ���) ��ȭ�� �ο�����
						messageTo(Constant.ROBBYUSERINFO + "|" + getRoomInwon(msgs[1]));

						break;
					case Constant.ROOMUSERINFO: // (��ȭ�濡��) ��ȭ�� �ο�����
						messageRoom(Constant.ROOMUSERINFO + "|" + getRoomInwon());

						break;

					case Constant.ENTERROOM: // ����� (��ȭ�� ����)
						messageTo(Constant.CLEARMSG + "|"); // ���� ä�� ��� �ʱ�ȭ

						for (int i = 0; i < roomV.size(); i++) { // ���̸� ã�´�.
							Room r = roomV.get(i);
							if (r.title.equals(msgs[1])) { // ��ġ�ϴ� �� ã������

								myRoom = r;
								myRoom.count++; // �ο��� 1����

								break;
							}
						}
						// ���� > ��ȭ�� �̵�!!
						waitV.remove(this);
						myRoom.userV.add(this);

						messageRoom(Constant.ENTERROOM + "|" + nickName);
						messageTo(Constant.GETROOMNAME + "|" + myRoom.title);
						messageWait(Constant.ROOMLIST + "|" + getRoomInfo());
						messageWait(Constant.ROBBYWAITINFO + "|" + getWaitInwon());

						break;

					case Constant.PRINTMSG: // �޽���
						// Ŭ���̾�Ʈ���� �޽��� ������
						messageRoom(Constant.PRINTMSG + "|" + "[" + nickName + "]�� " + msgs[1]);

						break;

					case Constant.EXITROOM: // ��ȭ�� ����
						myRoom.count--; // �ο��� ����
						// ���ο��鿡�� ���� �˸���.
						messageRoom(Constant.EXITROOM + "|" + nickName);
						// ��ȭ�� > ���� �̵�!!
						myRoom.userV.remove(this);
						waitV.add(this);

						if (myRoom.userV.size() == 0) {
							if (!myRoom.title.equals("����ä�ù�")) {
								roomV.remove(myRoom);
							}
						}
						// ��ȭ�� ������ ���ο� �ٽ����
						messageWait(Constant.ROOMLIST + "|" + getRoomInfo());
						// ���ǿ� ������ �ٽ����
						messageRoom(Constant.ROOMUSERINFO + "|" + getRoomInwon());
						// ���ǿ� ��� �ο� ���� �ٽ� ���
						messageWait(Constant.ROBBYWAITINFO + "|" + getWaitInwon());

						// �� ��� ���� ����
						server.writeRoom();

						break;

					case Constant.ADDTODO: // ���� ��� �߰�
						System.out.println("add Todo!");
						myRoom.todoV.add(new Todo(msgs[1], nickName));

						messageRoom(Constant.PRINTMSG + "|" + "===�߰��� ���� ����====");
						messageRoom(Constant.PRINTMSG + "|" + "���� �̸�: " + msgs[1]);
						messageRoom(Constant.PRINTMSG + "|" + "������ ��: " + nickName);
						messageRoom(Constant.PRINTMSG + "|" + "=======================");

						// �� ��� ����(���ϵ� ���� ���ŵ�) ����
						server.writeRoom();

						break;

					case Constant.PRINTTODO: // ��ü ���� ��� ���
						System.out.println("print all Todo!");
						messageTo(Constant.CLEARMSG + "|");
						messageTo(Constant.PRINTMSG + "|" + "==���� ��� ǥ��==");
						for (Todo T : myRoom.todoV) {
							messageTo(Constant.PRINTMSG + "|" + "=======================");
							messageTo(Constant.PRINTMSG + "|" + T.getTitle());
							messageTo(Constant.PRINTMSG + "|" + T.getName());
							messageTo(Constant.PRINTMSG + "|" + "=======================");
						}

						break;

					case Constant.DELTODO: // ���� ��� ����
						System.out.println("delete Todo!" + msgs[1]);
						deleteTodo(msgs[1]);
						messageRoom(Constant.PRINTMSG + "|" + "�� �� : " + msgs[1] + "�� �����߽��ϴ�.");

						// �� ��� ����(���ϵ� ���� ���ŵ�) ����
						server.writeRoom();

						break;

					case Constant.SENDIDPW:
						boolean flag = false;
						String idpw[] = msgs[1].split("%");

						for (User U : server.Users) {
							if (U.ID.equals(idpw[0])) { // ID ����
								if (U.PW.endsWith(idpw[1])) { // ID�� ��ġ�ϸ�, PW�� ��ġ�ϴ��� Ȯ��
									if (!U.ISLOGIN) { // �̹� �α��� ���ִ��� Ȯ��
										messageTo(Constant.LOGINOK + "|"); // ��� ���� �����ϸ� �α���
										U.ISLOGIN = true; // �α��εǾ��ִٴ� ���·� �ٲ�
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

						boolean isExist = false; // �̹� �����ϴ� ID���� üũ
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

	public String getRoomInfo() { // ��ü �� ��� ������ �޾ƿ�
		String str = "";
		for (int i = 0; i < roomV.size(); i++) {
			// "�ڹٹ�--1,����Ŭ��--1,JDBC��--1"
			Room r = roomV.get(i);
			str += r.title + "--" + r.count;
			if (i < roomV.size() - 1)
				str += ",";
		}
		return str;
	}

	public String getRoomInwon() { // ���� ���� �ο������� �޾ƿ�
		String str = "";

		for (int i = 0; i < myRoom.userV.size(); i++) {
			// "�浿,����,�ֿ�"
			Service ser = myRoom.userV.get(i);
			str += ser.nickName;
			if (i < myRoom.userV.size() - 1)
				str += ",";
		}
		System.out.println("room Inwon : " + str);
		return str;
	}

	public String getRoomInwon(String title) { // ������ Ŭ���� ���� �ο� ������ ���
		if (title.equals("����ä�ù�")) {
			return "����ä�ù��� �ο��� ǥ������ �ʽ��ϴ�,";
		}

		String str = "";
		for (int i = 0; i < roomV.size(); i++) {
			Room room = roomV.get(i);

			if (room.title.equals(title)) {
				if (room.count == 0) {
					return "������ 0��,";
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

	public String getWaitInwon() { // ���ǿ� ��� �ο� ������ ����
		String str = "";
		for (int i = 0; i < waitV.size(); i++) {
			Service ser = waitV.get(i);

			str += ser.nickName;
			if (i < waitV.size() - 1)
				str += ",";
		}
		return str;
	}

	public void messageAll(String msg) { // ���ӵ� ��� Ŭ���̾�Ʈ(����+��ȭ��)���� �޽��� ����
		for (int i = 0; i < allV.size(); i++) { // ���� �ε���
			// ������ Ŭ���̾�Ʈ�� ���� ������
			Service service = allV.get(i);

			try {
				service.messageTo(msg);
			} catch (IOException e) { // �����߻�, Ŭ���̾�Ʈ ���� ���´�.
				// ���� ���� Ŭ���̾�Ʈ�� ���Ϳ��� ����!!
				allV.remove(i--);

				System.out.println("Ŭ���̾�Ʈ ���� ����!!");
			}
		}
	}

	public void messageWait(String msg) { // ���� ����ڿ��� �޽��� ����
		for (int i = 0; i < waitV.size(); i++) {
			// ������ Ŭ���̾�Ʈ ������
			Service service = waitV.get(i);

			try {
				System.out.println("  To wait " + i + " : " + msg);
				service.messageTo(msg);
			} catch (IOException e) { // �����߻�, Ŭ���̾�Ʈ ���� ���´�.
				// ���� ���� Ŭ���̾�Ʈ�� ���Ϳ��� ����!!
				waitV.remove(i--);

				System.out.println("Ŭ���̾�Ʈ ���� ����!!");
			}
		}
	}

	public void messageRoom(String msg) { // ��ȭ�����ڿ��� �޽��� ����
		for (int i = 0; i < myRoom.userV.size(); i++) {
			// ������ Ŭ���̾�Ʈ ������
			Service service = myRoom.userV.get(i);

			try {
				service.messageTo(msg);
			} catch (IOException e) {
				myRoom.userV.remove(i--);

				System.out.println("Ŭ���̾�Ʈ ���� ����!!");
			}
		}
	}

	public void deleteTodo(String msg) { // ���� ��Ͽ��� msg �̸��� ���� �� ����
		for (int i = 0; i < myRoom.todoV.size(); i++) {
			if (myRoom.todoV.get(i).title.equals(msg)) {
				myRoom.todoV.remove(i);
			}
		}
	}

	public void messageTo(String msg) throws IOException { // Ư�� Ŭ���̾�Ʈ���� �޽��� ���� (���� ����>Ŭ���̾�Ʈ �޽��� ����)
		System.out.println("  To Client: " + msg);
		out.write((msg + "\n").getBytes());
	}
}