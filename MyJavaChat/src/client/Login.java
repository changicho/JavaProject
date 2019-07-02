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
		setTitle("�α��� â");

		bt_login = new JButton("�α���");
		bt_signup = new JButton("ȸ�� ����");
		bt_exit = new JButton("������");
		
		labelID = new JLabel("ID");
		labelPW = new JLabel("PW");
		labelSTATUS = new JLabel("�����κ��� ����");
		
		id = new JTextField ();
		pw = new JTextField ();
		status = new JTextField();
		
		p = new JPanel();
		// ä��â
		id.setBounds(80, 10, 250, 30);
		pw.setBounds(80, 50, 250, 30);
		status.setBounds(150, 100, 200, 30);
		
		labelID.setBounds(20, 10, 50, 30);
		labelPW.setBounds(20, 50, 50, 30);
		labelSTATUS.setBounds(20,100,100,30);
		
		// ��ư��
		bt_login.setBounds(10, 200, 120, 30);
		bt_signup.setBounds(140, 200, 120, 30);
		bt_exit.setBounds(270, 200, 100, 30);
		
		// Panel�� �߰�
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
		status.setText("�������");
		
	}
	
	// �α��� ���н� ���� ���� ���. str : ���� ����
	public void showFail(String str) {
		status.setText(str);
	}
	
	// ȸ������ ������ �Ϸ�Ǿ��ٰ� ���
	public void showSignupOK() {
		status.setText("signupOK");
	}
}
