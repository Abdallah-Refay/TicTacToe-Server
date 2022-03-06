package Models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private int player_id;
    private String username;
    private String hashedPassword;
    private int wins;
    private int losses;
    private int score;
    private boolean online;

    public static ObservableList<Player> getAllUsers() {
        //creating list of all players on system
        ObservableList<Player> list = FXCollections.observableArrayList();
        // get user bring observable list
        Player player1 = new Player();
        List<Player> players = player1.findAllPlayers();
        for (Player player : players) {
            list.add(player);
        }
        return list;
    }

    public int getId() {
        return player_id;
    }

    public void setId(int id) {
        this.player_id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    // check if player username exist
    public boolean checkUserName(String username) {
        ConnectDB connectDB = new ConnectDB();
        String sql = "select username from players where username=? ";
        try (Connection con = connectDB.getConnection(); PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, username);
            return st.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Create new player -Sign up-
    public boolean signUp(String username, String hashedPassword) {
        boolean isAdded = false;        // check on added or not in database
        if (!(checkUserName(username))) {
            ConnectDB connectDB = new ConnectDB();
            String sql = "insert into players (username, hashedPassword) values (?, ?)";
            try (Connection con = connectDB.getConnection(); PreparedStatement st = con.prepareStatement(sql)) {
                st.setString(1, username);
                st.setString(2, hashedPassword);
                st.executeUpdate();
                isAdded = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return isAdded;
    }

    //return all player info if founded
    public Player findByUsername(String username) {
        ConnectDB connectDB = new ConnectDB();
        String sql = "select * from players where username = ?";
        Player player = null;
        try (Connection con = connectDB.getConnection(); PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            player = new Player();
            while (rs.next()) {
                player.setId(Integer.parseInt(rs.getString(1)));        //username
                player.setUsername(rs.getString(2));        //username
                player.setHashedPassword(rs.getString(3)); //pw
                player.setWins(rs.getInt(4));              //wins
                player.setLosses(rs.getInt(5));            //losses
                player.setScore(rs.getInt(6));             //score
                player.setOnline(rs.getBoolean(7));        //online
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return player;
    }

    public Player findID(int id) {
        ConnectDB connectDB = new ConnectDB();
        String sql = "select * from players where player_id = ?";
        Player player = null;
        try (Connection con = connectDB.getConnection(); PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, String.valueOf(id));
            ResultSet rs = st.executeQuery();
            player = new Player();
            while (rs.next()) {
                player.setId(Integer.parseInt(rs.getString(1)));        //id
                player.setUsername(rs.getString(2));        //username
                player.setHashedPassword(rs.getString(3)); //pw
                player.setWins(rs.getInt(4));              //wins
                player.setLosses(rs.getInt(5));            //losses
                player.setScore(rs.getInt(6));             //score
                player.setOnline(rs.getBoolean(7));        //online
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return player;
    }

    //login by username and password
    public Player login(String username, String hashedPassword) {
        Player player = new Player();
        player = player.findByUsername(this.username);
        if (player != null) {
            if (this.hashedPassword.equals(player.getHashedPassword())) {
                player.setOnline(true);
                player.save(player);
            } else {
                player = null;
            }
        }
        return player;
    }

    //logout from application so offline state
    public void logout(String username) {
        Player player = new Player();
        player = player.findByUsername(username);
        player.setOnline(false);
        player.save(player);
    }

    public void wins(int palyer_id) {
        ConnectDB connectDB = new ConnectDB();
        String sql = "update players set wins = wins + 1, score = score + 10 where player_id = ?";
        try (Connection con = connectDB.getConnection(); PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, palyer_id);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loses(int palyer_id) {
        ConnectDB connectDB = new ConnectDB();
        String sql = "update players set losses = losses + 1, score = score - 10 where player_id = ?";
        try (Connection con = connectDB.getConnection(); PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, palyer_id);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void save(Player player) {
        ConnectDB connectDB = new ConnectDB();
        String sql = "update players set  username = ?, hashedPassword = ?, wins = ?, losses = ?, score = ?, online = ? where player_id = ?";
        try (Connection con = connectDB.getConnection(); PreparedStatement st = con.prepareStatement(sql)) {
            // st.setInt(1, player.getId());
            st.setString(1, player.getUsername());
            st.setString(2, player.getHashedPassword());
            st.setInt(3, player.getWins());
            st.setInt(4, player.getLosses());
            st.setInt(5, player.getScore());
            st.setBoolean(6, player.isOnline());
            st.setInt(7, player.getId());
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //query on all players and add in arraylist which is online
    public ArrayList<Player> findOnlinePlayers() {
        ConnectDB connectDB = new ConnectDB();
        String sql = "select * from players where online = true";
        ArrayList<Player> onlinePlayers = new ArrayList<>();
        try (Connection con = connectDB.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Player player = new Player();
                player.setId(rs.getInt(1));                //id
                player.setUsername(rs.getString(2));        //username
                player.setHashedPassword(rs.getString(3)); //pw
                player.setWins(rs.getInt(4));              //wins
                player.setLosses(rs.getInt(5));            //losses
                player.setScore(rs.getInt(6));            //scores
                player.setOnline(rs.getBoolean(7));        //online
                //add online player
                onlinePlayers.add(player);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return onlinePlayers;
    }

    //query on all players and add in arraylist which is offline
    public ArrayList<Player> findOfflinePlayers() {
        ConnectDB connectDB = new ConnectDB();
        String sql = "select * from players where online = false";
        ArrayList<Player> offlinePlayers = new ArrayList<>();
        try (Connection con = connectDB.getConnection(); Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Player player = new Player();
                player.setId(rs.getInt(1));                //id
                player.setUsername(rs.getString(2));        //username
                player.setHashedPassword(rs.getString(3)); //pw
                player.setWins(rs.getInt(4));              //wins
                player.setLosses(rs.getInt(5));            //losses
                player.setScore(rs.getInt(6));            //scores
                player.setOnline(rs.getBoolean(7));        //offline
                //add offline player
                offlinePlayers.add(player);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return offlinePlayers;
    }

    //query on all players and add in arraylist which is online & offline
    public ArrayList<Player> findAllPlayers() {
        ConnectDB connectDB = new ConnectDB();
        String sql = "select * from players order by score ";
        ArrayList<Player> allPlayers = new ArrayList<>();
        try (Connection con = connectDB.getConnection(); Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Player player = new Player();
                player.setId(rs.getInt(1));                //id
                player.setUsername(rs.getString(2));        //username
                player.setHashedPassword(rs.getString(3)); //pw
                player.setWins(rs.getInt(4));              //wins
                player.setLosses(rs.getInt(5));            //losses
                player.setScore(rs.getInt(6));            //scores
                player.setOnline(rs.getBoolean(7));        //online or offline
                allPlayers.add(player);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return allPlayers;


    }
}
