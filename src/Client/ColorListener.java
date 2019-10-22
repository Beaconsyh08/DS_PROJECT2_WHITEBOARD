package Client;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class ColorListener implements ActionListener{
    public Board board;
    public ColorListener(Board board1) {
        board=board1;
    }
    public void actionPerformed(ActionEvent e) {
        JButton button =(JButton)e.getSource();
        Color color= button.getBackground();
        board.color=color;
        board.left.setBackground(color);
    }
}