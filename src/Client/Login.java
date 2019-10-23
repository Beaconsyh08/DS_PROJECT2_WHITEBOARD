//chenliu 1041291
package Client;

import org.json.simple.JSONObject;

import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Login {

	private JFrame frame;
	private JTextField userArea;
	private JPasswordField passArea;
	private static String ipAddress = "127.0.0.1";
	private static int port = 8888;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login window = new Login();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Login() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		userArea = new JTextField();
		userArea.setBounds(163, 80, 130, 26);
		frame.getContentPane().add(userArea);
		userArea.setColumns(10);

		passArea = new JPasswordField();
		passArea.setBounds(163, 117, 130, 26);
		frame.getContentPane().add(passArea);

		//todo
		if(false) {
			JOptionPane.showMessageDialog(null,"Fail: server not found.");
		}


		JButton btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String username = userArea.getText();
				String password = String.valueOf(passArea.getPassword());
				//check test area
				if(username.equals("")) {
					JOptionPane.showMessageDialog(null,"Fail: no username entered.");
					//todo throw error to prevent creating new canvas;
				}else if(password.equals("")) {
					JOptionPane.showMessageDialog(null,"Fail: no passowrd entered.");
				}
				try{
					Socket socket = new Socket(ipAddress, port);
					DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

					JSONObject loginInfo = new JSONObject();
					//todo 盐加密
					loginInfo.put("username", username);
					loginInfo.put("password", password);
					loginInfo.put("method_name", "login");
					dataOutputStream.writeUTF(loginInfo.toJSONString());
					dataOutputStream.flush();
				} catch (UnknownHostException x){
					x.printStackTrace();
				} catch (IOException y) {
					y.printStackTrace();
				}
				frame.setVisible(false);
//				Board board = new Board();


//				else {
//					if(args.equals("005")) {
//						JOptionPane.showMessageDialog(null,"Fail: server not found.");
//					}
//					else {
//						response = Client.router(request).split("%");
//						String status = response[1];
//						if(status.equals("succ")) {
//							frame.dispose();
//							Main.main(username);
//						}else if(status.equals("001")){
//							JOptionPane.showMessageDialog(null,"Fail: incorrect username.");
//						}else if(status.equals("002")){
//							JOptionPane.showMessageDialog(null,"Fail: incorrect password.");
//						}else {
//							JOptionPane.showMessageDialog(null,"Fail: server closed.");
//						}
//					}
//				}
			}
		});




		btnLogin.setBounds(170, 166, 117, 29);
		frame.getContentPane().add(btnLogin);

		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setBounds(75, 85, 72, 16);
		frame.getContentPane().add(lblUsername);

		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setBounds(75, 122, 72, 16);
		frame.getContentPane().add(lblPassword);

		JLabel lblCanvas = new JLabel("Canvas");
		lblCanvas.setBounds(197, 27, 72, 16);
		frame.getContentPane().add(lblCanvas);
	}
}
