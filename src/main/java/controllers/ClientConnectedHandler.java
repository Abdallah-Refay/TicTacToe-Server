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

//*****note : each response would be handled in client side also by switch cases
// take all passwords passed to server side especially in this class shall be hashed
public class ClientConnectedHandler extends Thread {
    private boolean running ; //handle while true in acceptance of socket client connection
    private DataInputStream dataInputStream ;//stream comes from client A
    private DataOutputStream dataOutputStream ;//stream goes to client A in case of login,signup,logout,close
    private Socket clientSocket;//each Client connected has its own socket
    static Vector<ClientConnectedHandler> clients = new Vector<>(); //adding each client after connection successeded
    static HashMap<Integer,ClientConnectedHandler> players = new HashMap<>(); //adding client id and this(referring to its socket and data input stream and output)
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
                            response.addProperty("type","login_response");
                            response.addProperty("successful", "false");
                            this.dataOutputStream.writeUTF(response.toString()); //this would get back to client to handle such error in log in
                        } else {
                            response.addProperty("type","login_response");
                            response.addProperty("successful", "true");
                            players.put(player.getId(),this); //once player logged in add it in hashmap //it would be needed in (invitation, game , chat )
                            this.clientConnectedID = player.getId();//you are going to need it in later deleting from players by id in case of log out
                            clients.add(this);
                            dataOutputStream.writeUTF(response.toString());
                            //after log in need to send a new array of all online players
                            // to each player in clients  connected now to server
                            //to update list of online players at client gui
                            updateList(response) ;
                        }
                        break;
                    case "logout":
                        logout(response);
                        clients.remove(this);
                        players.remove(this.clientConnectedID);
                        break;
                    case "signup":
                        if(signup(request)) {
                            response.addProperty("type","signup_response");
                            response.addProperty("successful", "true");
                            this.dataOutputStream.writeUTF(response.toString());
                        }else {
                            response.addProperty("type","signup_response");
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
                    case "get_online_players":
                        break;
                    case "get_offline_players":
                        break;
                    case "request_record":
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private void close(DataInputStream reader, DataOutputStream writer ){
        //so need to give client feed back on his own gui that server is down
        //and also send to all clients that server is closed
        JsonObject response = new JsonObject();
        response.addProperty("type","server_closed");
        clients.forEach(client-> {
            try {
                 client.dataOutputStream.writeUTF(response.toString());
            } catch (IOException e) {
                 e.printStackTrace();
            }
        });

    }
    private Player login(JsonObject request){
        Player player = new Player();
        String username = request.get("username").getAsString();
        String password = request.get("password").getAsString();
        return player.login(username, password);
    }
    private void logout(JsonObject request){
        Player player = new Player();
        String userName = request.get("username").toString();
        player.logout(userName);
    }
    private boolean signup(JsonObject request){
        String userName = request.get("username").toString();
        String password = request.get("password").toString();
        Player player = new Player();
        return player.signUp(userName,password ) ;
    }
    private void updateList(JsonObject response) {
        response.addProperty("type","update-list");
        Player player=new Player();
        ///for online players update
        JsonArray new_online_players_json_array=new JsonArray();
        ArrayList<Player> new_online_Players=player.findOnlinePlayers();
        for(Player online_player:new_online_Players) {
            JsonObject playerJson=new JsonObject();
            playerJson.addProperty("username",online_player.getUsername());
            playerJson.addProperty("id",online_player.getId());
            playerJson.addProperty("score",online_player.getScore());
            new_online_players_json_array.add(playerJson);
        }
        response.add("online_players",new_online_players_json_array);
        ///for offline players update
        JsonArray new_offline_players_json_array=new JsonArray();
        ArrayList<Player> new_offline_Players=player.findOfflinePlayers();
        for(Player off_player:new_offline_Players)
        {
            JsonObject playerJson=new JsonObject();
            playerJson.addProperty("username",off_player.getUsername());
            playerJson.addProperty("id",off_player.getId());
            playerJson.addProperty("score",off_player.getScore());
            new_offline_players_json_array.add(playerJson);
        }
        response.add("offline_players",new_offline_players_json_array);

        for(ClientConnectedHandler client:clients)
        {
            try {
                //send response of type update list and both online and
                //offline list of players to each client socket to update in gui
                client.dataOutputStream.writeUTF(response.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
//response types which shall be handled in client side
// "type","login_response" -> "successful", "false" || "successful", "true"
// "type","update-list" ->  "offline_players",new_offline_players_json_array && "online_players",new_online_players_json_array
// "type","signup_response" -> "successful", "false" || "successful", "true"
//
//
//
//
//
//
//
//
//