package Server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Description
 *
 * @author YUHAO SONG
 * Student_id  981738
 * Date        2019-09-05
 * @version 1.5
 */
public class MessageHandler implements Runnable {
    private Socket socket;   // A connected socket
    private DataInputStream inputFromClient;
    private DataOutputStream outputToClient;

    public MessageHandler(Socket socket) {
        this.socket = socket;
    }


    /**
     * Parse the Object and do the corresponding task depends on the method
     *
     * @param jsonObject jsonObject from client
     * @throws IOException IO
     */
    private void parseAndReplyJSONObject(JSONObject jsonObject) throws IOException {
        String method = ((String) jsonObject.get("method_name")).trim().toLowerCase();
        String userName = ((String) jsonObject.get("user_name")).trim().toLowerCase();
        String message = ((String) jsonObject.get("chat_message")).trim().toLowerCase();

        sendBackToClient(method, userName, message);
    }

    /**
     *
     */
    @Override
    public void run() {
        try {
            // Create data input and output streams
            JSONParser jsonParser = new JSONParser();
            inputFromClient = new DataInputStream(socket.getInputStream());
            outputToClient = new DataOutputStream(socket.getOutputStream());

            // Continuously serve the client
            while (true) {
                if (inputFromClient.available() > 0) {
                    JSONObject jsonObject = (JSONObject) jsonParser.parse(inputFromClient.readUTF());
                    parseAndReplyJSONObject(jsonObject);
                }
            }
        } catch (IOException | ParseException ex) {
            ex.printStackTrace();
        } finally {
            try {
                inputFromClient.close();
                outputToClient.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Create the JSON Object and send back to the client
    private void sendBackToClient(String method, String userName, String message) throws IOException {
        JSONObject jsonMsg = new JSONObject();
        jsonMsg.put("method_name", method);
        jsonMsg.put("user_name", userName);
        jsonMsg.put("chat_message", message);

        outputToClient.writeUTF(jsonMsg.toJSONString());
        outputToClient.flush();
    }
}
