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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
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
    private LinkedBlockingQueue<Object> chatMsg;
    private LinkedBlockingQueue<Object> systemMsg;
    private LinkedBlockingQueue<Object> drawMsg;


    //initialize
    public Board(UserProfile user, JTextField textFieldIPAddress, JTextField textFieldPort, JTextArea txtSystemMessage) {

        this.user = user;
        this.setSize(1000, 600);
        this.setTitle("Draw Board");
        this.setDefaultCloseOperation(3);
        this.setLocationRelativeTo(null);

        // set not resizable
        this.setResizable(false);

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
        lblChat.setBounds(133, 6, 34, 16);
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
        btnEnter.setBounds(50, 481, 117, 29);
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
            }
        });

        JScrollPane scrollUserList = new JScrollPane();
        scrollUserList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollUserList.setBounds(210, 27, 84, 442);
        panelright.add(scrollUserList);

        JList<String> UserList = new JList<String>();
        UserList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UserList.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
        UserList.setModel(new AbstractListModel<String>() {

            String[] values = new String[]{"User1", "User2", "User3", "User4", "User5"};

            public int getSize() {
                return values.length;
            }

            public String getElementAt(int index) {
                return values[index];
            }
        });
        scrollUserList.setViewportView(UserList);

        JButton btnKickOut = new JButton("Kick Out");
        btnKickOut.setBounds(205, 481, 92, 29);
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
        btnNewButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ArrayList<Shape> shapes1;
                panel_darw.repaint();
                try {
                    JFileChooser chooser = new JFileChooser();
                    chooser.showOpenDialog(null);
                    File file = chooser.getSelectedFile();
                    if (file == null) {
                        JOptionPane.showMessageDialog(null, "no file selected");
                    } else {
                        shapes.removeAll(shapes);
                        file_opened = file;
                        FileInputStream fis = new FileInputStream(file);
                        ObjectInputStream ois = new ObjectInputStream(fis);
                        shapes1 = (ArrayList<Shape>) ois.readObject();
                        for (int n = 0; n < shapes1.size(); n++) {
                            shapes.add(shapes1.get(n));
                        }
                        System.out.println(shapes.size());
                        panel_darw.repaint();
                        ois.close();
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        btnNewButton.setBounds(700, 0, 117, 29);
        paneldown.add(btnNewButton);

        //save as
        JButton btnNewButton_1 = new JButton("Save as");

        btnNewButton_1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
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
        btnNewButton_1.setBounds(877, 0, 117, 29);
        paneldown.add(btnNewButton_1);

        //save
        JButton btnNewButton_2 = new JButton("Save");
        btnNewButton_2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
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
        btnNewButton_2.setBounds(700, 31, 117, 29);
        paneldown.add(btnNewButton_2);

        //new
        JButton btnNewButton_3 = new JButton("New");
        btnNewButton_3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                shapes.removeAll(shapes);
                panel_darw.repaint();
                file_opened = null;
            }
        });
        btnNewButton_3.setBounds(877, 31, 117, 29);
        paneldown.add(btnNewButton_3);

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

    private void clientInitialize(String ipAddress, int portNumber) {

        try {
            socket = new Socket(ipAddress, portNumber);
            chatMsg = new LinkedBlockingQueue<Object>();
            systemMsg = new LinkedBlockingQueue<Object>();
            drawMsg = new LinkedBlockingQueue<Object>();
            server = new ConnectionToServer(socket);
            inputFromServer = new DataInputStream(socket.getInputStream());
            outputToServer = new DataOutputStream(socket.getOutputStream());

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
        }

        Thread systemHandling = new Thread() {
            public void run() {
                // reg name to connection
                try {
                    sendMsg("system", user.getUserName(), "regUserName");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                while (true) {
                    try {
                        JSONObject message = (JSONObject) systemMsg.take();
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
                                int stroke = Integer.parseInt((String) jsonDraw.get("stroke"));
                                Color color = new Color(Integer.parseInt(((String) jsonDraw.get("color")).trim()));

                                switch (type) {
                                    case "Line":
                                        Shape shape = new Shape("Line", x1, y1, x2, y2, color, stroke, "");
                                        shapes.add(shape);
                                        break;
                                    case "Rectangle":
                                        Shape shape2 = new Shape("Rectangle", x1, y1, x2, y2, color, stroke, "");
                                        shapes.add(shape2);
                                        break;
                                    case "Oval":
                                        Shape shape3 = new Shape("Oval", x1, y1, x2, y2, color, stroke, "");
                                        shapes.add(shape3);
                                        break;
                                    case "Circle":
                                        Shape shape4 = new Shape("Circle", x1, y1, x2, y2, color, stroke, "");
                                        shapes.add(shape4);
                                        break;
                                    case "Text":
                                        Shape shape5 = new Shape("Text", x1, y1, x2, y2, color, stroke, "");
                                        shapes.add(shape5);
                                        break;
                                }
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

        Thread drawHandling = new Thread() {
            public void run() {

                // get full canvas
                if (!user.isManager()) {
                    try {
                        sendMsg("system", user.getUserName(), "fullCanvas");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                while (true) {
                    if (drawMsg.size() > 0) {
                        try {
                            JSONObject message = (JSONObject) drawMsg.take();
                            // todo add handling process
                            Graphics gd = panel_darw.getGraphics();
                            parseAndDraw(message, gd);
                        } catch (InterruptedException e) {
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
            clientInitialize(ipAddress, portNumber);
        } else {
            portNumber = Integer.parseInt(portStr);
            if ((portNumber < 1025) || (portNumber > 65536)) {
                textFieldPort.setText("Enter Port");
                textFieldPort.setForeground(Color.GRAY);
                throw new InvalidPortNumberException();
            } else {
                clientInitialize(ipAddress, portNumber);
            }
        }
    }

    // Send the JSON object to the server and receive the feedback
    private void sendMsg(String method, String userName, String message)
            throws IOException, ParseException {
        // Output and Input Stream
        JSONObject jsonWord = new JSONObject();
        jsonWord.put("method_name", method);
        jsonWord.put("user_name", userName);
        jsonWord.put("txt_message", message);

        // Send message to Server
        outputToServer.writeUTF(jsonWord.toJSONString());
        outputToServer.flush();
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
        int strokeInt = Integer.parseInt((String) jsonDraw.get("stroke"));

        g2d.setColor(color);
        g2d.setStroke(stroke);

        switch (type) {
            case "Line":
                g2d.drawLine(x1, y1, x2, y2);
                Shape shape = new Shape("Line", x1, y1, x2, y2, color, strokeInt, "");
                shapes.add(shape);
                break;
            case "Rectangle":
                g2d.drawRect(Math.min(x2, x1), Math.min(y2, y1), Math.abs(x2 - x1), Math.abs(y1 - y2));
                Shape shape2 = new Shape("Rectangle", x1, y1, x2, y2, color, strokeInt, "");
                shapes.add(shape2);
                break;
            case "Oval":
                g2d.drawOval(Math.min(x2, x1), Math.min(y2, y1), Math.abs(x2 - x1), Math.abs(y1 - y2));
                Shape shape3 = new Shape("Oval", x1, y1, x2, y2, color, strokeInt, "");
                shapes.add(shape3);
                break;
            case "Circle":
                int r = (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                g2d.drawOval(x1, y1, r, r);
                Shape shape4 = new Shape("Circle", x1, y1, x2, y2, color, strokeInt, "");
                shapes.add(shape4);
                break;
            case "Text":
                g2d.drawString(text, x1, y1);
                Shape shape5 = new Shape("Text", x1, y1, x2, y2, color, strokeInt, "");
                shapes.add(shape5);
                break;
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