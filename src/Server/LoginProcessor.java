package Server;

import org.json.simple.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;

public class LoginProcessor {

    public void checkLoginProcessor(JSONObject jsonObject, DBUtils dbUtils, Socket socket) throws SQLException {
        String username = (String) jsonObject.get("username");
        String password = (String) jsonObject.get("password");

        int logInStatus = 0;

        Connection connection = dbUtils.getConnection();

        //check existence
        String checkExistence = "SELECT * FROM user WHERE username" + " = " +  "'" + username + "'";
//        String checkExistence = "SELECT * FROM user";
        PreparedStatement ptmp = connection.prepareStatement(checkExistence);
        ResultSet rs1 = ptmp.executeQuery();
        System.out.println(checkExistence);
//        Statement statement1 = connection.createStatement();
//        ResultSet rs1 = statement1.executeQuery(checkExistence);

        int size = 0;
        if (rs1 != null) {
            rs1.last();
            size = rs1.getRow();
        }

        if (size == 0) {
            String createUser = "INSERT INTO user (userID, username, managerID, password) VALUE (NULL, '"
                    + username + "' ,NULL, '" + password + "')";
            Statement statement2 = connection.createStatement();
            statement2.execute(createUser);
            logInStatus = 1;
        } else {
            String passw = rs1.getString("password");
            if (passw.equals(password)) {
                logInStatus = 2;
            } else {
                logInStatus = 3;
            }

        }

        // login thread
        int finalLogInStatus = logInStatus;
        new Thread(() -> {
            try {
                while (true) {
                    JSONObject message = new JSONObject();
                    message.put("status", finalLogInStatus);
                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                    outputStream.writeUTF(message.toJSONString());
                    outputStream.flush();
                    System.out.println("message send: " + message);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

}
