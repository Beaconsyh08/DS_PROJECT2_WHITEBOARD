package Client;

import java.awt.Color;
import java.awt.Stroke;
import java.io.Serializable;

public class Shape implements Serializable{
    public String type;
    public int x1, y1, x2, y2;
    public Color color;
    public int stroke;
    public String text;

    public Shape(String type, int x1, int y1, int x2, int y2,Color color, int stroke, String text){
        this.type=type;
        this.x1=x1;
        this.y1=y1;
        this.x2=x2;
        this.y2=y2;
        this.color=color;
        this.stroke=stroke;
        this.text=text;
    }
}
