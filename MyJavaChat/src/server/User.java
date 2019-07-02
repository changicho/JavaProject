package server;

public class User {
	String ID;
	String PW;
	boolean ISLOGIN = false;

	public User(String ID, String PW) {
		this.ID = ID;
		this.PW = PW;
		this.ISLOGIN = false;
	}
}
