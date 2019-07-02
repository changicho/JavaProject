package client;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

//	ä�ù� UI
public class ChatRoomUI extends JFrame {
	JTextField sendTF;
	JLabel la_msg;
	JTextArea ta;
	JScrollPane sp_ta, sp_list;
	JList<String> li_inwon;
	JButton bt_change, bt_exit, bt_addtodo, bt_showtodo, bt_deltodo;
	JPanel p;

	public ChatRoomUI() {
		setTitle("ä�ù�");
		sendTF = new JTextField(15);
		la_msg = new JLabel("Message");
		
		ta = new JTextArea();
		ta.setLineWrap(true);	// TextArea ���α��̸� ����� text�߻��� �ڵ� �ٹٲ�
		
		li_inwon = new JList<String>();
		sp_ta = new JScrollPane(ta);
		sp_list = new JScrollPane(li_inwon);
		bt_deltodo = new JButton("���� ����");
		bt_exit = new JButton("������");
		bt_addtodo = new JButton("���� �߰�");
		bt_showtodo = new JButton("���� ǥ��");
		
		p = new JPanel();
		// ä��â
		sp_ta.setBounds(10, 10, 380, 390);
		la_msg.setBounds(10, 410, 60, 30);
		sendTF.setBounds(70, 410, 320, 30);
		// ä�ù� �ο� ���
		sp_list.setBounds(400, 10, 120, 260);
		
		// ��ư��
		bt_showtodo.setBounds(400, 290, 120, 30);
		bt_addtodo.setBounds(400, 330, 120, 30);
		bt_deltodo.setBounds(400, 370, 120, 30);
		bt_exit.setBounds(400, 410, 120, 30);
		
		// Panel�� �߰�
		p.setLayout(null);
		p.setBackground(Color.GRAY);
		p.add(sp_ta);
		p.add(la_msg);
		p.add(sendTF);
		p.add(sp_list);
		p.add(bt_showtodo);
		p.add(bt_addtodo);
		p.add(bt_deltodo);
		p.add(bt_exit);
		add(p);
		setBounds(300, 200, 550, 500);
		
		sendTF.requestFocus();
	}
}