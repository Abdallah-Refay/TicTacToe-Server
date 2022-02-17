package controllers;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
public class Server extends Thread{
    static Socket socket;
    private static ServerSocket serverSocket;
    static Thread th ;
    public Server() {
        System.out.println("Starting el server  ");
        open() ;
    }
    //opening server and waiting for clients connection
    public static void open(){
        try {
            serverSocket = new ServerSocket(5001);
            //when server is initialized always listen to incoming client connection in thread
            //we made a thread to make that server app does not stop infinitely to accept client connection at each time
            //making this thread in constructor of server to be called once server object created at initializing of server app
            th = new Thread(() -> {
                while (!serverSocket.isClosed()) {
                    try {
                        System.out.println( "Server has been started  ");
                        socket = serverSocket.accept();
                        System.out.println("client connected on port "+socket.getLocalPort());
                        // new ServerHandler(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            th.start();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    // closing server
    public static void close(){
        if (serverSocket != null) {
            try {
                th.stop();
                serverSocket.close();
                System.out.println("server is closed ;D");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args){
        new Server();
    }
}
