package Server;

import org.json.simple.JSONObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LoginProcessor {

    public int checkLoginProcessor(JSONObject jsonObject, DBUtils dbUtils) throws SQLException {
        String username = (String) jsonObject.get("username");
        String password = (String) jsonObject.get("password");


        Connection connection = dbUtils.getConnection();

        //check existence
        String checkExistence = "SELECT * FROM user WHERE username = " + username;
        Statement statement1 = connection.createStatement();
        ResultSet rs1 = statement1.executeQuery(checkExistence);

        if (!rs1.next()) {
            String createUser = "INSERT INTO user (userID, username, managerID, password) VALUE (NULL, "
                    + username + ",NULL, " + password + ")";
            Statement statement2 = connection.createStatement();
            statement2.execute(createUser);
            return 1;
        } else {
            while (rs1.next()) {
                String passw = rs1.getString("password");
                if (passw.equals(password)) {
                    return 2;
                } else {
                    return 3;
                }

            }
        }
        return 5;
    }

}
