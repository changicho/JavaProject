package server;


public class Todo {
	String title;	// 且老 力格
	String nickname;	// 且老 积己磊
	
	public Todo(String title, String nickname) {
		this.title = title;
		this.nickname = nickname;
	}
	
	public String getInfo() {
		String result = "积己磊 疙 : " + nickname + " // 且老 捞抚 : " + title + "%n";
		
		return result;
	}
	
	public String getName() {
		return "积己磊 疙 : " + nickname;
	}
	
	public String getTitle() {
		return "且老 捞抚 : " + title;
	}
	
}
