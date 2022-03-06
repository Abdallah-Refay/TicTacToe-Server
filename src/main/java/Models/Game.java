package Models;

import java.sql.*;

public class Game {
    private int id;//game id
    private String winner; // user name of winner
    private boolean IsFinished;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public Game create() {
        ConnectDB connectDB = new ConnectDB();
        Game game = new Game();
        String sql = "insert into game (winner) values (null)";//creating new game o winner till now
        try (Connection con = connectDB.getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate(sql);
            game.setId(game.getLatestGameID());

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return game;
    }

    public Game findByID(int id) {
        ConnectDB connectDB = new ConnectDB();
        String sql = "select * from game where game_id = ?";
        Game game = null;
        try (Connection con = connectDB.getConnection(); PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            game = new Game();
            while (rs.next()) {
                game.setId(rs.getInt(1));
                game.setWinner(rs.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return game;
    }

    public int getLatestGameID() {
        ConnectDB connectDB = new ConnectDB();
        String sql = "select count(game_id) from game";
        int latestGameID = 0;
        try (Connection con = connectDB.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                latestGameID = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return latestGameID;
    }

    public void finishGame(int id, String winnerUsername) {
        Game game = new Game();
        game = game.findByID(id);
        game.setWinner(winnerUsername);
        game.save();
    }

    public void save() {
        ConnectDB connectDB = new ConnectDB();
        String sql = "update game set winner = ? where game_id = ?";
        try (Connection con = connectDB.getConnection(); PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, this.winner);
            st.setInt(2, this.id);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Game create(int id) {
        ConnectDB connectDB = new ConnectDB();
        Game game = new Game();
        String sql = "insert into game (game_id ) values (?) ";
        try (Connection con = connectDB.getConnection(); PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, id);
            st.execute();
            game.setId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return game;
    }

    public void pauseGame(int id) {
        Game game = new Game();
        game = game.findByID(id);
        game.setGameState(false);
        game.savePaused();
    }

    public void setGameState(boolean test) {
        IsFinished = test;
    }

    public boolean getGameState(int gameId) {
        ConnectDB connectDB = new ConnectDB();
        String sql = "select * from game where game_id = ? and IsFinished= false";
        Game game = null;
        try (Connection con = connectDB.getConnection(); PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, gameId);
            ResultSet rs = st.executeQuery();
            game = new Game();
            while (rs.next()) {
                game.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return game != null;
    }

    public void savePaused() {
        ConnectDB connectDB = new ConnectDB();
        String sql2 = "update game set IsFinished = false  where game_id = ?";
        try (Connection con = connectDB.getConnection(); PreparedStatement st = con.prepareStatement(sql2)) {
            //st.setString(1, this.winner);
            st.setInt(1, this.id);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}