package Server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerTest {

    private static DBUtils dbUtils;
    private static int logInStatus;
    private JFrame frame;
    private JTextField txtIP;
    private JTextField txtPort;
    private int portNumber = 2019;
    private InetAddress ipAddress;
    private ServerSocket serverSocket;
    private String portIndicator = "Enter Port";
    private JTextArea txtMsg;
    private ArrayList<ConnectionToClient> clientList;
    private LinkedBlockingQueue<JSONObject> chatMsg;
    private LinkedBlockingQueue<JSONObject> systemMsg;
    private LinkedBlockingQueue<JSONObject> drawMsg;
    private ArrayList<JSONObject> canvasShapes;
    private ArrayList<String> userNameArray = new ArrayList<String>();
    private LinkedBlockingQueue<JSONObject> initializeMsg;

    /**
     * Create the application.
     */
    public ServerTest() throws UnknownHostException {
        initialize();
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    dbUtils = new DBUtils();
                    ServerTest window = new ServerTest();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Check the String input is number or not
    public static boolean isNumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 720, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.setTitle("Shared Whiteboard Server");

        JPanel panel = new JPanel();
        panel.setBorder(null);
        panel.setBounds(0, 0, 355, 459);
        frame.getContentPane().add(panel);
        panel.setLayout(null);

        JLabel lblIP = new JLabel("IP ADDRESS:");
        lblIP.setBounds(16, 58, 123, 24);
        lblIP.setFont(new Font("Georgia", Font.PLAIN, 18));
        panel.add(lblIP);

        txtIP = new JTextField();
        txtIP.setBounds(144, 53, 190, 34);
        txtIP.setText("127.0.0.1");
        txtIP.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtIP.getText().trim().equals("127.0.0.1")) {
                    txtIP.setText("");
                    txtIP.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtIP.getText().trim().equals("")) {
                    txtIP.setText("127.0.0.1");
                    txtIP.setForeground(Color.BLACK);
                }
            }
        });
        txtIP.setHorizontalAlignment(SwingConstants.CENTER);
        txtIP.setFont(new Font("Georgia", Font.PLAIN, 20));
        txtIP.setColumns(10);
        panel.add(txtIP);

        JLabel lblPort = new JLabel("PORT:");
        lblPort.setBounds(73, 113, 59, 24);
        lblPort.setFont(new Font("Georgia", Font.PLAIN, 18));
        panel.add(lblPort);

        txtPort = new JTextField();
        txtPort.setBounds(144, 108, 190, 34);
        txtPort.setText(portIndicator);
        txtPort.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtPort.getText().trim().equals(portIndicator)) {
                    txtPort.setText("");
                    txtPort.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtPort.getText().trim().equals("")) {
                    txtPort.setText(portIndicator);
                    txtPort.setForeground(Color.GRAY);
                }
            }
        });
        txtPort.setHorizontalAlignment(SwingConstants.CENTER);
        txtPort.setForeground(Color.GRAY);
        txtPort.setFont(new Font("Georgia", Font.PLAIN, 20));
        txtPort.setColumns(10);
        panel.add(txtPort);

        JButton btnStart = new JButton("START");
        btnStart.setToolTipText("Start the server");
        btnStart.addActionListener(e -> {
            try {
                setPort();
                txtMsg.setForeground(Color.BLACK);
            } catch (InvalidPortNumberException | IOException ex) {
                txtMsg.append(ex.getMessage());
                txtMsg.setForeground(Color.RED);
            }
        });
        btnStart.setFont(new Font("Georgia", Font.PLAIN, 20));
        btnStart.setBounds(109, 194, 137, 34);
        panel.add(btnStart);

        JScrollPane scrollMsg = new JScrollPane();
        scrollMsg.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollMsg.setBounds(16, 249, 318, 204);
        panel.add(scrollMsg);

        txtMsg = new JTextArea();
        txtMsg.setEditable(false);
        txtMsg.setFont(new Font("Georgia", Font.PLAIN, 20));
        scrollMsg.setViewportView(txtMsg);

        JScrollPane scrollBrdList = new JScrollPane();
        scrollBrdList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollBrdList.setBounds(371, 44, 329, 415);
        frame.getContentPane().add(scrollBrdList);

        JTextArea txtBoardList = new JTextArea();
        txtBoardList.setEditable(false);
        txtBoardList.setFont(new Font("Georgia", Font.PLAIN, 20));
        scrollBrdList.setViewportView(txtBoardList);

        JLabel lblRunningWhiteBoard = new JLabel("Running White Board");
        lblRunningWhiteBoard.setFont(new Font("Georgia", Font.PLAIN, 20));
        lblRunningWhiteBoard.setBounds(442, 12, 196, 24);
        frame.getContentPane().add(lblRunningWhiteBoard);


    }

    private void printInitialInfo() throws UnknownHostException {
        ipAddress = InetAddress.getLocalHost();
        txtMsg.append("Dictionary Server started at " + new Date() + '\n');
        txtMsg.append("Current IP Address : " + ipAddress.getHostAddress() + "\n");
        if (portNumber == 2019) {
            txtMsg.append("Using Default Port = " + portNumber + '\n');
        } else {
            txtMsg.append("Current Port = " + portNumber + "\n");
        }
    }

    /**
     * Set the port for the server, if not set, use default port - 2019
     * And initialize the server if everything setting up
     *
     * @throws InvalidPortNumberException port number is not in the reasonable range
     * @throws UnknownHostException       host unknown
     */
    private void setPort() throws InvalidPortNumberException, IOException {
        String portStr = txtPort.getText();
        if (portStr.equals("") || (portStr.equals(portIndicator))) {
            portNumber = 2019;
            serverInitialize(portNumber);
        } else {
            if (!isNumeric(portStr)) {
                txtPort.setText(portIndicator);
                txtPort.setForeground(Color.GRAY);
                throw new InvalidPortNumberException();
            } else {
                portNumber = Integer.parseInt(portStr);
                if ((portNumber < 1025) || (portNumber > 65536)) {
                    txtPort.setText(portIndicator);
                    txtPort.setForeground(Color.GRAY);
                    throw new InvalidPortNumberException();
                } else {
                    serverInitialize(portNumber);
                }
            }
        }
    }

    /**
     * @param PORT
     * @throws UnknownHostException
     */
    private void serverInitialize(int PORT) throws IOException {


        {
            printInitialInfo();
            serverSocket = new ServerSocket(PORT);
            clientList = new ArrayList<ConnectionToClient>();
            chatMsg = new LinkedBlockingQueue<JSONObject>();
            drawMsg = new LinkedBlockingQueue<JSONObject>();
            systemMsg = new LinkedBlockingQueue<JSONObject>();
            canvasShapes = new ArrayList<JSONObject>();
            initializeMsg = new LinkedBlockingQueue<JSONObject>();

            new Thread(() -> {
                try {
                    // Create a server socket
                    while (true) {
                        // Listen for a new connection request
                        Socket clientSocket = serverSocket.accept();
                        ConnectionToClient socketConnection = new ConnectionToClient(clientSocket);
                        clientList.add(socketConnection);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }).start();

            //todo 只给来的人发
            // login thread
//            new Thread(() -> {
//                try {
//                    while (true) {
//                        JSONObject message = new JSONObject();
//                        message.put("status", logInStatus);
//                        System.out.println("Message Received: " + message);
//
//                        for (ConnectionToClient clientConnection : clientList) {
//                            clientConnection.parseAndReplyOrigin(message);
//                            System.out.println(message);
//                        }
//
//                    }
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//            }).start();

            // chat message thread
            new Thread(() -> {
                try {
                    while (true) {
                        JSONObject message = chatMsg.take();
//                        System.out.println("Message Received: " + message);
                        // Do some handling here...
                        for (ConnectionToClient clientConnection : clientList) {
                            System.out.println("clientList chat" + clientList);
                            clientConnection.parseAndReplyOrigin(message);
                        }

                    }
                } catch (InterruptedException | IOException ex) {
                    ex.printStackTrace();
                }
            }).start();

            // draw thread
            new Thread(() -> {
                try {
                    while (true) {
                        JSONObject message = drawMsg.take();
//                        System.out.println("Message Received: " + message);

                        // todo it send to everybody inclued the drawer, maybe improve? or just leave it
                        for (ConnectionToClient clientConnection : clientList) {
                            System.out.println("clientList draw" + clientList);
                            clientConnection.parseAndReplyOrigin(message);
//                            System.out.println(message);
                        }

                    }
                } catch (InterruptedException | IOException ex) {
                    ex.printStackTrace();
                }
            }).start();

            // initialize thread
            new Thread(() -> {
                try {
                    while (true) {
                        JSONObject message = initializeMsg.take();
                        String txtMessage = ((String) message.get("txt_message")).trim();
                        switch (txtMessage) {
                            case "fullCanvas":
                                for (JSONObject canvasShape : canvasShapes) {
                                    JSONObject canvasShapeJSON = new JSONObject();
                                    canvasShapeJSON.put("method_name", "initialize");
                                    canvasShapeJSON.put("shape", canvasShape);
                                    canvasShapeJSON.put("txt_message", "add_to_shapes");
                                    for (ConnectionToClient clientConnection : clientList) {
                                        clientConnection.parseAndReplyOrigin(canvasShapeJSON);
                                    }
                                }
                                break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }).start();


            // system thread
            new Thread(() -> {
                try {
                    while (true) {
                        JSONObject message = systemMsg.take();
                        String txtMessage = ((String) message.get("txt_message")).trim();
                        switch (txtMessage) {
                            case "regUserName":
                                String userName = ((String) message.get("user_name")).trim();
                                System.out.println(userName);
                                userNameArray.add(userName);
                                System.out.println(userNameArray);
                                sendUpdateUserList();
                                break;

                            case "exit":
                                String userNameExit = ((String) message.get("user_name")).trim();
                                userNameArray.remove(userNameExit);
                                updateConnectionList(userNameExit);
                                sendUpdateUserList();
                                break;

                            case "kick":
                                String userNameKick = ((String) message.get("kicked_user")).trim();
                                // todo send kick out message to the person be kicked
                                if (!userNameKick.equals("")) {
                                    JSONObject kickJSON = new JSONObject();
                                    kickJSON.put("method_name", "system");
                                    kickJSON.put("txt_message", "bye");
                                    for (ConnectionToClient clientConnection : clientList) {
                                        if (clientConnection.getUserName().equals(userNameKick)) {
                                            clientConnection.parseAndReplyOrigin(kickJSON);
                                            break;
                                        }
                                    }
                                    userNameArray.remove(userNameKick);
                                    updateConnectionList(userNameKick);
                                    sendUpdateUserList();
                                }
                                break;

                            case "finish":
                                JSONObject finishJSON = new JSONObject();
                                finishJSON.put("method_name", "system");
                                finishJSON.put("txt_message", "finish");
                                for (ConnectionToClient clientConnection : clientList) {
                                    clientConnection.parseAndReplyOrigin(finishJSON);
                                    userNameArray.remove(clientConnection.getUserName());
                                    updateConnectionList(clientConnection.getUserName());
                                }
                                break;

                            case "newCanvas":
                                synchronized (canvasShapes) {
                                    canvasShapes.removeAll(canvasShapes);
                                }
                                for (ConnectionToClient clientConnection : clientList) {
                                    clientConnection.parseAndReplyOrigin(message);
                                }
                                break;

                            case "openCanvas":
                                synchronized (canvasShapes) {
                                    canvasShapes.removeAll(canvasShapes);
                                }
                                break;
                        }
                    }
                } catch (InterruptedException | IOException ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
    }

    private void sendUpdateUserList() throws IOException {
        JSONObject userListJSON = new JSONObject();
        userListJSON.put("method_name", "system");
        userListJSON.put("userList", userNameArray);
        userListJSON.put("txt_message", "update_user_list");
        for (ConnectionToClient clientConnection : clientList) {
            clientConnection.parseAndReplyOrigin(userListJSON);
        }
    }

    private void updateConnectionList(String userNameExit) {
        ArrayList<ConnectionToClient> newClientList = new ArrayList<ConnectionToClient>();
        for (ConnectionToClient clientConnection : clientList) {
            if (clientConnection.getUserName().equals(userNameExit)) {
                clientConnection.setAlive(false);
            }
            if (clientConnection.isAlive) {
                newClientList.add(clientConnection);
            }
        }
        clientList = newClientList;
        System.out.println("connection after delete" + newClientList);
    }

//    private synchronized void addToShapes(ArrayList<Shape> shapes, Shape shape) {
//        shapes.add(shape);
//    }


    private class ConnectionToClient {
        private Socket socket;
        private DataInputStream inputFromClient;
        private DataOutputStream outputToClient;
        private String userName;
        private boolean isAlive = true;

        ConnectionToClient(Socket socket) throws IOException {
            this.socket = socket;
            JSONParser jsonParser = new JSONParser();
            inputFromClient = new DataInputStream(socket.getInputStream());
            outputToClient = new DataOutputStream(socket.getOutputStream());

            Thread read = new Thread() {
                public void run() {
                    while (true) {
                        try {
                            if (inputFromClient.available() > 0) {
                                JSONObject jsonObject = (JSONObject) jsonParser.parse(inputFromClient.readUTF());
                                String method = ((String) jsonObject.get("method_name")).trim().toLowerCase();
                                System.out.println("raw json" + jsonObject);
                                switch (method) {
//                                    case "login":
//                                        LoginProcessor loginProcessor = new LoginProcessor();
//                                        logInStatus = loginProcessor.checkLoginProcessor(jsonObject, dbUtils);
//                                        System.out.println(jsonObject.toJSONString());
//                                        break;
                                    case "message":
                                        chatMsg.put(jsonObject);
                                        break;
                                    case "system":
                                        if (((String) jsonObject.get("txt_message")).trim().equals("regUserName")) {
                                            String userNameNew = ((String) jsonObject.get("user_name")).trim();
                                            setUserName(userNameNew);
                                        }
                                        systemMsg.put(jsonObject);
                                        break;
                                    case "draw":
                                        drawMsg.put(jsonObject);
                                        synchronized (canvasShapes) {
                                            canvasShapes.add(jsonObject);
                                        }
                                        break;
                                    case "initialize":
                                        initializeMsg.put(jsonObject);
                                        break;
                                }

                            }
                        } catch (IOException | InterruptedException | ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            read.setDaemon(true); // terminate when main ends
            read.start();
        }

        public boolean isAlive() {
            return isAlive;
        }

        public void setAlive(boolean alive) {
            isAlive = alive;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public void parseAndReplyOrigin(JSONObject jsonObject) throws IOException {
            outputToClient.writeUTF(jsonObject.toJSONString());
            outputToClient.flush();
        }

    }
}
