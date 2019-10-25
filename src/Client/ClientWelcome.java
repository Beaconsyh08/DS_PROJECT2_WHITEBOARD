package Client;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * Description
 *
 * @author YUHAO SONG
 * Student_id  981738
 * Date        2019-09-05
 * @version 1.5
 */
public class ClientWelcome {
    private String userName;
    private JFrame frmWelcomePage;
    //    private JTextField textFieldPort;
    private JLabel lblNewLabel;
    private JScrollPane scrollPane;
    private JTextArea txtSystemMessage;
    //    private JTextField textFieldIPAddress;
//    private JLabel lblUserName;
//    private JTextField txtUserName;
    private JButton btnCreate;
    //    private String userName;
    private DataOutputStream outputToServer;
    private DataInputStream inputFromServer;
    private JSONParser jsonParser = new JSONParser();
    private String isCreatedStr;
    private UserProfile user;
    private String answer;

    /**
     * Create the application.
     *
     * @wbp.parser.entryPoint
     */
    public ClientWelcome(String userName, Socket socket, JTextField ip, JTextField port, UserProfile user) {
        this.userName = userName;
        this.user = user;
        initialize(socket, ip, port);
        frmWelcomePage.setVisible(true);
    }

//    /**
//     * Launch the application.
//     */
//    public static void main(String[] args) {
//        EventQueue.invokeLater(() -> {
//            try {
//                ClientWelcome window = new ClientWelcome("hahaha");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize(Socket socket, JTextField ip, JTextField port) {
        frmWelcomePage = new JFrame();
        frmWelcomePage.setTitle("Shared Whiteboard");
        frmWelcomePage.getContentPane().setFont(new Font("Georgia", Font.PLAIN, 20));
        frmWelcomePage.setBounds(100, 100, 700, 500);
        frmWelcomePage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmWelcomePage.getContentPane().setLayout(null);
        frmWelcomePage.setResizable(false);

        frmWelcomePage.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                try {
                    sendMsg("system", user.getUserName(), "exit");
                    System.exit(0);
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        lblNewLabel = new JLabel("Welcome to the Shared Whiteboard");
        lblNewLabel.setForeground(new Color(0, 0, 102));
        lblNewLabel.setFont(new Font("Dialog", Font.PLAIN, 26));
        lblNewLabel.setBounds(115, 30, 469, 85);
        frmWelcomePage.getContentPane().add(lblNewLabel);

        scrollPane = new JScrollPane();
        scrollPane.setBounds(82, 130, 535, 96);
        frmWelcomePage.getContentPane().add(scrollPane);

        txtSystemMessage = new JTextArea();
        txtSystemMessage.setEditable(false);
        txtSystemMessage.setForeground(new Color(255, 94, 33));
        txtSystemMessage.setFont(new Font("Georgia", Font.PLAIN, 20));
        txtSystemMessage.setText("You could join after manager approval." + "\n" + "You could create a board if there is no board exist."
                + "\n" + "Don't join before board exist.");
        scrollPane.setViewportView(txtSystemMessage);

        // CONNECT BUTTON: connect to the server and go to next window if connected
        JButton btnJoin = new JButton("Join");
        btnJoin.setToolTipText("Join an existing white board");

        btnJoin.addActionListener(e -> {

            try {
                outputToServer = new DataOutputStream(socket.getOutputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                sendMsg("initialize", userName, "joinBoard");
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            try {
                inputFromServer = new DataInputStream(socket.getInputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            try {
                JSONObject jsonObject = (JSONObject) jsonParser.parse(inputFromServer.readUTF());
                System.out.println(jsonObject);
                String method_command = ((String) jsonObject.get("method_name")).trim();


                answer = ((String) jsonObject.get("txt_message")).trim();


                System.out.println(jsonObject);
            } catch (ParseException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (answer.equals("false")) {
                JOptionPane.showMessageDialog(null, "Board is not exists, please create or wait");
            } else {


//            userName = txtUserName.getText().trim();

                Board boardClient = new Board(user, ip, port, txtSystemMessage, socket);
                System.out.println("user_name:" + userName);

                try {
                    if (boardClient.socket.isConnected()) {
                        System.out.println("True");
                        boardClient.setVisible(true);
                        frmWelcomePage.setVisible(false);
                    }
                } catch (NullPointerException ex) {
                    txtSystemMessage.setText("Make sure server is on!" + "\n" + "Make sure correct port and IP Address are entered! ");
                    txtSystemMessage.setForeground(Color.RED);
                }
            }
        });
        btnJoin.setBackground(new Color(255, 242, 68));
        btnJoin.setFont(new Font("Georgia", Font.PLAIN, 20));
        btnJoin.setBounds(146, 393, 150, 50);
        frmWelcomePage.getContentPane().add(btnJoin);

        btnCreate = new JButton("Create");
        btnCreate.setToolTipText("Create a new white board");
        btnCreate.setFont(new Font("Georgia", Font.PLAIN, 20));
        btnCreate.setBackground(new Color(255, 41, 30));
        btnCreate.setBounds(409, 393, 150, 50);
        btnCreate.addActionListener(e -> {
//            userName = txtUserName.getText().trim();
            try {
                outputToServer = new DataOutputStream(socket.getOutputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                sendMsg("initialize", userName, "createBoard");
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }

            try {
                inputFromServer = new DataInputStream(socket.getInputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            try {
                JSONObject jsonObject = (JSONObject) jsonParser.parse(inputFromServer.readUTF());
                isCreatedStr = ((String) jsonObject.get("txt_message")).trim();
                System.out.println(jsonObject);
            } catch (ParseException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }


            System.out.println(isCreatedStr);
            if (isCreatedStr.equals("false")) {
                //todo here
                user.setManager(true);
                Board boardClient = new Board(user, ip, port, txtSystemMessage, socket);
                System.out.println("user_name:" + userName);

                try {
                    if (boardClient.socket.isConnected()) {
                        System.out.println("True");
                        boardClient.setVisible(true);
                        frmWelcomePage.setVisible(false);
                    }
                } catch (NullPointerException ex) {
                    txtSystemMessage.setText("Make sure server is on!" + "\n" + "Make sure correct port and IP Address are entered! ");
                    txtSystemMessage.setForeground(Color.RED);
                }
                // todo set manager
                try {
                    sendMsg("system", userName, "setManager");

                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }

            } else {
                JOptionPane.showMessageDialog(null, "Board already exists, please join it");
            }
        });
        frmWelcomePage.getContentPane().add(btnCreate);
    }

    // todo
    private boolean checkEmpty() {
        return true;
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
            outputToServer.writeUTF(jsonWord.toJSONString());
            outputToServer.flush();
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
            outputToServer.writeUTF(jsonWord.toJSONString());
            outputToServer.flush();
        } catch (SocketException e) {
            // todo tanchuang
            e.printStackTrace();
            System.out.println("hahaha");
        }
    }

}


