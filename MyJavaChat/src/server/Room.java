package server;

import java.util.Vector;

// ��ȭ���� ����ǥ�� Ŭ����
public class Room {
	// ������
	String title;
	// �� �ο���
	int count;
	// ����(�� ������)
	String boss;
	// userV: ���� �濡 ������ Client���� ����
	Vector<Service> userV;
	// ���� ����
	Vector<Todo> todoV;

	public Room() {
		userV = new Vector<>();
		todoV = new Vector<>();
	}
}