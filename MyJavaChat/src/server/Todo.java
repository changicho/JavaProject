package server;


public class Todo {
	String title;	// ���� ����
	String nickname;	// ���� ������
	
	public Todo(String title, String nickname) {
		this.title = title;
		this.nickname = nickname;
	}
	
	public String getInfo() {
		String result = "������ �� : " + nickname + " // ���� �̸� : " + title + "%n";
		
		return result;
	}
	
	public String getName() {
		return "������ �� : " + nickname;
	}
	
	public String getTitle() {
		return "���� �̸� : " + title;
	}
	
}
