package Client;

import org.json.simple.JSONObject;

import java.awt.*;
import java.io.Serializable;

public class Shape implements Serializable {
    public String type;
    public int x1, y1, x2, y2;
    public Color color;
    public int stroke;
    public String text;

    public Shape(String type, int x1, int y1, int x2, int y2, Color color, int stroke, String text) {
        this.type = type;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
        this.stroke = stroke;
        this.text = text;
    }


    public JSONObject toJSON(String userName) {
        JSONObject jsonDraw = new JSONObject();

        jsonDraw.put("method_name", "draw");
        jsonDraw.put("user_name", userName);
        jsonDraw.put("type", type);
        jsonDraw.put("x1", Integer.toString(x1));
        jsonDraw.put("y1", Integer.toString(y1));
        jsonDraw.put("x2", Integer.toString(x2));
        jsonDraw.put("y2", Integer.toString(y2));
        jsonDraw.put("color", Integer.toString(color.getRGB()));
        jsonDraw.put("stroke", Integer.toString(stroke));
        jsonDraw.put("text", text);

        return jsonDraw;
    }
}
