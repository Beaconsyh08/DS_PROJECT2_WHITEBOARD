//chenliu 1041291
package Client;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Login {

    private static String ipAddress = "127.0.0.1";
    private static int port = 2019;
    private JFrame frame;
    private JTextField userArea;
    private JPasswordField passArea;
    private Socket socket;
    private DataOutputStream dataOutputStream;

    /**
     * Create the application.
     */
    public Login() {
        initialize();
    }

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
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        userArea = new JTextField();
        userArea.setBounds(233, 88, 130, 26);
        frame.getContentPane().add(userArea);
        userArea.setColumns(10);

        passArea = new JPasswordField();
        passArea.setBounds(233, 133, 130, 26);
        frame.getContentPane().add(passArea);

        //        private JTextField textFieldPort;
        JTextField textFieldPort = new JTextField();
        textFieldPort.setHorizontalAlignment(SwingConstants.CENTER);
//        textFieldPort.setFont(new Font("Georgia", Font.PLAIN, 20));
        textFieldPort.setBounds(233, 229, 130, 26);
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
        textFieldIPAddress.setBounds(233, 181, 130, 26);
        frame.getContentPane().add(textFieldIPAddress);
        textFieldIPAddress.setColumns(10);


        JButton btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Georgia", Font.PLAIN, 20));
        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = userArea.getText();
                String password = String.valueOf(passArea.getPassword());
                String portStr = textFieldPort.getText();
                String ipAddress = textFieldIPAddress.getText();
                if (portStr.equals("") || (portStr.equals("Enter Port"))) {
                    port = 2019;
                } else {
                    port = Integer.parseInt(portStr);
                }

                if (username.equals("")) {
                    JOptionPane.showMessageDialog(null, "Fail: no username entered.");
                } else if (password.equals("")) {
                    JOptionPane.showMessageDialog(null, "Fail: no password entered.");
                } else if (port < 1025 || port > 65536) {
                    JOptionPane.showMessageDialog(null, "Fail: port number should in range 1025~65536.");
                } else {
                    try {
                        socket = new Socket(ipAddress, port);
                        if (socket.isConnected()) {
                            try {
                                dataOutputStream = new DataOutputStream(socket.getOutputStream());
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


                                Thread thread = new Thread() {
                                    public void run() {
                                        while (true) {
                                            try {
                                                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                                                JSONParser jsonParser = new JSONParser();

                                                JSONObject tmp = (JSONObject) jsonParser.parse(dataInputStream.readUTF());
                                                Long status = (Long) tmp.get("status");

                                                System.out.println("status" + status);
                                                if (status.equals(1L)) {
                                                    JOptionPane.showMessageDialog(null, "Welcome, new account created");
                                                    UserProfile user = new UserProfile(username, false);
                                                    ClientWelcome clientWelcome = new ClientWelcome(username, socket, textFieldIPAddress, textFieldPort, user);
                                                    frame.setVisible(false);
                                                    // reg name to connection

                                                    try {
                                                        sendMsg4("system", user.getUserName(), "regUserName", Boolean.toString(user.isManager()));
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    } catch (ParseException e) {
                                                        e.printStackTrace();
                                                    }
                                                    break;
                                                } else if (status.equals(4L)) {
                                                    JOptionPane.showMessageDialog(null, "User already logged in");
                                                    // todo delete connection
//                                    sendMsg("system", username, "exit");
//                                                    System.exit(1);
                                                } else if (status.equals(2L)) {
                                                    JOptionPane.showMessageDialog(null, "Welcome back!");
                                                    UserProfile user = new UserProfile(username, false);
                                                    ClientWelcome clientWelcome = new ClientWelcome(username, socket, textFieldIPAddress, textFieldPort, user);
                                                    frame.setVisible(false);
                                                    // reg name to connection

                                                    try {
                                                        sendMsg4("system", user.getUserName(), "regUserName", Boolean.toString(user.isManager()));
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    } catch (ParseException e) {
                                                        e.printStackTrace();
                                                    }
                                                    break;
                                                } else if (status.equals(3L)) {
                                                    JOptionPane.showMessageDialog(null, "Oops, wrong password. Or username already been registered.");
                                                    // todo delete connection
//                                    sendMsg("system", username, "exit");
//                                                    System.exit(1);
                                                    break;
                                                } else {
                                                    JOptionPane.showMessageDialog(null, "Unknown Error, please contact manager");
                                                    // todo delete connection
//                                    sendMsg("system", username, "exit");
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


                            } catch (UnknownHostException x) {
                                x.printStackTrace();

                            } catch (IOException y) {
                                y.printStackTrace();
                            }
                        }
                    } catch (IOException e2) {
                        JOptionPane.showMessageDialog(null, "Fail: server not found.");
//                        System.exit(1);
                        e2.printStackTrace();
                    }


                }

            }
        });


        btnLogin.setBounds(148, 286, 117, 29);
        frame.getContentPane().add(btnLogin);

        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(new Font("Georgia", Font.PLAIN, 20));
        lblUsername.setBounds(74, 88, 149, 22);
        frame.getContentPane().add(lblUsername);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Georgia", Font.PLAIN, 20));
        lblPassword.setBounds(81, 135, 111, 22);
        frame.getContentPane().add(lblPassword);

        JLabel lblCanvas = new JLabel("Whiteboard Login");
        lblCanvas.setFont(new Font("Georgia", Font.PLAIN, 20));
        lblCanvas.setBounds(133, 24, 168, 26);
        frame.getContentPane().add(lblCanvas);

        JLabel lblPort = new JLabel("Port Number:");
        lblPort.setFont(new Font("Georgia", Font.PLAIN, 20));
        lblPort.setBounds(49, 230, 138, 22);
        frame.getContentPane().add(lblPort);

        JLabel lblIPAdd = new JLabel("IP Address:");
        lblIPAdd.setFont(new Font("Georgia", Font.PLAIN, 20));
        lblIPAdd.setBounds(69, 186, 117, 22);
        frame.getContentPane().add(lblIPAdd);

    }

    private void sendMsg(String method, String userName, String message)
            throws IOException, ParseException {
        // Output and Input Stream
        try {
            JSONObject jsonWord = new JSONObject();
            jsonWord.put("method_name", method);
            jsonWord.put("user_name", userName);
            jsonWord.put("txt_message", message);

            // Send message to Server
            dataOutputStream.writeUTF(jsonWord.toJSONString());
            dataOutputStream.flush();
        } catch (SocketException e) {
            // todo tanchuang
            e.printStackTrace();
            System.out.println("hahaha");
        }
    }

    private void sendMsg4(String method, String userName, String message, String other)
            throws IOException, ParseException {
        // Output and Input Stream
        try {
            JSONObject jsonWord = new JSONObject();
            jsonWord.put("method_name", method);
            jsonWord.put("user_name", userName);
            jsonWord.put("txt_message", message);
            jsonWord.put("other", other);

            // Send message to Server
            dataOutputStream.writeUTF(jsonWord.toJSONString());
            dataOutputStream.flush();
        } catch (SocketException e) {
            // todo tanchuang
            e.printStackTrace();
            System.out.println("hahaha");
        }
    }
}
