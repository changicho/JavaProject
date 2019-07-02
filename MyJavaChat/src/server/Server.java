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

// Server클래스: 소켓을 통한 접속서비스, 접속클라이언트 관리
public class Server implements Runnable {
	// 모든 사용자(대기실사용자 + 대화방사용자)
	Vector<Service> allV;
	// 대기실 사용자
	Vector<Service> waitV;
	// 채팅방 벡터
	Vector<Room> roomV;
	// 할일 목록
	Vector<Todo> Todos;
	// 유저 계정 정보
	Vector<User> Users;

	public Server() {
		allV = new Vector<>();
		waitV = new Vector<>();
		roomV = new Vector<>();
		Todos = new Vector<>();
		Users = new Vector<>();

		// 방 정보 파일에서 입력
		myFileReadRoom();
		// 계정 정보 파일에서 입력
		myFileReadUser();

		new Thread(this).start();
	}

	@Override
	public void run() {
		try {
			// 현재 실행중인 ip + 명시된 port > 소켓서비스
			ServerSocket ss = new ServerSocket(5000);
			
			System.out.println("Start Server.......");
			while (true) {
				// 클라이언트 접속 대기
				Socket s = ss.accept(); // s: 접속한 클라이언트의 소켓정보

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

	public void writeRoom() {	// 방정보 저장
		File file = new File(Constant.ROOMS);
		FileWriter writer = null;

		try {
			// 기존 파일의 내용에 이어서 쓰려면 true를, 기존 내용을 없애고 새로 쓰려면 false를 지정한다.
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

	public void myFileReadRoom() {	// 방 정보 불러옴
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
	
	public void writeUser() {	// 계정정보 저장
		File file = new File(Constant.USERS);
		FileWriter writer = null;

		try {
			// 기존 파일의 내용에 이어서 쓰려면 true를, 기존 내용을 없애고 새로 쓰려면 false를 지정한다.
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

	public void myFileReadUser() {	// 계정정보 불러옴
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