package com.example.server;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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


    //ensure that input field of alphanumeric and have only _ char
    public String validatUserName(String input){
        String regex = "^[A-Za-z]\\w{5,29}$";
        // Compile the ReGex
        Pattern p = Pattern.compile(regex);
        // If the username is empty return false
        //if (input == null) { return false; }
        // Pattern class contains matcher() method to find matching between given username and regular expression.
        Matcher m = p.matcher(input);
        if(m.matches()) return input;
        else return null;
    }
    //hashing input field of password
    public String hashingPassword(String input){
        try {
            // getInstance() method is called with algorithm SHA-1
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());
            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);
            // Convert message digest into hex value
            String hashtext = no.toString(16);
            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    //login through function in database query on UserName input and encrypted password
    public void playerLogIn() throws SQLException {
        //using validUserName and encryptPass to insert successfully
        String validUserName = validatUserName(getUserName());
        String encryptPass = hashingPassword(getPassword());
        if(getUserName().isBlank()==false &&getPassword().isBlank()==false){
            System.out.println( "logging in ..................." );
            if( serverBuild.logIn(validUserName , encryptPass)){
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
    //sign up through function in database query on UserName input and encrypted password
    public void playerSignUp() throws SQLException {
        if(signUpUserName.getText().isBlank()==false &&signUpPassword.getText().isBlank()==false){
            //using validUserName and encryptPass to insert successfully
            String validUserName = validatUserName(getSignUpUserName());
            String encryptPass = hashingPassword(getSignUpPassword());
            //------------------------------------------------------------------
            System.out.println( "Signing Up ..................." );
            if( serverBuild.sigUp(validUserName , encryptPass)){
                System.out.println("Welcome new member "+validUserName);
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