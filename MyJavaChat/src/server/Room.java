package server;

import java.util.Vector;

// 대화방의 정보표현 클래스
public class Room {
	// 방제목
	String title;
	// 방 인원수
	int count;
	// 방장(방 개설자)
	String boss;
	// userV: 같은 방에 접속한 Client정보 저장
	Vector<Service> userV;
	// 할일 정보
	Vector<Todo> todoV;

	public Room() {
		userV = new Vector<>();
		todoV = new Vector<>();
	}
}