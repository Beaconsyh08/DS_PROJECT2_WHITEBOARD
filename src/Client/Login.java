//chenliu 1041291
package Client;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Login {

    private JFrame frame;
    private JTextField userArea;
    private JPasswordField passArea;

    private Socket socket;
    private static String ipAddress = "127.0.0.1";
    private static int port = 8888;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            Login window = new Login();
            window.frame.setVisible(true);
        } catch (ConnectException e) {
            JOptionPane.showMessageDialog(null, "Fail: server not found.");
        }

//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					Login window = new Login();
//					window.frame.setVisible(true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
    }

    /**
     * Create the application.
     */
    public Login() throws ConnectException {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() throws ConnectException {
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

        try {
            socket = new Socket(ipAddress, port);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //todo
        if (false) {
            JOptionPane.showMessageDialog(null, "Fail: server not found.");
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
                if (username.equals("")) {
                    JOptionPane.showMessageDialog(null, "Fail: no username entered.");
                    //todo throw error to prevent creating new canvas;
                } else if (password.equals("")) {
                    JOptionPane.showMessageDialog(null, "Fail: no passowrd entered.");
                }
                try {
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    JSONObject loginInfo = new JSONObject();
                    //todo 盐加密
                    loginInfo.put("username", username);
                    loginInfo.put("password", password);
                    loginInfo.put("method_name", "login");
                    dataOutputStream.writeUTF(loginInfo.toJSONString());
                    dataOutputStream.flush();
                } catch (UnknownHostException x) {
                    x.printStackTrace();
                } catch (IOException y) {
                    y.printStackTrace();
                }

                //todo handle msg receive

                final boolean[] flag = {true};
                Thread thread = new Thread() {
                    public void run() {
                        while (true) {
                            try {
                                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                                JSONParser jsonParser = new JSONParser();

                                JSONObject tmp = (JSONObject) jsonParser.parse(dataInputStream.readUTF());
                                Long status = (Long) tmp.get("status");

                                System.out.println(status);
                                if (status.equals(1L)) {
                                    JOptionPane.showMessageDialog(null, "Welcome, new account created");
                                    ClientWelcome clientWelcome = new ClientWelcome();
                                    frame.setVisible(false);
                                    break;
                                } else if (status.equals(2L)) {
                                    JOptionPane.showMessageDialog(null, "Welcome back!");
                                    ClientWelcome clientWelcome = new ClientWelcome();
                                    frame.setVisible(false);
                                    break;
                                } else if (status.equals(3L)) {
                                    JOptionPane.showMessageDialog(null, "Oops, wrong password");
                                    flag[0] = false;
                                    break;
                                } else {
                                    JOptionPane.showMessageDialog(null, "Unknown Error, please contact manager");
                                    flag[0] = false;
                                    break;
                                }
                            } catch (IOException | ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                if (flag[0]) {
                    thread.setDaemon(true);
                    thread.start();
                }

//				Board board = new Board();

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
