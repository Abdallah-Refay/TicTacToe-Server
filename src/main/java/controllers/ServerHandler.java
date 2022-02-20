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

public class ServerHandler extends Thread {
    private boolean running; //in case of server disconnected it would be false to stop while true in run method
    private DataInputStream dataInputStream;//stream comes from client A
    private DataOutputStream dataOutputStream;//stream goes to client A in case of login,signup,logout,close
    //virtual port opened for client at connection
    //so you make it equal to socket after acceptance method in server code "look at line number 25 in server"
    private Socket socket;//each Client connected has its own socket
    static Vector<ServerHandler> clients = new Vector<>(); //adding each client after connection successeded
    static HashMap<Integer,ServerHandler> players = new HashMap<>();//adding client id and this(referring to its socket and data input stream and output)
    private int currentID;
    public ServerHandler(Socket c) {
        try {
            //in this try connection is established successfully
            dataInputStream = new DataInputStream(c.getInputStream());
            dataOutputStream = new DataOutputStream(c.getOutputStream());
            clients.add(this);  //adding this client to clients vector with its socket
            socket=c;
            start();
        } catch (IOException e) {
            close(dataInputStream, dataOutputStream);
        }
    }
    public void run(){
        running = true ;
        while (running) {
            try {
                //stream recieved from client through socket
                String lineSent = dataInputStream.readUTF();//rinad type sign in
                if(lineSent == null)throw new IOException();
                //convert string comes from client to jason object
                //each json object comes from client has an attribute called type
                //we gonna switch on this type to check if login , signup , play .......
                JsonObject requestObject = JsonParser.parseString(lineSent).getAsJsonObject();
                String type = requestObject.get("type").getAsString();
                //response object from opponent player at each switch statement
                JsonObject responseObject = new JsonObject();
                ServerHandler opponent;
                int opponentID;
                Game game;
                //--------------------------------------------------------------------------
                switch (type) {
                    case "login":
                        Player player = login(requestObject);
                        if(player == null){
                            responseObject.addProperty("type","loginresponse");
                            responseObject.addProperty("successful", "false");
                            dataOutputStream.writeUTF(responseObject.toString()); //this would get back to client to handle such error in log in
                            System.out.println("user name : "+requestObject.get("username")+ " does not exist ");
                        } else {
                            System.out.println(player.getId());
                            responseObject.addProperty("type","loginresponse");
                            responseObject.addProperty("successful", "true");
                            responseObject.addProperty("id", player.getId());
                            responseObject.addProperty("username", player.getUsername());
                            responseObject.addProperty("score", player.getScore());
                            responseObject.addProperty("wins", player.getWins());
                            responseObject.addProperty("losses", player.getLosses());
                            players.put(player.getId(),this); //once player logged in add it in hashmap //it would be needed in (invitation, game , chat )
                            this.currentID = player.getId();//you are going to need it in later deleting from players by id in case of log out
                            clients.add(this);
                            dataOutputStream.writeUTF(responseObject.toString());
                            //after log in need to send a new array of all online players
                            // to each player in clients  connected now to server
                            //to update list of online players at client gui
                            updateList(responseObject);
                            System.out.println("user name : "+requestObject.get("username")+ "  logged in ");
                        }
                        break;
                    case"logout":
                        logout(requestObject.toString());
                        clients.remove(this);
                        System.out.println("use name : "+requestObject.get("username")+ " has logged out ");
                        players.remove(this.currentID);
                        break;
                    case "signup":
                        if(signup(requestObject)) {
                            responseObject.addProperty("successful", "true");
                            dataOutputStream.writeUTF(responseObject.toString());
                            System.out.println("use name : "+requestObject.get("username")+ " has signed up successfully ");
                        } else {
                            responseObject.addProperty("successful", "false");
                            dataOutputStream.writeUTF(responseObject.toString());
                            System.out.println("use name : "+requestObject.get("username")+ " can not sign up ");
                        }
                        break;
                    //------------------------------------------------------------------------------------------------------
                    case "sendInvitation":
                        int senderId=Integer.parseInt(requestObject.get("senderplayerid").getAsString());
                        String senderUsername=requestObject.get("senderusername").getAsString();
                        int senderScore=requestObject.get("senderscore").getAsInt();
                        int receiverId=Integer.parseInt(requestObject.get("sendtoid").getAsString());
                        game = createGame();
                        System.out.println(game.getId());
                        responseObject.addProperty("game_id", game.getId());
                        responseObject.addProperty("type","invitationreceived");
                        responseObject.addProperty("sender",senderId);
                        responseObject.addProperty("opponentusername",senderUsername);
                        responseObject.addProperty("opponentsscore",senderScore);
                        ServerHandler receiverhandler=players.get(receiverId);
                        System.out.println("player "+senderUsername +" of id : "+senderId+"sent invitation  to player "+players.get(receiverId) +" of id : "+receiverId);
                        System.out.println(players);
                        receiverhandler.dataOutputStream.writeUTF(responseObject.toString());
                        System.out.println(" invitation has been sent on socket : "+players.get(receiverId).socket.getPort());
                        break;

                    case "acceptinvetation":
                        int accepterId=Integer.parseInt(requestObject.get("accepter").getAsString());
                        int acceptedId=Integer.parseInt(requestObject.get("accepted").getAsString());
                        int acceptedGameID=requestObject.get("game_id").getAsInt();
                        responseObject.addProperty("type","yourinvetationaccepted");
                        responseObject.addProperty("game_id",acceptedGameID);
                        responseObject.addProperty("whoaccepted",accepterId);
                        ServerHandler acceptedhandler=players.get(acceptedId);
                        System.out.println(players);
                        System.out.println("player "+players.get(accepterId) +" of id : "+accepterId+"accepted your invitation.");
                        acceptedhandler.dataOutputStream.writeUTF(responseObject.toString());
                        System.out.println(" acceptance has been sent on socket : "+players.get(currentID).socket.getPort());
                        break;
                    //------------------------------------------------------------------------------------------------------------
                    case "create_game":
                        game = createGame();
                        System.out.println(game.getId());
                        responseObject.addProperty("game_id", game.getId());
                        //Show gameboard with the response object with the game id
                        break;
                    case "play" :
                        opponentID=Integer.parseInt(requestObject.get("opponet").getAsString());
                        System.out.println("play"+opponentID);
                        String position=requestObject.get("position").getAsString();
                        String sign=requestObject.get("sign").getAsString();
                        opponent=players.get(opponentID);
                        responseObject.addProperty("type","oponnetmove");
                        responseObject.addProperty("position",position);
                        responseObject.addProperty("opponentsing",sign);
                        requestObject.addProperty("player_id", this.currentID);
                        GameRecord gameRecord = move(requestObject);
                        opponent.dataOutputStream.writeUTF(responseObject.toString());;
                        break;
                    case "finish_game":
                        finishGame(requestObject);
                        updateList(responseObject);
                        break;
                    //---------------------------------------------------------------------------------------------
                    case "client_close":
                        String closingClientusername=requestObject.get("username").getAsString();
                        logout( closingClientusername);
                        this.dataOutputStream.close();
                        this.dataInputStream.close();
                        clients.remove(this);
                        players.remove(this.currentID);
                        System.out.println("Player with id " + this.currentID + " closed the client.");
                        responseObject.addProperty("type","update-list");
                        Player player3=new Player();
                        JsonArray newonlineplayersjsonarr=new JsonArray();
                        ArrayList<Player> newonlinePlayers=player3.findOnlinePlayers();
                        for(Player onplayer:newonlinePlayers) {
                            JsonObject playerJson=new JsonObject();
                            playerJson.addProperty("username",onplayer.getUsername());
                            playerJson.addProperty("id",onplayer.getId());
                            playerJson.addProperty("score",onplayer.getScore());
                            newonlineplayersjsonarr.add(playerJson);
                        }
                        responseObject.add("onlineplayers",newonlineplayersjsonarr);
                        System.out.println("new online players"+newonlineplayersjsonarr);
                        JsonArray newofflineplayersjsonarr=new JsonArray();
                        ArrayList<Player> newofflinePlayers=player3.findOfflinePlayers();
                        for(Player offplayer:newofflinePlayers){
                            JsonObject playerJson=new JsonObject();
                            playerJson.addProperty("username",offplayer.getUsername());
                            playerJson.addProperty("id",offplayer.getId());
                            playerJson.addProperty("score",offplayer.getScore());
                            newofflineplayersjsonarr.add(playerJson);
                        }
                        System.out.println("new offline players"+newofflineplayersjsonarr);
                        responseObject.add("offlineplayers",newofflineplayersjsonarr);

                        for(ServerHandler client:clients) {
                            System.out.println("send for clients about");
                            client.dataOutputStream.writeUTF(responseObject.toString());
                        }
                        break;

                    case "client_close_while_playing":
                        clients.remove(this);
                        players.remove(this.currentID);
                        opponentID = requestObject.get("opponentId").getAsInt();
                        opponent = players.get(opponentID);
                        responseObject.addProperty("type", "opponent_disconnect");
                        opponent.dataOutputStream.writeUTF(responseObject.toString());
                        System.out.println("Player with id " + this.currentID + " closed the client while playing.");
                        break;
                    case "getonlineplayers" :
                        Player player1=new Player();
                        JsonArray onlineplayersjsonarr=new JsonArray();
                        ArrayList<Player> onlinePlayers=player1.findOnlinePlayers();
                        for(Player onplayer:onlinePlayers) {
                            JsonObject playerJson=new JsonObject();
                            playerJson.addProperty("username",onplayer.getUsername());
                            playerJson.addProperty("id",onplayer.getId());
                            playerJson.addProperty("score",onplayer.getScore());
                            onlineplayersjsonarr.add(playerJson);
                        }
                        System.out.println(onlineplayersjsonarr);
                        responseObject.add("onlineplayers",onlineplayersjsonarr);
                        responseObject.addProperty("type","onlineplayers");
                        dataOutputStream.writeUTF(responseObject.toString());
                        break;

                    case "getofflineplayers" :
                        Player player2=new Player();
                        ArrayList<Player> offlinePlayers=player2.findOfflinePlayers();

                        JsonArray offlineplayersjsonarr=new JsonArray();
                        for(Player offplayer:offlinePlayers) {
                            JsonObject playerJson=new JsonObject();
                            playerJson.addProperty("username",offplayer.getUsername());
                            playerJson.addProperty("id",offplayer.getId());
                            playerJson.addProperty("score",offplayer.getScore());
                            offlineplayersjsonarr.add(playerJson);
                        }
                        System.out.println(offlineplayersjsonarr);
                        responseObject.add("offlineplayers",offlineplayersjsonarr);
                        responseObject.addProperty("type","offlineplayers");
                        dataOutputStream.writeUTF(responseObject.toString());
                        break;

                    case "request_record":
                        int gameID = requestObject.get("game_id").getAsInt();
                        String[] moves = getMoves(gameID);
                        responseObject.addProperty("type","game_record");
                        responseObject.addProperty("moves", Arrays.toString(moves));
                        dataOutputStream.writeUTF(responseObject.toString());
                        break;
                }
                if(requestObject == null|| type.equals("close")){
                    leaveNetwork(this);
                    throw new IOException();
                }
            } catch (EOFException | SocketException e) {
                running = false;
                return;
            } catch (IOException e) {
                running = false;
                close(dataInputStream, dataOutputStream);
                break;
            }
        }
    }
    //////////////////////////////////////////////////////////
    public Player login(JsonObject request) {
        String username = request.get("username").getAsString();
        String password = request.get("password").getAsString();
        Player player = new Player();
        return player.login(username, password);
    }
    public void logout(String name) {
        String username = name;
        Player player = new Player();
        player.logout(username);
    }
    public void updateList(JsonObject responseObject){
        responseObject.addProperty("type","update-list");
        Player player3=new Player();
        JsonArray newonlineplayersjsonarr=new JsonArray();
        ArrayList<Player> newonlinePlayers=player3.findOnlinePlayers();
        for(Player onplayer:newonlinePlayers)
        {
            JsonObject playerJson=new JsonObject();
            playerJson.addProperty("username",onplayer.getUsername());
            playerJson.addProperty("id",onplayer.getId());
            playerJson.addProperty("score",onplayer.getScore());
            newonlineplayersjsonarr.add(playerJson);
        }
        responseObject.add("onlineplayers",newonlineplayersjsonarr);
        System.out.println("new online players"+newonlineplayersjsonarr);
        JsonArray newofflineplayersjsonarr=new JsonArray();
        ArrayList<Player> newofflinePlayers=player3.findOfflinePlayers();
        for(Player offplayer:newofflinePlayers)
        {
            JsonObject playerJson=new JsonObject();
            playerJson.addProperty("username",offplayer.getUsername());
            playerJson.addProperty("id",offplayer.getId());
            playerJson.addProperty("score",offplayer.getScore());
            newofflineplayersjsonarr.add(playerJson);
        }
        System.out.println("new offline players"+newofflineplayersjsonarr);
        responseObject.add("offlineplayers",newofflineplayersjsonarr);

        for(ServerHandler client:clients)
        {
            System.out.println("send for clients about");
            try {
                client.dataOutputStream.writeUTF(responseObject.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public boolean signup(JsonObject request) {
        String username = request.get("username").getAsString();
        String password = request.get("password").getAsString();
        Player player = new Player();
        return player.signUp(username, password);
    }
    public Game createGame() {
        Game game = new Game();
        return game.create();
    }
    public GameRecord move(JsonObject msg) {
        int gameID = msg.get("game_id").getAsInt();
        int playerID = msg.get("player_id").getAsInt();
        int position = msg.get("position").getAsInt();
        int move = msg.get("sign").getAsInt();
        GameRecord gameRecord = new GameRecord();
        return gameRecord.create(gameID, playerID, move, position);
    }
    public void finishGame(JsonObject msg) {
        int winnerID = msg.get("winner").getAsInt();
        int loserID = msg.get("looser").getAsInt();
        int gameID = msg.get("game_id").getAsInt();

        System.out.println(winnerID);
        System.out.println(loserID);

        Player player = new Player();

        player.wins(winnerID);
        player.loses(loserID);

        new Game().finishGame(gameID, Integer.toString(winnerID));
    }
    public String[] getMoves(int gameID) {
        GameRecord gameRecord = new GameRecord();
        ArrayList<GameRecord> movesAL = gameRecord.findByGameID(gameID);
        int arraySize = movesAL.size();
        String[] moves = new String[arraySize];

        for (int i = 0; i < arraySize; i++) {
            String move = movesAL.get(i).getPosition() + "-" + movesAL.get(i).getMove() + "-" + movesAL.get(i).getPlayerID();
            moves[i] = move;
        }
        return moves;
    }
    public void leaveNetwork(ServerHandler serverHandler){
        clients.remove(serverHandler);
        System.out.println("left chat");
    }
    public void close(DataInputStream reader, DataOutputStream writer) {
        //so need to give client feed back on his own gui that server is down
        //and also send to all clients that server is closed
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("type", "server_closed");
        System.out.println("Current players connected:  "+players.size());
        if(players.size() > 0) {
            players.forEach((id, handler) -> {
                try {
                    //send that json of closed server message to each connected client
                    handler.dataOutputStream.writeUTF(responseObject.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        //set running flage false to make server handler stops from listening to new clients in his thread
        running= false;
        try {
            if(reader != null)
                reader.close();
            if(writer!=null)
                writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
