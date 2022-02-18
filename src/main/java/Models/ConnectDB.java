package Models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class ConnectDB {

    public Connection getConnection() throws SQLException {
        //database uploaded on AWS EC2 instance
        return DriverManager.getConnection("jdbc:mysql://19.169.201.198:3306/xo_db", "mohamed", "mohamed@123");
    }

}





