package com.example.server;
import java.sql.*;



public class ServerDatabases {
    Connection conn ;
    ResultSet player_rs;
    ResultSet matches_rs;
    ResultSet position_rs;
    public ServerDatabases(){
        ConnectDB();
    }
    public  void ConnectDB(){
        try{
            conn = DriverManager.getConnection("jdbc:mysql://sql6.freemysqlhosting.net:3306/sql6471052","sql6471052","BgLhjRtRS1");
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }
    public ResultSet rsGetPlayers(){
        Statement statement;
        try {
            statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            return statement.executeQuery("SELECT * FROM Players");
        } catch (SQLException ex) {
            this.player_rs= null;
        }
        return null ;
    }
    public ResultSet rsGetMatches(){
        Statement statement;
        try {
            statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            return statement.executeQuery("SELECT * FROM Matches");
        } catch (SQLException ex) {
            return null;
        }
    }
    public ResultSet rsGetPositions(){
        Statement statement;
        try {
            statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            return statement.executeQuery("SELECT * FROM Positions");
        } catch (SQLException ex) {
            return null;
        }
    }


    //using prepared statement to query on log in user name and encrypted password
    public boolean logIn(String username, String password) throws SQLException{
        PreparedStatement preSt = this.conn.prepareStatement("SELECT username, password FROM Players WHERE username= ? AND password= ?");
        preSt.setString(1,username);preSt.setString(2,password);
        ResultSet results = preSt.executeQuery();
        if(results.next()){
            if(username.equals(results.getString(1)) && password.equals(results.getString(2))){
                System.out.println(" yes exist user name and encrypted password ");
                return true;
            }

            else
                return false;
        }
        else
            return false;
    }
    //check if new user name exist or not used only before sign up
    public boolean isExistUserName(String username) throws SQLException {
        PreparedStatement preSt = this.conn.prepareStatement("SELECT username, password FROM Players WHERE username= ? ");
        preSt.setString(1,username);
        ResultSet results = preSt.executeQuery();
        if(results.next()){
            if(username.equals(results.getString(1)))
                return true;
            else
                return false;
        }
        else return false ;
    }
    //using prepared statement to insert username and password
    public boolean sigUp(String username, String password) throws SQLException {
        if (!isExistUserName(username)) {
            PreparedStatement preSt = this.conn.prepareStatement("INSERT INTO Players (username, password) VALUES (?,?)");
            preSt.setString(1, username);
            preSt.setString(2, password);
            int results = preSt.executeUpdate();
            if (results == 1)
                return true;
            else
                return false;
        } else
            return false;
    }


}
