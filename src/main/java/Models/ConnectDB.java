package Models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class ConnectDB {

    public Connection getConnection() throws SQLException {
        //database uploaded on AWS EC2 instance
        return DriverManager.getConnection("jdbc:mysql://18.159.211.138:3306/xo_db", "sambo", "Sambo@123");
    }

}





