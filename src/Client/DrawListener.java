package Client;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class DrawListener implements MouseListener, MouseMotionListener {

    public static final Stroke stroke1 = new BasicStroke(1);
    public static final Stroke stroke2 = new BasicStroke(5);
    public static final Stroke stroke3 = new BasicStroke(10);
    //variables
    public Graphics2D g;
    public Color color;
    public Board board;
    public ArrayList<Shape> shapes;
    public int x0, y0, x1, y1;
    public ButtonGroup bg, bg2;
    public String tool;
    public String stroke;
    public int strokeint;
    private DataOutputStream outputToServerDraw;
    private String userName;


    //initialize
    public DrawListener(Graphics g, ButtonGroup bg, ButtonGroup bg2, Board board1,
                        ArrayList shapes, String userName, DataOutputStream outputToServerDraw) {
        this.g = (Graphics2D) g;
        this.bg = bg;
        this.bg2 = bg2;
        this.board = board1;
        this.shapes = shapes;
        this.userName = userName;
        this.outputToServerDraw = outputToServerDraw;
    }

    //get initial initial (x,y), tool and color
    public void mousePressed(MouseEvent e) {
        x0 = e.getX();
        y0 = e.getY();
        ButtonModel bm = bg.getSelection();
        tool = bm.getActionCommand();
        ButtonModel bm2 = bg2.getSelection();
        stroke = bm2.getActionCommand();
        color = board.color;
        g.setColor(color);

        if ("Slim".equals(stroke)) {
            g.setStroke(stroke1);
            strokeint = 1;

        } else if (stroke.equals("Medium")) {
            g.setStroke(stroke2);
            strokeint = 5;
        } else if (stroke.equals("Bold")) {
            g.setStroke(stroke3);
            strokeint = 10;
        }
    }


    //drawing
    public void mouseReleased(MouseEvent e) {
        x1 = e.getX();
        y1 = e.getY();
        if ("Line".equals(tool))//line
        {
            g.drawLine(x0, y0, x1, y1);
            Shape shape = new Shape("Line", x0, y0, x1, y1, g.getColor(), strokeint, "");
            shapes.add(shape);
            JSONObject jsondraw = shape.toJSON(userName);
            try {
                sendCDrawMsg(jsondraw);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }

        } else if ("Rectangle".equals(tool)) {//rectangle
            g.drawRect(Math.min(x1, x0), Math.min(y1, y0), Math.abs(x1 - x0), Math.abs(y0 - y1));
            Shape shape = new Shape("Rectangle", x0, y0, x1, y1, g.getColor(), strokeint, "");
            shapes.add(shape);
            JSONObject jsondraw = shape.toJSON(userName);
            try {
                sendCDrawMsg(jsondraw);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        } else if ("Oval".equals(tool)) {//oval
            g.drawOval(Math.min(x1, x0), Math.min(y1, y0), Math.abs(x1 - x0), Math.abs(y0 - y1));
            Shape shape = new Shape("Oval", x0, y0, x1, y1, g.getColor(), strokeint, "");
            shapes.add(shape);
            JSONObject jsondraw = shape.toJSON(userName);
            try {
                sendCDrawMsg(jsondraw);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        } else if (tool.equals("Circle")) {//circle
            int r = (int) Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2));
            g.drawOval(x0, y0, r, r);
            Shape shape = new Shape("Circle", x0, y0, x1, y1, g.getColor(), strokeint, "");
            shapes.add(shape);
            JSONObject jsondraw = shape.toJSON(userName);
            try {
                sendCDrawMsg(jsondraw);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        } else if (tool.equals("Text")) {//text
            String text;
            text = JOptionPane.showInputDialog("Please enter your text:");
            if (text != null) {
                g.drawString(text, x0, y0);
                Shape shape = new Shape("Text", x0, y0, x1, y1, g.getColor(), strokeint, text);//
                shapes.add(shape);
                JSONObject jsondraw = shape.toJSON(userName);
                try {
                    sendCDrawMsg(jsondraw);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        //free draw
        if ("FreeDraw".equals(tool)) {
            g.drawLine(x0, y0, x, y);
            Shape shape = new Shape("Line", x0, y0, x, y, g.getColor(), strokeint, "");
            shapes.add(shape);
            JSONObject jsondraw = shape.toJSON(userName);
            try {
                sendCDrawMsg(jsondraw);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            x0 = x;
            y0 = y;
        }
        //eraser
        else if ("Erase".equals(tool)) {
            g.setColor(Color.white);
            g.drawLine(x0, y0, x, y);
            Shape shape = new Shape("Line", x0, y0, x, y, g.getColor(), strokeint, "");
            shapes.add(shape);
            JSONObject jsondraw = shape.toJSON(userName);
            try {
                sendCDrawMsg(jsondraw);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            x0 = x;
            y0 = y;
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    private void sendCDrawMsg(JSONObject jsonDraw)
            throws IOException, ParseException {
        // Send message to Server
        System.out.println(jsonDraw.toJSONString());
        this.outputToServerDraw.writeUTF(jsonDraw.toJSONString());
        this.outputToServerDraw.flush();
    }
}


