package client;

import java.awt.Color;
import java.awt.Label;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Login extends JFrame {
	JTextField  id, pw, status;
	JButton bt_login, bt_signup, bt_exit;
	JLabel labelID, labelPW, labelSTATUS;
	JPanel p;

	public Login() {
		setTitle("로그인 창");

		bt_login = new JButton("로그인");
		bt_signup = new JButton("회원 가입");
		bt_exit = new JButton("나가기");
		
		labelID = new JLabel("ID");
		labelPW = new JLabel("PW");
		labelSTATUS = new JLabel("서버로부터 응답");
		
		id = new JTextField ();
		pw = new JTextField ();
		status = new JTextField();
		
		p = new JPanel();
		// 채팅창
		id.setBounds(80, 10, 250, 30);
		pw.setBounds(80, 50, 250, 30);
		status.setBounds(150, 100, 200, 30);
		
		labelID.setBounds(20, 10, 50, 30);
		labelPW.setBounds(20, 50, 50, 30);
		labelSTATUS.setBounds(20,100,100,30);
		
		// 버튼들
		bt_login.setBounds(10, 200, 120, 30);
		bt_signup.setBounds(140, 200, 120, 30);
		bt_exit.setBounds(270, 200, 100, 30);
		
		// Panel에 추가
		p.setLayout(null);
		p.setBackground(Color.GRAY);
		p.add(id);
		p.add(pw);
		p.add(status);
		p.add(labelID);
		p.add(labelPW);
		p.add(labelSTATUS);
		
		p.add(bt_login);
		p.add(bt_signup);
		p.add(bt_exit);
		add(p);
		setBounds(500, 300, 400, 300);
		status.setText("어서오세요");
		
	}
	
	// 로그인 실패시 실패 이유 출력. str : 실패 이유
	public void showFail(String str) {
		status.setText(str);
	}
	
	// 회원가입 성공시 완료되었다고 출력
	public void showSignupOK() {
		status.setText("signupOK");
	}
}
