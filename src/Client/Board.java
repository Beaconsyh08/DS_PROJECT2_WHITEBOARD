package Client;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class Board extends JFrame {

    public JPanel left;
    public Color color = Color.black;
    public ArrayList<Shape> shapes = new ArrayList<Shape>();
    public File file_opened = null;
    public Socket socket;
    public Graphics2D g;
    public JPanel panel_darw;
    private int portNumber;
    private DataInputStream inputFromServer;
    private DataOutputStream outputToServer;
    private JTextArea messageArea;
    private JTextArea chatWindowArea;
    private UserProfile user;
    private ConnectionToServer server;
    private LinkedBlockingQueue<JSONObject> chatMsg;
    private LinkedBlockingQueue<JSONObject> systemMsg;
    private LinkedBlockingQueue<JSONObject> drawMsg;
    private LinkedBlockingQueue<JSONObject> initializeMsg;
    private ArrayList<String> userList;
    private DefaultListModel<String> userListModel = new DefaultListModel<String>();
    private JList<String> UserList = new JList<String>();
    private JLabel lblDrawing;
    private LocalDateTime time1 = LocalDateTime.now();
    private LocalDateTime time2 = LocalDateTime.now();
    private Thread drawHandling;


    //initialize
    public Board(UserProfile user, JTextField textFieldIPAddress, JTextField textFieldPort, JTextArea txtSystemMessage, Socket socket) {

        this.socket = socket;
        this.user = user;
        this.setSize(1019, 624);
        this.setTitle("Draw Board");
        this.setDefaultCloseOperation(3);
        this.setLocationRelativeTo(null);

        // set not resizable
        this.setResizable(false);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                if(user.isManager()){
                    JOptionPane.showMessageDialog(null,
                            "You are the manager, the board will be closed completely.");
                }
                try {
                    sendMsg("system", user.getUserName(), "exit");
                    if (user.isManager()) {
                        for (String user : userList) {
                            // todo trytry
                            sendKick("finish", user);
                        }
                    }
                    System.exit(0);
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            setPort(textFieldIPAddress, textFieldPort);
        } catch (InvalidPortNumberException | UnknownHostException ex) {
            txtSystemMessage.setText(ex.getMessage());
            txtSystemMessage.setForeground(Color.RED);
        }


        //main panel
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        getContentPane().add(panel);


        //paint panel
        panel_darw = new JPanel() {
            //repaint
            public void paint(Graphics g1) {
                super.paint(g1);
                g = (Graphics2D) g1;
                for (int i = 0; i < shapes.size(); i++) {
                    Shape shape = (Shape) shapes.get(i);

                    g.setColor(shape.color);
                    g.setStroke(new BasicStroke(shape.stroke));
                    int x1 = shape.x1;
                    int x2 = shape.x2;
                    int y1 = shape.y1;
                    int y2 = shape.y2;
                    if (shape.type.equals("Line")) {
                        g.drawLine(shape.x1, shape.y1, shape.x2, shape.y2);
                    } else if (shape.type.equals("Rectangle")) {
                        g.drawRect(Math.min(x2, x1), Math.min(y2, y1), Math.abs(x2 - x1), Math.abs(y1 - y2));
                    } else if (shape.type.equals("Oval")) {
                        g.drawOval(Math.min(x2, x1), Math.min(y2, y1), Math.abs(x2 - x1), Math.abs(y1 - y2));
                    } else if (shape.type.equals("Circle")) {
                        int r = (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                        g.drawOval(x1, y1, r, r);
                    } else if (shape.type.equals("Text")) {
                        g.drawString(shape.text, x1, y1);
                    }
                }
            }
        };
        panel_darw.setBorder(new LineBorder(new Color(0, 0, 0)));
        panel_darw.setBackground(Color.white);
        panel.add(panel_darw);

        //tool panel
        JPanel panelleft = new JPanel();
        panelleft.setBorder(new LineBorder(new Color(0, 0, 0)));
        panelleft.setPreferredSize(new Dimension(100, 0));
        panelleft.setBackground(Color.LIGHT_GRAY);
        panel.add(panelleft, BorderLayout.WEST);
        panelleft.setLayout(null);

        JPanel buttongroup1Panel = new JPanel();
        buttongroup1Panel.setBackground(Color.LIGHT_GRAY);
        buttongroup1Panel.setBounds(1, 16, 98, 231);
        buttongroup1Panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Tools"));
        panelleft.add(buttongroup1Panel);
        buttongroup1Panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 5));


        JPanel buttongroup2Panel = new JPanel();
        buttongroup2Panel.setBackground(Color.LIGHT_GRAY);
        buttongroup2Panel.setBounds(1, 259, 98, 116);
        buttongroup2Panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Size"));
        panelleft.add(buttongroup2Panel);
        buttongroup2Panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 5));

        // buttons
        ButtonGroup buttongroup = new ButtonGroup();
        String[] tools = {"FreeDraw", "Text", "Line", "Circle", "Rectangle", "Oval", "Erase"};
        for (String t : tools) {
            JRadioButton button = new JRadioButton(t);
            buttongroup.add(button);
            buttongroup1Panel.add(button);
            button.setActionCommand(t);
            if (t.equals("FreeDraw")) {
                button.setSelected(true);
            }
        }

        ButtonGroup buttongroup2 = new ButtonGroup();
        String[] strike = {"Slim", "Medium", "Bold"};
        for (String t : strike) {
            JRadioButton button = new JRadioButton(t);
            buttongroup2.add(button);
            buttongroup2Panel.add(button);
            button.setActionCommand(t);
            if (t.equals("Slim")) {
                button.setSelected(true);
            }
        }


        //chat panel
        JPanel panelright = new JPanel();
        panelright.setBorder(new LineBorder(new Color(0, 0, 0)));
        panelright.setPreferredSize(new Dimension(300, 0));
        panelright.setBackground(Color.LIGHT_GRAY);
        panel.add(panelright, BorderLayout.EAST);
        panelright.setLayout(null);

        JLabel lblChat = new JLabel("Chat");
        lblChat.setFont(new Font("Georgia", Font.PLAIN, 16));
        lblChat.setBounds(133, 6, 46, 19);
        panelright.add(lblChat);

        JScrollPane chatWindowPane = new JScrollPane();
        chatWindowArea = new JTextArea();
        chatWindowPane.setBounds(6, 27, 198, 350);
        chatWindowArea.setLineWrap(true);
        panelright.add(chatWindowPane);
        chatWindowPane.setViewportView(chatWindowArea);

        messageArea = new JTextArea();
        messageArea.setBounds(6, 389, 198, 80);
        messageArea.setLineWrap(true);
        panelright.add(messageArea);

        JButton btnEnter = new JButton("Enter");
        btnEnter.setFont(new Font("Georgia", Font.PLAIN, 16));
        btnEnter.setBounds(50, 485, 110, 25);
        panelright.add(btnEnter);
        btnEnter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // todo check empty
                String chatMessage = messageArea.getText().trim();
                if (chatMessage.equals("") || chatMessage.equals("")) {
                    messageArea.setText("Please enter word!");
                    messageArea.setForeground(Color.RED);
                } else {
                    try {
                        sendMsg("message", user.getUserName(), chatMessage);
                    } catch (IOException | ParseException ex) {
                        ex.printStackTrace();
                    }
                }
                messageArea.setText("");
            }
        });

        JScrollPane scrollUserList = new JScrollPane();
        scrollUserList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollUserList.setBounds(210, 27, 84, 442);
        panelright.add(scrollUserList);


        UserList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UserList.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
//        for(int i = 0; i < userList.size(); i ++) {
//            userListModel.addElement(userList.get(i));
//        }
//        UserList.setModel(userListModel);
        scrollUserList.setViewportView(UserList);

        JButton btnKickOut = new JButton("Kick");
        btnKickOut.setFont(new Font("Georgia", Font.PLAIN, 16));
        btnKickOut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (UserList.isSelectionEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please Select a User!");
                } else {
                    String kickoutUser = UserList.getSelectedValue();
                    System.out.println(kickoutUser);
                    if (kickoutUser.equals(user.getUserName())) {
                        JOptionPane.showMessageDialog(null, "You cannot kick out the manager!");
                    } else {
                        try {
                            sendKick("kick", kickoutUser);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        btnKickOut.setBounds(212, 485, 80, 25);
        panelright.add(btnKickOut);


        //down panel
        JPanel paneldown = new JPanel();
        paneldown.setBorder(new LineBorder(new Color(0, 0, 0)));
        paneldown.setPreferredSize(new Dimension(0, 60));
        paneldown.setLayout(null);
        paneldown.setBackground(Color.LIGHT_GRAY);
        panel.add(paneldown, BorderLayout.SOUTH);

        //color panel
        JPanel panel_color = new JPanel();
        panel_color.setBackground(Color.cyan);
        panel_color.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel_color.setBounds(10, 10, 200, 40);
        paneldown.add(panel_color);
        BevelBorder bb = new BevelBorder(0, Color.gray, Color.white);
        BevelBorder bb1 = new BevelBorder(1, Color.gray, Color.white);
        //color display
        left = new JPanel();
        left.setBackground(color);
        left.setLayout(null);
        left.setBorder(bb);
        left.setPreferredSize(new Dimension(40, 40));
        //color selection
        JPanel right = new JPanel();
        right.setBackground(Color.LIGHT_GRAY);
        right.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        right.setPreferredSize(new Dimension(160, 40));
        ColorListener bl = new ColorListener(this);
        Color[] colors = {Color.black, Color.blue, Color.cyan, Color.gray,
                Color.green, Color.magenta, Color.orange, Color.pink, Color.red,
                Color.yellow, Color.darkGray, Color.white, Color.lightGray,
                new Color(89, 89, 94), new Color(1, 3, 14)
                , new Color(9, 83, 94)};
        for (Color c : colors) {
            JButton bt3 = new JButton();
            bt3.setOpaque(true);
            bt3.setBackground(c);
            bt3.setPreferredSize(new Dimension(20, 20));
            bt3.setBorder(bb);
            bt3.addActionListener(bl);
            right.add(bt3);
        }
        panel_color.add(left);
        panel_color.add(right);

        //open
        JButton btnNewButton = new JButton("Open");
        btnNewButton.setFont(new Font("Georgia", Font.PLAIN, 16));
        btnNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ArrayList<Shape> shapes1;
                panel_darw.repaint();
                try {
                    JFileChooser chooser = new JFileChooser();
                    int result = chooser.showOpenDialog(null);
                    if (result == chooser.APPROVE_OPTION) {
                        File file = chooser.getSelectedFile();
                        if (file == null) {
                            JOptionPane.showMessageDialog(null, "no file selected");
                        } else {
                            try {
                                sendMsg("system", user.getUserName(), "openCanvas");
                                sendMsg("message", "Manager-" + user.getUserName(), "Previous Canvas Loaded");
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            } catch (ParseException ex) {
                                ex.printStackTrace();
                            }
//                            removeAllShapes(shapes);
                            file_opened = file;
                            FileInputStream fis = new FileInputStream(file);
                            ObjectInputStream ois = new ObjectInputStream(fis);
                            shapes1 = (ArrayList<Shape>) ois.readObject();
                            for (int n = 0; n < shapes1.size(); n++) {
                                addToShapes(shapes, shapes1.get(n));
                                JSONObject jsondraw = shapes1.get(n).toJSON(user.getUserName());
                                try {
                                    sendCDrawMsg(jsondraw);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                } catch (ParseException ex) {
                                    ex.printStackTrace();
                                }
                            }
                            System.out.println(shapes.size());
//                            panel_darw.repaint();
                            ois.close();
                        }
                    }

                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        btnNewButton.setBounds(700, 5, 110, 25);
        paneldown.add(btnNewButton);

        //save as
        JButton btnNewButton_1 = new JButton("Save as");
        btnNewButton_1.setFont(new Font("Georgia", Font.PLAIN, 16));

        btnNewButton_1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(shapes.size());
                JFileChooser chooser = new JFileChooser();
                chooser.showSaveDialog(null);
                if (chooser.getSelectedFile() != null) {
                    File file = chooser.getSelectedFile();
                    try {
                        FileOutputStream fis = new FileOutputStream(file);
                        ObjectOutputStream oos = new ObjectOutputStream(fis);
                        System.out.println(shapes.get(0) instanceof Shape);
                        System.out.println(shapes.size());
                        oos.writeObject(shapes);
                        JOptionPane.showMessageDialog(null, "save succeed");
                        oos.close();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        btnNewButton_1.setBounds(875, 5, 110, 25);
        paneldown.add(btnNewButton_1);

        //save
        JButton btnNewButton_2 = new JButton("Save");
        btnNewButton_2.setFont(new Font("Georgia", Font.PLAIN, 16));
        btnNewButton_2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = null;
                if (file_opened == null) {
                    JFileChooser chooser = new JFileChooser();
                    chooser.showSaveDialog(null);
                    file = chooser.getSelectedFile();
                } else {
                    file = file_opened;
                }
                if (file != null) {
                    try {
                        file_opened = file;
                        FileOutputStream fis = new FileOutputStream(file);
                        ObjectOutputStream oos = new ObjectOutputStream(fis);
                        System.out.println(shapes.get(0) instanceof Shape);
                        oos.writeObject(shapes);
                        System.out.println(shapes.size());
                        JOptionPane.showMessageDialog(null, "save succeed");
                        oos.close();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        btnNewButton_2.setBounds(700, 32, 110, 25);
        paneldown.add(btnNewButton_2);

        //new
        JButton btnNewButton_3 = new JButton("New");
        btnNewButton_3.setFont(new Font("Georgia", Font.PLAIN, 16));
        btnNewButton_3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                removeAllShapes(shapes);
//                panel_darw.repaint();
//                file_opened = null;
                try {
                    sendMsg("system", user.getUserName(), "newCanvas");
                    sendMsg("message", "Manager-" + user.getUserName(), "New Canvas Created");
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            }
        });
        btnNewButton_3.setBounds(875, 32, 110, 25);
        paneldown.add(btnNewButton_3);

        lblDrawing = new JLabel("Welcome to Shared Whiteboard");
        lblDrawing.setFont(new Font("Georgia", Font.PLAIN, 16));
        lblDrawing.setBounds(370, 22, 266, 15);
        paneldown.add(lblDrawing);

        if (!user.isManager()) {
            btnKickOut.setEnabled(false);
            btnNewButton.setEnabled(false);
            btnNewButton_1.setEnabled(false);
            btnNewButton_2.setEnabled(false);
            btnNewButton_3.setEnabled(false);
        }

        //final
        this.setVisible(true);
        Graphics g = panel_darw.getGraphics();
        DrawListener drawlistener = new DrawListener(g, buttongroup, buttongroup2, this, shapes, user.getUserName(), outputToServer);

        panel_darw.addMouseListener(drawlistener);
        panel_darw.addMouseMotionListener(drawlistener);
    }

//    main
//    public static void main(String[] args) {
//        EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                try {
//                    Board frame = new Board();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    private void clientInitialize(String ipAddress, int portNumber, Socket socket) {

        try {
//            socket = new Socket(ipAddress, portNumber);
            chatMsg = new LinkedBlockingQueue<JSONObject>();
            systemMsg = new LinkedBlockingQueue<JSONObject>();
            drawMsg = new LinkedBlockingQueue<JSONObject>();
            initializeMsg = new LinkedBlockingQueue<JSONObject>();
            server = new ConnectionToServer(socket);
            inputFromServer = new DataInputStream(socket.getInputStream());
            outputToServer = new DataOutputStream(socket.getOutputStream());

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
        }

//        // reg name to connection
//        try {
//            sendMsg4("system", user.getUserName(), "regUserName", Boolean.toString(user.isManager()));
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }


        try {
            sendMsg("system", user.getUserName(), "fullUserList");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // get full canvas
        if (!user.isManager()) {
            try {
                sendMsg("initialize", user.getUserName(), "fullCanvas");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Thread initializeHandling = new Thread() {
            public void run() {
                while (true) {
                    try {
                        JSONObject message = (JSONObject) initializeMsg.take();
                        String command = ((String) message.get("txt_message")).trim();

                        switch (command) {
                            case "add_to_shapes":
                                JSONObject jsonDraw = (JSONObject) message.get("shape");

                                String type = ((String) jsonDraw.get("type")).trim();
                                String text = ((String) jsonDraw.get("text")).trim();
                                int x1 = Integer.parseInt((String) jsonDraw.get("x1"));
                                int x2 = Integer.parseInt((String) jsonDraw.get("x2"));
                                int y1 = Integer.parseInt((String) jsonDraw.get("y1"));
                                int y2 = Integer.parseInt((String) jsonDraw.get("y2"));
//                                Stroke stroke = new BasicStroke(Integer.parseInt((String) jsonDraw.get("stroke")));
                                int stroke = Integer.parseInt(((String) jsonDraw.get("stroke")).trim());
                                Color color = new Color(Integer.parseInt(((String) jsonDraw.get("color")).trim()));

                                switch (type) {
                                    case "Line":
                                        Shape shape = new Shape("Line", x1, y1, x2, y2, color, stroke, "");
                                        addToShapes(shapes, shape);
                                        break;
                                    case "Rectangle":
                                        Shape shape2 = new Shape("Rectangle", x1, y1, x2, y2, color, stroke, "");
                                        addToShapes(shapes, shape2);
                                        break;
                                    case "Oval":
                                        Shape shape3 = new Shape("Oval", x1, y1, x2, y2, color, stroke, "");
                                        addToShapes(shapes, shape3);
                                        break;
                                    case "Circle":
                                        Shape shape4 = new Shape("Circle", x1, y1, x2, y2, color, stroke, "");
                                        addToShapes(shapes, shape4);
                                        break;
                                    case "Text":
                                        Shape shape5 = new Shape("Text", x1, y1, x2, y2, color, stroke, text);
                                        addToShapes(shapes, shape5);
                                        break;
                                }
                                break;

                            case "askToJoin":
                                JSONObject jsonJoin = (JSONObject) message.get("shape");
                                String joiner = ((String) message.get("user_name")).trim();
                                if (JOptionPane.showConfirmDialog(null, joiner + " want to join the board", "WARNING",
                                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
//                                        sendMsg("replyJoin", user.getUserName(), "true");
                                    sendMsg4("initialize", user.getUserName(), "replyJoin", "true");
                                    // yes option
                                } else {
                                    // no option
//                                    sendMsg("replyJoin", user.getUserName(), "false");
                                    sendMsg4("initialize", user.getUserName(), "replyJoin", "false");
                                }
                                break;
                        }

                    } catch (InterruptedException e) {
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        initializeHandling.setDaemon(true);
        initializeHandling.start();


        Thread systemHandling = new Thread() {
            public void run() {
                while (true) {
                    try {
                        JSONObject message = (JSONObject) systemMsg.take();
                        String command = ((String) message.get("txt_message")).trim();

                        switch (command) {
                            case "bye":
                                JOptionPane.showMessageDialog(null, "You have been kicked out by the manager!");
                                System.exit(0);
                                break;

                            case "finish":
                                JOptionPane.showMessageDialog(null, "The manager has shut down the whiteboard!");
                                System.exit(0);
                                break;

                            case "update_user_list":
                                userList = ((ArrayList<String>) message.get("userList"));
                                System.out.println("updated userlist " + userList);
                                userListModel.removeAllElements();
                                for (int i = 0; i < userList.size(); i++) {
                                    userListModel.addElement(userList.get(i));
                                }
                                UserList.setModel(userListModel);
                                break;

                            case "newCanvas":
                                removeAllShapes(shapes);
                                panel_darw.repaint();
                                file_opened = null;
                                break;

                            case "openCanvas":
                                drawHandling.wait();
                                removeAllShapes(shapes);
                                panel_darw.repaint();
                                drawHandling.notify();
                                break;

                            case "serverDown":
                                // todo tanchuang tishi server down
                                JOptionPane.showMessageDialog(null, "Sever is down!");
                                System.out.println("hahaha");
                                System.exit(0);
                                break;
                        }
                    } catch (InterruptedException e) {
                    }
                }
            }
        };
        systemHandling.setDaemon(true);
        systemHandling.start();

        Thread messageHandling = new Thread() {
            public void run() {
                while (true) {
                    try {
                        JSONObject message = (JSONObject) chatMsg.take();
                        readAndAppendChatMsg(message, chatWindowArea);
                    } catch (InterruptedException | IOException | ParseException e) {
                    }
                }
            }
        };
        messageHandling.setDaemon(true);
        messageHandling.start();

        drawHandling = new Thread() {
            public void run() {

                while (true) {
                    if (drawMsg.size() > 0) {
                        try {
                            JSONObject message = (JSONObject) drawMsg.take();
                            String drawerName = (String) message.get("user_name");
                            // todo problem?
                            try {
                                lblDrawing.setText(drawerName + " is drawing...");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            Graphics gd = panel_darw.getGraphics();
                            parseAndDraw(message, gd);
                            time1 = LocalDateTime.now();
                        } catch (InterruptedException e) {
                        }
                    } else if (drawMsg.size() == 0) {
                        time2 = LocalDateTime.now();
//                        LocalDateTime time1 = null;
                        long elapsedMinutes = Duration.between(time1, time2).toMillis();
//                        System.out.println(elapsedMinutes);
                        if (elapsedMinutes > 500) {
                            lblDrawing.setText("Welcome to Shared Whiteboard");
                        }
                    }
                }
            }
        };
        drawHandling.setDaemon(true);
        drawHandling.start();
    }

    // Set the port and initialize the client, almost same as the one in DictServer Class
    public void setPort(JTextField textFieldIPAddress, JTextField textFieldPort)
            throws InvalidPortNumberException, UnknownHostException {
        String portStr = textFieldPort.getText();
        String ipAddress = textFieldIPAddress.getText();
        if (portStr.equals("") || (portStr.equals("Enter Port"))) {
            portNumber = 2019;
            clientInitialize(ipAddress, portNumber, socket);
        } else {
            portNumber = Integer.parseInt(portStr);
            if ((portNumber < 1025) || (portNumber > 65536)) {
                textFieldPort.setText("Enter Port");
                textFieldPort.setForeground(Color.GRAY);
                throw new InvalidPortNumberException();
            } else {
                clientInitialize(ipAddress, portNumber, socket);
            }
        }
    }

    // Send the JSON object to the server and receive the feedback
    private void sendMsg(String method, String userName, String message)
            throws IOException, ParseException {
        // Output and Input Stream

        try {
            JSONObject jsonWord = new JSONObject();
            jsonWord.put("method_name", method);
            jsonWord.put("user_name", userName);
            jsonWord.put("txt_message", message);
            System.out.println("sent json" + jsonWord);
            // Send message to Server
            outputToServer.writeUTF(jsonWord.toJSONString());
            outputToServer.flush();
        } catch (SocketException e) {
            // todo tanchuang>>>???
            e.printStackTrace();
            System.out.println("hahaha");
            JOptionPane.showMessageDialog(null, "The server is not active now");
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
            JOptionPane.showMessageDialog(null, "The server is not active now");
        }
    }

    private void sendKick(String command, String kickoutUser) throws IOException {
        try {
            JSONObject kickJSON = new JSONObject();
            kickJSON.put("method_name", "system");
            kickJSON.put("user_name", user.getUserName());
            kickJSON.put("txt_message", command);
            kickJSON.put("kicked_user", kickoutUser);
            outputToServer.writeUTF(kickJSON.toJSONString());
            outputToServer.flush();
        } catch (SocketException e) {
            // todo tanchuang
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "The server is not active now");
            System.out.println("hahaha");
        }
    }

    // todo could improve the format
    // Append the result from server to the Client UI to show for user at correct place
    private void readAndAppendChatMsg(JSONObject jsonObject, JTextArea chatWindowArea) throws IOException, ParseException {
        String method = ((String) jsonObject.get("method_name")).trim().toLowerCase();
        String senderName = ((String) jsonObject.get("user_name")).trim();
        String message = ((String) jsonObject.get("txt_message")).trim();
        chatWindowArea.append(senderName);
        chatWindowArea.append(": ");
        chatWindowArea.append(message);
        chatWindowArea.append("\n");
    }

    private void parseAndDraw(JSONObject jsonDraw, Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        String type = ((String) jsonDraw.get("type")).trim();
        String text = ((String) jsonDraw.get("text")).trim();
        int x1 = Integer.parseInt((String) jsonDraw.get("x1"));
        int x2 = Integer.parseInt((String) jsonDraw.get("x2"));
        int y1 = Integer.parseInt((String) jsonDraw.get("y1"));
        int y2 = Integer.parseInt((String) jsonDraw.get("y2"));
        Stroke stroke = new BasicStroke(Integer.parseInt((String) jsonDraw.get("stroke")));
        Color color = new Color(Integer.parseInt(((String) jsonDraw.get("color")).trim()));
        int strokeInt = Integer.parseInt(((String) jsonDraw.get("stroke")).trim());

        g2d.setColor(color);
        g2d.setStroke(stroke);

        switch (type) {
            case "Line":
                g2d.drawLine(x1, y1, x2, y2);
                Shape shape = new Shape("Line", x1, y1, x2, y2, color, strokeInt, "");
                addToShapes(shapes, shape);
                break;
            case "Rectangle":
                g2d.drawRect(Math.min(x2, x1), Math.min(y2, y1), Math.abs(x2 - x1), Math.abs(y1 - y2));
                Shape shape2 = new Shape("Rectangle", x1, y1, x2, y2, color, strokeInt, "");
                addToShapes(shapes, shape2);
                break;
            case "Oval":
                g2d.drawOval(Math.min(x2, x1), Math.min(y2, y1), Math.abs(x2 - x1), Math.abs(y1 - y2));
                Shape shape3 = new Shape("Oval", x1, y1, x2, y2, color, strokeInt, "");
                addToShapes(shapes, shape3);
                break;
            case "Circle":
                int r = (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                g2d.drawOval(x1, y1, r, r);
                Shape shape4 = new Shape("Circle", x1, y1, x2, y2, color, strokeInt, "");
                addToShapes(shapes, shape4);
                break;
            case "Text":
                g2d.drawString(text, x1, y1);
                Shape shape5 = new Shape("Text", x1, y1, x2, y2, color, strokeInt, text);
                addToShapes(shapes, shape5);
                break;
        }
    }

    private synchronized void addToShapes(ArrayList<Shape> shapes, Shape shape) {
        shapes.add(shape);
    }

    private synchronized void removeAllShapes(ArrayList<Shape> shapes) {
        shapes.removeAll(shapes);
    }

    private void sendCDrawMsg(JSONObject jsonDraw)
            throws IOException, ParseException {
        // Send message to Server
        System.out.println(jsonDraw.toJSONString());
        try {
            outputToServer.writeUTF(jsonDraw.toJSONString());
            outputToServer.flush();
        } catch (SocketException e) {
            // todo tanchuang
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "The server is not active now");
            System.out.println("hahaha");
        }

    }

    private class ConnectionToServer {
        private Socket socket;

        ConnectionToServer(Socket socket) throws IOException {
            this.socket = socket;
            JSONParser jsonParser = new JSONParser();

            Thread read = new Thread() {
                public void run() {
                    while (true) {
                        try {
                            if (inputFromServer.available() > 0) {
                                JSONObject jsonObject = (JSONObject) jsonParser.parse(inputFromServer.readUTF());
                                String method = ((String) jsonObject.get("method_name")).trim().toLowerCase();
                                System.out.println("raw json" + jsonObject);
                                switch (method) {
                                    case "message":
                                        chatMsg.put(jsonObject);
                                        break;
                                    case "system":
                                        systemMsg.put(jsonObject);
                                        break;
                                    case "draw":
                                        drawMsg.put(jsonObject);
                                        break;
                                    case "initialize":
                                        initializeMsg.put(jsonObject);
                                        break;
                                }
                            }
                        } catch (IOException | ParseException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            read.setDaemon(true);
            read.start();
        }
    }
}