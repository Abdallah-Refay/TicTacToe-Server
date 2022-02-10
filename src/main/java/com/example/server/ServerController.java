package com.example.server;
import javafx.event.ActionEvent;
import java.sql.*;
import static com.example.server.ServerMain.serverBuild;
public class ServerController {


    public void playerExist(){
    }
    public void playerSignUp(){

    }

    public void Start(ActionEvent event) {
        serverBuild = new ServerDatabases();
        try{
            if(!serverBuild.conn.isClosed()){
                System.out.println("Server is connected to databases , on github");
            }
            else System.out.println("Server is not connected to databases , on github");
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