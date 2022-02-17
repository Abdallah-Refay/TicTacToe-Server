package controllers;
import Models.Game;
import Models.GameRecord;
import Models.Player;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;


public class ClientConnectedHandler extends Thread {
    private boolean running ; //handle while true in acceptance of socket client connection
    private DataInputStream dataInputStream ;//stream comes from client A
    private DataOutputStream dataOutputStream ;//stream goes to client A in case of login,signup,logout,close
    private Socket clientSocket;//each Client connected has its own socket
    static Vector<ClientConnectedHandler> clients = new Vector<>(); //adding each client after connection successeded
    static HashMap<Integer,ClientConnectedHandler> players = new HashMap<>(); //adding client id and this(refering to its socket and data iput stream and output)
    private int clientConnectedID;
    public ClientConnectedHandler(Socket clientSocket) {
        try {
            //in this try connection is established successfuly
            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            //adding this client to clients vector with its socket
            clients.add(this);
            this.clientSocket=clientSocket;
            start();
        } catch (IOException e) {
            //connection failed in case server is closed
            close(dataInputStream, dataOutputStream);
        }
    }

    public void close(DataInputStream reader, DataOutputStream writer ){
        //so need to give client feed back on his own gui that server is down
        //and also send to all clients that server is closed
        JsonObject response = new JsonObject();
        response.addProperty("type","Server Closed");
        clients.forEach(client-> {
            try {
                client.dataOutputStream.writeUTF(response.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }
}
