package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtils {

    private static final String url = "jdbc:mysql://localhost:3306/ds_project2?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String user = "root";
    private static final String password = "root";
    private static Connection connection;
    static{
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url,user,password);
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            //todo
        } catch (SQLException e) {
            e.printStackTrace();
            //todo
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
