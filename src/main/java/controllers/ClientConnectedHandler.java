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
            //in this try connection is established successfully
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
    public void run(){
        running = true ;
        while (running){
            try {
                String str = dataInputStream.readUTF();
                JsonObject request = JsonParser.parseString(str).getAsJsonObject();
                String type = request.get("type").getAsString();
                JsonObject response = new JsonObject();
                ClientConnectedHandler opponent ;
                int opponentId ;
                Game game ;
                //-------------------------------------------------------------------
                switch(type){
                    case "login":
                        Player player = login(request);
                        if(player == null){
                            request.addProperty("type","login_response");
                            request.addProperty("successful", "false");
                            //this would get back to client to handle such error in log in
                            this.dataOutputStream.writeUTF(request.toString());
                        } else {
                            request.addProperty("type","login_response");
                            request.addProperty("successful", "true");
                            request.addProperty("id", player.getId());
                            request.addProperty("username", player.getUsername());
                            request.addProperty("score", player.getScore());
                            request.addProperty("wins", player.getWins());
                            request.addProperty("losses", player.getLosses());
                            //once player logged in add it in hashmap
                            //it would be needed in (invitation, game , chat )
                            players.put(player.getId(),this);
                            //you gonna need it in deleting from players by id
                            this.clientConnectedID = player.getId();
                            clients.add(this);
                            dataOutputStream.writeUTF(request.toString());
                            //updateList(request);
                        }
                        break;
                    case "logout":
                        clients.remove(this);
                        players.remove(this.clientConnectedID);
                        break;
                    case "signup":
                        if(signup(request)) {
                            response.addProperty("successful", "true");
                            this.dataOutputStream.writeUTF(response.toString());
                        }else {
                            response.addProperty("successful", "false");
                            this.dataOutputStream.writeUTF(response.toString());
                        }
                        break;
                    case "create_game":
                        break;
                    case "play":
                        break;
                    case "sendInvitation":
                        break;
                    case "acceptinvetation":
                        break;
                    case "finish_game":
                        break;
                    case "client_close":
                        break;
                    case "client_close_while_playing":
                        break;
                    case "getonlineplayers":
                        break;
                    case "getofflineplayers":
                        break;
                    case "request_record":
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
    public Player login(JsonObject request){
        Player player = new Player();
        return player ;
    }
    public Player logout(JsonObject request){
        Player player = new Player();
        return player ;
    }
    public boolean signup(JsonObject request){
        String userName = request.get("username").toString();
        String password = request.get("password").toString();
        Player player = new Player();
        return player.signUp(userName,password ) ;
    }


}
