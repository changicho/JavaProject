package server;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import constant.Constant;

// ServerŬ����: ������ ���� ���Ӽ���, ����Ŭ���̾�Ʈ ����
public class Server implements Runnable {
	// ��� �����(���ǻ���� + ��ȭ������)
	Vector<Service> allV;
	// ���� �����
	Vector<Service> waitV;
	// ä�ù� ����
	Vector<Room> roomV;
	// ���� ���
	Vector<Todo> Todos;
	// ���� ���� ����
	Vector<User> Users;

	public Server() {
		allV = new Vector<>();
		waitV = new Vector<>();
		roomV = new Vector<>();
		Todos = new Vector<>();
		Users = new Vector<>();

		// �� ���� ���Ͽ��� �Է�
		myFileReadRoom();
		// ���� ���� ���Ͽ��� �Է�
		myFileReadUser();

		new Thread(this).start();
	}

	@Override
	public void run() {
		try {
			// ���� �������� ip + ��õ� port > ���ϼ���
			ServerSocket ss = new ServerSocket(5000);
			
			System.out.println("Start Server.......");
			while (true) {
				// Ŭ���̾�Ʈ ���� ���
				Socket s = ss.accept(); // s: ������ Ŭ���̾�Ʈ�� ��������

				System.out.println("waiting");
				Service ser = new Service(s, this);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Server();
	}

	public void writeRoom() {	// ������ ����
		File file = new File(Constant.ROOMS);
		FileWriter writer = null;

		try {
			// ���� ������ ���뿡 �̾ ������ true��, ���� ������ ���ְ� ���� ������ false�� �����Ѵ�.
			writer = new FileWriter(file, false);

			for (Room room : roomV) {
				writer.write(room.title + "," + room.boss + ",");
				for (Todo todo : room.todoV) {
					writer.write(todo.title + "%" + todo.nickname);
				}
				writer.write('\n');
			}
			writer.flush();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void myFileReadRoom() {	// �� ���� �ҷ���
		List<String> FileStrlist = new ArrayList<String>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(Constant.ROOMS));
			while (true) {
				String line = br.readLine();
				if (line == null) {
					break;
				}
					
				FileStrlist.add(line);
			}
			br.close();
			
		} catch (FileNotFoundException e) {
			e.getStackTrace();
			
		} catch (IOException e) {
			e.getStackTrace();
		}

		for (String readLine : FileStrlist) {
			String msgs[] = readLine.split(",");

			Room myRoom = new Room();
			myRoom.title = msgs[0];
			myRoom.count = 0;
			myRoom.boss = msgs[1];
			
			if(msgs.length==3) {
				String todos[] = msgs[2].split("%");
			
				for (int i = 0; i < todos.length; i = i + 2) {
					myRoom.todoV.add(new Todo(todos[i], todos[i + 1]));
				}
			}
			

			roomV.add(myRoom);
		}
	}
	
	public void writeUser() {	// �������� ����
		File file = new File(Constant.USERS);
		FileWriter writer = null;

		try {
			// ���� ������ ���뿡 �̾ ������ true��, ���� ������ ���ְ� ���� ������ false�� �����Ѵ�.
			writer = new FileWriter(file, false);

			for (User user : Users) {
				writer.write(user.ID + "%" + user.PW);
				writer.write('\n');
			}
			writer.flush();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void myFileReadUser() {	// �������� �ҷ���
		List<String> list = new ArrayList<String>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(Constant.USERS));
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;
				list.add(line);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.getStackTrace();
		} catch (IOException e) {
			e.getStackTrace();
		}

		for (String readLine : list) {
			String idpw[] = readLine.split("%");
			Users.add(new User(idpw[0], idpw[1]));
		}
	}
}