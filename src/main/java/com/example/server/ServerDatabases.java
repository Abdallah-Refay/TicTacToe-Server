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
            return null;
        }
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
    public boolean logIn(String username, String password) throws SQLException{
        player_rs = rsGetPlayers();
        player_rs.first();
        boolean flage = true ;
        while(flage){
            if(player_rs.getString(1)==username && player_rs.getString(2)==password){
                System.out.println("Player Exists");
                break;
            }
            else if(player_rs.isLast()){
                flage =false ;
                System.out.println("Player does not Exist and needs to sign up");
                break;
            }
            else{
                player_rs.next();
            }
        }
        return flage;
    }
    public void sigUp(String username, String password) throws SQLException{
        player_rs = rsGetPlayers();
        player_rs.first();
        boolean flage = true ;
        while(flage){
            if(player_rs.getString(1)==username && player_rs.getString(1)==password){
                System.out.println("Player Exists");
                break;
            }
            else if(player_rs.isLast()){
                flage =false ;
                System.out.println("Player does not Exist and needs to sign up");
                break;
            }
            else{
                player_rs.next();
            }
        }
    }


}
