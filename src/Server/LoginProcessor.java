package Server;

import Client.DigestUtil;
import Client.SaltUtil;
import org.json.simple.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoginProcessor {

    public int checkLoginProcessor(ArrayList<ServerTest.ConnectionToClient> clientList, JSONObject jsonObject, DBUtils dbUtils, Socket socket, List<String> existingUser) throws SQLException {
        String username = (String) jsonObject.get("username");
        String password = (String) jsonObject.get("password");
        String firstPwd = (String) jsonObject.get("firstPwd");
        String salt = (String) jsonObject.get("salt");
        Long times = (Long) jsonObject.get("times");

        int logInStatus = 0;

        Connection connection = dbUtils.getConnection();

        //check existence
        String checkExistence = "SELECT * FROM user WHERE username" + " = " + "'" + username + "'";
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
        logInStatus = 0;
        if (size == 0) {
            String createUser = "INSERT INTO user (userID, username, managerID, password, salt, times) VALUE (NULL, '"
                    + username + "' ,NULL, '" + password + "', '" + salt + "', '" + times  + "')";
            Statement statement2 = connection.createStatement();
            System.out.println(createUser);
            statement2.execute(createUser);
            logInStatus = 1;
        } else {
            String salt1 = rs1.getString("salt");
            Integer times1 = rs1.getInt("times");
//            String firstPwd = DigestUtil.digest(password, DigestUtil.SALT, DigestUtil.DIGEST_TIMES);
            String lastPwd = DigestUtil.digest(firstPwd, salt1, times1);

            String passw = rs1.getString("password");
            if (lastPwd.equals(passw)) {
                if (existingUser.contains(username)) {
                    logInStatus = 4;
                    clientList.remove(clientList.size()-1);
                } else {
                    logInStatus = 2;
                }
            } else {
                logInStatus = 3;
                clientList.remove(clientList.size()-1);
            }

        }

        // login thread
        int finalLogInStatus = logInStatus;
        new Thread(() -> {
            try {
                int count = 0;
                while (count < 1) {
                    JSONObject message = new JSONObject();
                    message.put("status", finalLogInStatus);
                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                    outputStream.writeUTF(message.toJSONString());
                    outputStream.flush();
                    System.out.println("message send: " + message);
                    count++;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
        return logInStatus;
    }

}
