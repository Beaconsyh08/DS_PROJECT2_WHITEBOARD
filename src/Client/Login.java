//chenliu 1041291
package Client;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.*;
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
    private static int port = 2019;

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

        //        private JTextField textFieldPort;
        JTextField textFieldPort = new JTextField();
        textFieldPort.setHorizontalAlignment(SwingConstants.CENTER);
//        textFieldPort.setFont(new Font("Georgia", Font.PLAIN, 20));
        textFieldPort.setBounds(163, 154, 130, 26);
        textFieldPort.setText("Enter Port");
        textFieldPort.setForeground(Color.GRAY);
        textFieldPort.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textFieldPort.getText().trim().equals("Enter Port")) {
                    textFieldPort.setText("");
                    textFieldPort.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textFieldPort.getText().trim().equals("")) {
                    textFieldPort.setText("Enter Port");
                    textFieldPort.setForeground(Color.GRAY);
                }
            }
        });
        frame.getContentPane().add(textFieldPort);
        textFieldPort.setColumns(10);

        JTextField textFieldIPAddress = new JTextField();
        textFieldIPAddress.setHorizontalAlignment(SwingConstants.CENTER);
//        textFieldIPAddress.setFont(new Font("Georgia", Font.PLAIN, 20));
        textFieldIPAddress.setText("127.0.0.1");
        textFieldIPAddress.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textFieldIPAddress.getText().trim().equals("127.0.0.1")) {
                    textFieldIPAddress.setText("");
                    textFieldIPAddress.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textFieldIPAddress.getText().trim().equals("")) {
                    textFieldIPAddress.setText("127.0.0.1");
                    textFieldIPAddress.setForeground(Color.GRAY);
                }
            }
        });
        textFieldIPAddress.setBounds(163, 191, 130, 26);
        frame.getContentPane().add(textFieldIPAddress);
        textFieldIPAddress.setColumns(10);

        try {
            socket = new Socket(ipAddress, port);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Fail: server not found.");
            System.exit(1);
            e.printStackTrace();
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
                } else if (password.equals("")) {
                    JOptionPane.showMessageDialog(null, "Fail: no passowrd entered.");
                }
                try {
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    JSONObject loginInfo = new JSONObject();
                    String firstPwd = DigestUtil.digest(password, DigestUtil.SALT, DigestUtil.DIGEST_TIMES);
                    String salt = SaltUtil.generateSalt();
                    Integer times = SaltUtil.getEncryptTimes();
                    String lastPwd = DigestUtil.digest(firstPwd, salt, times);

                    loginInfo.put("username", username);
                    loginInfo.put("firstPwd", firstPwd);
                    loginInfo.put("password", lastPwd);
                    loginInfo.put("method_name", "login");
                    loginInfo.put("salt", salt);
                    loginInfo.put("times", times);

                    dataOutputStream.writeUTF(loginInfo.toJSONString());
                    dataOutputStream.flush();
                } catch (UnknownHostException x) {
                    x.printStackTrace();
                } catch (IOException y) {
                    y.printStackTrace();
                }

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
                                    ClientWelcome clientWelcome = new ClientWelcome(username, socket, textFieldIPAddress, textFieldPort);
                                    frame.setVisible(false);
                                    break;
                                } else if (status.equals(4L)) {
                                    JOptionPane.showMessageDialog(null, "User already logged in");
                                    System.exit(1);
                                } else if (status.equals(2L)) {
                                    JOptionPane.showMessageDialog(null, "Welcome back!");
                                    ClientWelcome clientWelcome = new ClientWelcome(username, socket, textFieldIPAddress, textFieldPort);
                                    frame.setVisible(false);
                                    break;
                                } else if (status.equals(3L)) {
                                    JOptionPane.showMessageDialog(null, "Oops, wrong password. Or username already been registered.");
                                    System.exit(1);
                                    break;
                                } else {
                                    JOptionPane.showMessageDialog(null, "Unknown Error, please contact manager");
                                    System.exit(1);
                                    break;
                                }
                            } catch (IOException | ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                thread.setDaemon(true);
                thread.start();
            }
        });


        btnLogin.setBounds(170, 232, 117, 29);
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
