package com.example.server;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;


public class ServerController  {
    @FXML
    private Button logIn ;
    @FXML
    private TextField userName ;
    @FXML
    private PasswordField Password ;
    @FXML
    private Button signup ;
    @FXML
    private TextField signUpUserName ;
    @FXML
    private PasswordField signUpPassword ;

    ServerDatabases serverBuild;

    public String getUserName(){
        return userName.getText();
    }
    public String getPassword(){
        return Password.getText();
    }
    public String getSignUpUserName(){
        return signUpUserName.getText();
    }
    public String getSignUpPassword(){
        return signUpPassword .getText();
    }
    public void validatLogIn(){
        ResultSet rs = serverBuild.player_rs;
        getUserName();
        getPassword();
    }
    public void playerLogIn() throws SQLException {
        if(userName.getText().isBlank()==false &&Password.getText().isBlank()==false){
            System.out.println( "logging in ..................." );
            if( serverBuild.logIn(getUserName() , getPassword())){
                System.out.println("hello "+getUserName());
            }
            else{
                System.out.println("Invalid user name or password");
            }
        }
        else{
            System.out.println("Enter user name and password ");
        }


    }
    public void playerSignUp() throws SQLException {
        if(signUpUserName.getText().isBlank()==false &&signUpPassword.getText().isBlank()==false){
            System.out.println( "Signing Up ..................." );
            if( serverBuild.sigUp(getSignUpUserName() , getSignUpPassword())){
                System.out.println("Welcome new member "+getSignUpUserName());
            }
            else{
                System.out.println("Existing user name or password");
            }
        }
        else{
            System.out.println("Enter user name and password ");
        }

    }





    public void Start(ActionEvent event) {
        serverBuild = new ServerDatabases();
        try{
            if(!serverBuild.conn.isClosed()){
                System.out.println("Server is connected to databases");
                //System.out.println( serverBuild.player_rs.first());
                //System.out.println( serverBuild.player_rs.getString(2) +" : "+serverBuild.player_rs.getString(3));
            }
            else System.out.println("Server is not connected to databases");
        }
        catch(Exception ex ){
            System.out.println(ex.getMessage());
        }
    }
    public void Stop(ActionEvent event) throws SQLException{
        if(!serverBuild.conn.isClosed()){
            serverBuild.conn.close();
            System.out.println("Server Databases is down now");

        }
        else{
            System.out.println("Server is down already");
        }
    }


}