package controllers;

import Models.Game;
import Models.GameRecord;
import Models.PausedGames;
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

//import static server_ui.ServerScene.table;

public class ServerHandler extends Thread {
    static Vector<ServerHandler> clients = new Vector<>(); //adding each client after connection successeded
    static HashMap<Integer, ServerHandler> players = new HashMap<>();//adding client id and this(referring to its socket and data input stream and output)
    private boolean running; //in case of server disconnected it would be false to stop while true in run method
    private DataInputStream dataInputStream;//stream comes from client A
    private DataOutputStream dataOutputStream;//stream goes to client A in case of login,signup,logout,close
    //virtual port opened for client at connection
    //so you make it equal to socket after acceptance method in server code "look at line number 25 in server"
    private Socket socket;//each Client connected has its own socket
    private int currentID;

    public ServerHandler(Socket c) {
        try {
            //in this try connection is established successfully
            dataInputStream = new DataInputStream(c.getInputStream());
            dataOutputStream = new DataOutputStream(c.getOutputStream());
            // if(!clients.contains(this)) clients.add(this);  //adding this client to clients vector with its socket
            socket = c;
            start();
        } catch (IOException e) {
            close(dataInputStream, dataOutputStream);
        }
    }

    public void run() {
        running = true;
        while (running) {
            try {
                //stream received from client through socket
                String lineSent = dataInputStream.readUTF();
                if (lineSent == null) throw new IOException();
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
                    case "sendmessageforone":
                        String senderusername = requestObject.get("senderusername").toString();
                        System.out.println(requestObject.get("recieverid"));
                        int recieverID = Integer.parseInt(String.valueOf(requestObject.get("recieverid")));
                        ServerHandler receiverHandler = players.get(recieverID);//opponent socket
                        String msg = requestObject.get("message").toString();
                        responseObject = new JsonObject();
                        responseObject.addProperty("type", "receivemessagefromone");
                        responseObject.addProperty("message", msg);
                        responseObject.addProperty("senderusername", senderusername);
                        receiverHandler.dataOutputStream.writeUTF(responseObject.toString());
                        break;
                    case "sendmessageforall":
                        String name = requestObject.get("username").toString();
                        String massege = requestObject.get("message").toString();
                        responseObject = new JsonObject();
                        responseObject.addProperty("type", "allreceivemessagefromone");
                        responseObject.addProperty("senderusername", name);
                        responseObject.addProperty("message", massege);
                        sendMsgToAll(responseObject);
                        break;
                    case "login":
                        Player player = login(requestObject);
                        if (player == null) {
                            responseObject.addProperty("type", "loginresponse");
                            responseObject.addProperty("successful", "false");
                            dataOutputStream.writeUTF(responseObject.toString()); //this would get back to client to handle such error in log in
                            System.out.println("user name : " + requestObject.get("username") + " does not exist ");
                        } else {
                            System.out.println(player.getId());
                            responseObject.addProperty("type", "loginresponse");
                            responseObject.addProperty("successful", "true");
                            responseObject.addProperty("id", player.getId());
                            responseObject.addProperty("username", player.getUsername());
                            responseObject.addProperty("score", player.getScore());
                            responseObject.addProperty("wins", player.getWins());
                            responseObject.addProperty("losses", player.getLosses());
                            players.put(player.getId(), this); //once player logged in add it in hashmap //it would be needed in (invitation, game , chat )
                            this.currentID = player.getId();//you are going to need it in later deleting from players by id in case of log out
                            clients.add(this);
                            dataOutputStream.writeUTF(responseObject.toString());
                            //after log in need to send a new array of all online players
                            // to each player in clients  connected now to server
                            //to update list of online players at client gui
                            updateList(responseObject);
                            System.out.println("user name : " + requestObject.get("username") + "  logged in ");
                            // table.setItems(Player.getAllUsers());
                            // table.refresh();
                        }
                        break;
                    case "signup":
                        if (signup(requestObject)) {
                            responseObject.addProperty("successful", "true");
                            dataOutputStream.writeUTF(responseObject.toString());
                            System.out.println("use name : " + requestObject.get("username") + " has signed up successfully ");
                        } else {
                            responseObject.addProperty("successful", "false");
                            dataOutputStream.writeUTF(responseObject.toString());
                            System.out.println("use name : " + requestObject.get("username") + " can not sign up ");
                        }
                        break;
                    //------------------------------------------------------------------------------------------------------
                    case "sendInvitation":
                        int senderId = Integer.parseInt(requestObject.get("senderplayerid").getAsString());
                        String senderUsername = requestObject.get("senderusername").getAsString();
                        int senderScore = requestObject.get("senderscore").getAsInt();
                        int receiverId = Integer.parseInt(requestObject.get("sendtoid").getAsString());
                        int gameID2;
                        game = createGame();
                        System.out.println("*******************" + game.getId());
                        if (game.getLatestGameID() == 0) gameID2 = 1;
                        else gameID2 = game.getLatestGameID() + 1;
                        game = createGame(gameID2);
                        System.out.println("*******************" + game.getId());
                        System.out.println(game.getId());
                        responseObject.addProperty("game_id", game.getId());
                        responseObject.addProperty("type", "invitationreceived");
                        responseObject.addProperty("sender", senderId);
                        responseObject.addProperty("opponentusername", senderUsername);
                        responseObject.addProperty("opponentsscore", senderScore);
                        ServerHandler receiverhandler = players.get(receiverId);
                        System.out.println("player " + senderUsername + " of id : " + senderId + "sent invitation  to player " + players.get(receiverId) + " of id : " + receiverId);
                        System.out.println(players);
                        receiverhandler.dataOutputStream.writeUTF(responseObject.toString());
                        System.out.println(" invitation has been sent on socket : " + players.get(receiverId).socket.getPort());
                        break;
                    case "acceptinvetation":
                        int accepterId = Integer.parseInt(requestObject.get("accepter").getAsString());
                        int acceptedId = Integer.parseInt(requestObject.get("accepted").getAsString());
                        int acceptedGameID = requestObject.get("game_id").getAsInt();
                        String acceptername = new Player().findID(accepterId).getUsername();
                        String accptedname = new Player().findID(acceptedId).getUsername();
                        responseObject.addProperty("type", "yourinvetationaccepted");

                        responseObject.addProperty("game_id", acceptedGameID);
                        responseObject.addProperty("whoaccepted", accepterId);
                        responseObject.addProperty("acceptername", acceptername);
                        responseObject.addProperty("accptedname", accptedname);

                        ServerHandler acceptedhandler = players.get(acceptedId);
                        System.out.println(players);
                        System.out.println("player " + players.get(accepterId) + " of id : " + accepterId + "accepted your invitation.");
                        acceptedhandler.dataOutputStream.writeUTF(responseObject.toString());
                        System.out.println(" acceptance has been sent on socket : " + players.get(currentID).socket.getPort());
                        break;
                    //------------------------------------------------------------------------------------------------------------
                    case "create_game":
                        game = createGame();
                        System.out.println(game.getId());
                        responseObject.addProperty("game_id", game.getId());
                        //Show game board with the response object with the game id
                        break;
                    case "play":
                        opponentID = Integer.parseInt(requestObject.get("opponet").getAsString());
                        System.out.println("play" + opponentID);
                        String position = requestObject.get("position").getAsString();
                        String sign = requestObject.get("sign").getAsString();
                        opponent = players.get(opponentID);
                        responseObject.addProperty("type", "oponnetmove");
                        responseObject.addProperty("position", position);
                        responseObject.addProperty("opponentsing", sign);
                        requestObject.addProperty("player_id", this.currentID);
                        GameRecord gameRecord = move(requestObject);
                        opponent.dataOutputStream.writeUTF(responseObject.toString());
                        break;
                    case "pause-game":
                        pauseGame(requestObject);
                        //updateList(responseObject);
                        break;
                    case "finish_game":
                        finishGame(requestObject);
                        updateList(responseObject);
                        break;
                    //---------------------------------------------------------------------------------------------
                    //case "client_close":
                    case "logout":
                        String closingClientusername = requestObject.get("username").getAsString();
                        logout(closingClientusername);
                        this.dataOutputStream.close();
                        this.dataInputStream.close();
                        clients.remove(this);
                        players.remove(this.currentID);
                        System.out.println("Player with id " + this.currentID + " closed the client.");
                        updateList(responseObject);
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
                    case "getonlineplayers":
                        Player player1 = new Player();
                        JsonArray onlineplayersjsonarr = new JsonArray();
                        ArrayList<Player> onlinePlayers = player1.findOnlinePlayers();
                        for (Player onplayer : onlinePlayers) {
                            JsonObject playerJson = new JsonObject();
                            playerJson.addProperty("username", onplayer.getUsername());
                            playerJson.addProperty("id", onplayer.getId());
                            playerJson.addProperty("score", onplayer.getScore());
                            playerJson.addProperty("wins", onplayer.getWins());
                            playerJson.addProperty("losses", onplayer.getLosses());
                            onlineplayersjsonarr.add(playerJson);
                        }
                        System.out.println(onlineplayersjsonarr);
                        responseObject.add("onlineplayers", onlineplayersjsonarr);
                        responseObject.addProperty("type", "onlineplayers");
                        dataOutputStream.writeUTF(responseObject.toString());
                        break;
                    case "getofflineplayers":
                        Player player2 = new Player();
                        ArrayList<Player> offlinePlayers = player2.findOfflinePlayers();
                        JsonArray offlineplayersjsonarr = new JsonArray();
                        for (Player offplayer : offlinePlayers) {
                            JsonObject playerJson = new JsonObject();
                            playerJson.addProperty("username", offplayer.getUsername());
                            playerJson.addProperty("id", offplayer.getId());
                            playerJson.addProperty("score", offplayer.getScore());
                            playerJson.addProperty("wins", offplayer.getWins());
                            playerJson.addProperty("losses", offplayer.getLosses());
                            offlineplayersjsonarr.add(playerJson);
                        }
                        System.out.println(offlineplayersjsonarr);
                        responseObject.add("offlineplayers", offlineplayersjsonarr);
                        responseObject.addProperty("type", "offlineplayers");
                        dataOutputStream.writeUTF(responseObject.toString());
                        break;
                    case "request_record":
                        int gameID = requestObject.get("game_id").getAsInt();
                        String[] moves = getMoves(gameID);
                        responseObject.addProperty("type", "game_record");
                        responseObject.addProperty("moves", Arrays.toString(moves));
                        dataOutputStream.writeUTF(responseObject.toString());
                        break;
                    case "latestgameid":
                        Game game1 = new Game();
                        responseObject.addProperty("type", "backGameID");
                        responseObject.addProperty("id", game1.getLatestGameID());
                        break;
                    case "pausegame":
                        int senderID = Integer.parseInt(requestObject.get("senderplayerid").toString());
                        int sendToID = Integer.parseInt(requestObject.get("sendtoid").toString());
                        int gameId = Integer.parseInt(requestObject.get("gameid").toString());
                        String sender = requestObject.get("sender").toString();
                        ServerHandler sendTo = players.get(sendToID);
                        System.out.println("*********************************************************");
                        System.out.println("i am server sending to admin2 that admin1 want to pause ?");
                        System.out.println("*********************************************************");
                        JsonObject response = new JsonObject();
                        response.addProperty("type", "askingforPausing");
                        response.addProperty("senderid", senderID);
                        response.addProperty("sendtoid", sendToID);
                        response.addProperty("gameid", gameId);
                        response.addProperty("sendername", sender);
                        sendTo.dataOutputStream.writeUTF(response.toString());
                        break;
                    case "acceptpause":
                        int comesfromID = Integer.parseInt(requestObject.get("accepter").toString());
                        int sendtoID = Integer.parseInt(requestObject.get("accepted").toString());
                        int gameId1 = Integer.parseInt(requestObject.get("game_id").toString());

                        ServerHandler sendToplayer = players.get(sendtoID);
                        JsonObject response1 = new JsonObject();
                        response1.addProperty("type", "pauseAcceptanceState");
                        // response1.addProperty("state",true);
                        response1.addProperty("senderid", comesfromID);
                        response1.addProperty("sendtoid", sendtoID);
                        response1.addProperty("gameid", gameId1);
                        sendToplayer.dataOutputStream.writeUTF(response1.toString());
                        System.out.println(" i am server sending to you admin1 that 2 refuse your request");
                        break;

                    case "rejectpause":
                        int comesfromID1 = Integer.parseInt(requestObject.get("accepter").toString());
                        int sendtoID1 = Integer.parseInt(requestObject.get("accepted").toString());
                        int gameId11 = Integer.parseInt(requestObject.get("game_id").toString());
                        ServerHandler sendToplayer1 = players.get(sendtoID1);
                        JsonObject response11 = new JsonObject();
                        response11.addProperty("type", "pauseRejectState");
                        //response11.addProperty("state",false);
                        response11.addProperty("senderid", comesfromID1);
                        response11.addProperty("sendtoid", sendtoID1);
                        response11.addProperty("gameid", gameId11);
                        sendToplayer1.dataOutputStream.writeUTF(response11.toString());
                        System.out.println(" i am server sending to you admin1 that 2 refuse your request");

                        break;
                    case "exitgame":
                        int loser = Integer.parseInt(requestObject.get("senderplayerid").toString());
                        int winner = Integer.parseInt(requestObject.get("sendtoid").toString());
                        int gameId2 = Integer.parseInt(requestObject.get("gameid").toString());
                        String sender2 = requestObject.get("sender").toString();
                        ServerHandler sendTo2 = players.get(winner);
                        JsonObject response2 = new JsonObject();
                        response2.addProperty("type", "opponentwithdraw");
                        response2.addProperty("sendername", sender2);
                        sendTo2.dataOutputStream.writeUTF(response2.toString());
                        JsonObject json = new JsonObject();
                        json.addProperty("winner", winner);
                        json.addProperty("looser", loser);
                        json.addProperty("game_id", gameId2);
                        finishGame(json);
                        updateList(json);
                        break;
                    case "askforpausedgames":
                        int playerid = Integer.parseInt(requestObject.get("playerId").toString());
                        ArrayList<PausedGames> newpuasingmatches = GameRecord.findPausedGames(playerid);
                        JsonArray pausingmatches = new JsonArray();
                        for (PausedGames g : newpuasingmatches) {
                            JsonObject match = new JsonObject();
                            match.addProperty("gameid", g.getGameid());
                            match.addProperty("opponentId", g.getOpponentId());
                            match.addProperty("opponentname", g.getOpponent());
                            pausingmatches.add(match);
                        }
                        JsonObject reply = new JsonObject();
                        reply.add("replyforpausedgames", pausingmatches);
                        reply.addProperty("type", "recievepasuerequest");
                        dataOutputStream.writeUTF(reply.toString());
                        break;


                    case "sendresumeinvitation":
                        int senderId1 = Integer.parseInt(requestObject.get("senderplayerid").getAsString());
                        String senderUsername1 = requestObject.get("senderusername").getAsString();
                        int receiverId1 = Integer.parseInt(requestObject.get("opponentid").getAsString());
                        int gameID1 = Integer.parseInt(requestObject.get("game_id").getAsString());
                        //game = createGame();
                        System.out.println("*********game**********" + gameID1);
                        System.out.println("*********sender**********" + senderId1);
                        System.out.println("**********send to********" + receiverId1);
                        responseObject.addProperty("type", "invitationresumereceived");
                        responseObject.addProperty("game_id", gameID1);
                        responseObject.addProperty("sender", senderId1);
                        responseObject.addProperty("opponentusername", senderUsername1);
                        responseObject.addProperty("opponentid", receiverId1);
                        ServerHandler receiverhandler1 = players.get(receiverId1);
                        // System.out.println("player "+senderUsername +" of id : "+senderId+"sent invitation  to player "+players.get(receiverId) +" of id : "+receiverId);
                        //System.out.println(players);
                        receiverhandler1.dataOutputStream.writeUTF(responseObject.toString());
                        //System.out.println(" invitation has been sent on socket : "+players.get(receiverId).socket.getPort());
                        break;
                    case "acceptresumeinvitation":
                        int accepterId1 = Integer.parseInt(requestObject.get("senderplayerid").getAsString());
                        int acceptedId1 = Integer.parseInt(requestObject.get("opponentid").getAsString());
                        int game_id1 = requestObject.get("game_id").getAsInt();

                        String acceptername1 = new Player().findID(accepterId1).getUsername();
                        String accptedname1 = new Player().findID(acceptedId1).getUsername();
                        responseObject.addProperty("type", "yourresumeinvetationaccepted");

                        responseObject.addProperty("game_id", game_id1);
                        responseObject.addProperty("whoaccepted", accepterId1);
                        responseObject.addProperty("acceptername", acceptername1);
                        responseObject.addProperty("accptedname", accptedname1);
                        //responseObject.addProperty("opponentscore" , accptedname1);
                        System.out.println("**********************************************");
                        System.out.println("abdallah : " + accepterId1);
                        System.out.println("opponent : " + acceptedId1);
                        System.out.println("game : " + game_id1);
                        System.out.println("**********************************************");
                        ServerHandler acceptedhandler1 = players.get(acceptedId1);
                        //System.out.println(players);
                        //System.out.println("player "+players.get(accepterId) +" of id : "+accepterId+"accepted your invitation.");
                        acceptedhandler1.dataOutputStream.writeUTF(responseObject.toString());
                        System.out.println(" acceptance has been sent on socket : " + players.get(currentID).socket.getPort());
                        break;
                }
                if (requestObject == null || type.equals("close")) {
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
        player.setUsername(username);
        player.setHashedPassword(password);
        return player.login(username, password);
    }

    public void logout(String name) {
        String username = name;
        Player player = new Player();
        player.logout(username);
    }

    public void updateList(JsonObject responseObject) {
        responseObject.addProperty("type", "update-list");
        Player player3 = new Player();

        JsonArray newonlineplayersjsonarr = new JsonArray();
        ArrayList<Player> newonlinePlayers = player3.findOnlinePlayers();
        for (Player onplayer : newonlinePlayers) {
            JsonObject playerJson = new JsonObject();
            playerJson.addProperty("username", onplayer.getUsername());
            playerJson.addProperty("id", onplayer.getId());
            playerJson.addProperty("score", onplayer.getScore());
            newonlineplayersjsonarr.add(playerJson);
        }
        responseObject.add("onlineplayers", newonlineplayersjsonarr);
        System.out.println("new online players" + newonlineplayersjsonarr);

        JsonArray newofflineplayersjsonarr = new JsonArray();
        ArrayList<Player> newofflinePlayers = player3.findOfflinePlayers();
        for (Player offplayer : newofflinePlayers) {
            JsonObject playerJson = new JsonObject();
            playerJson.addProperty("username", offplayer.getUsername());
            playerJson.addProperty("id", offplayer.getId());
            playerJson.addProperty("score", offplayer.getScore());
            newofflineplayersjsonarr.add(playerJson);
        }
        responseObject.add("offlineplayers", newofflineplayersjsonarr);
        System.out.println("new offline players" + newofflineplayersjsonarr);


        for (ServerHandler client : clients) {
            if (!client.socket.isClosed()) {
                System.out.println("send for clients about onlines and offlines");
                try {
                    client.dataOutputStream.writeUTF(responseObject.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("don't send to closed client ");
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
        return game;
    }

    public Game createGame(int id) {
        Game game = new Game();
        return game.create(id);
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

    public void pauseGame(JsonObject msg) {
        int gameID = msg.get("game_id").getAsInt();
        new Game().pauseGame(gameID);
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

    public void leaveNetwork(ServerHandler serverHandler) {
        clients.remove(serverHandler);
        System.out.println("left chat");
    }

    public void close(DataInputStream reader, DataOutputStream writer) {
        //so need to give client feed back on his own gui that server is down
        //and also send to all clients that server is closed
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("type", "server_closed");
        System.out.println("Current players connected:  " + players.size());
        if (players.size() > 0) {
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
        running = false;
        try {
            if (reader != null)
                reader.close();
            if (writer != null)
                writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsgToAll(JsonObject responseObject) {
        clients.forEach(client -> {
            try {
                client.dataOutputStream.writeUTF(responseObject.toString());
            } catch (IOException e) {
                System.out.println("client closed ");
                e.printStackTrace();
            }
        });
    }
}
