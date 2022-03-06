package Models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class GameRecord {
    private int gameID;
    private int playerID;
    private int stepNumber;
    private int move;
    private int position;

    public static ArrayList<PausedGames> findPausedGames(int plyer1Id) {
        ConnectDB connectDB = new ConnectDB();
        ArrayList<PausedGames> pausedGames = new ArrayList<>();
        PausedGames game;
        Player player;
        //String sql1 = "SELECT game_id  FROM  game WHERE IsFinished = false ";
        String sql1 = "SELECT  g.game_id" +
                " FROM moves as m , game as g" +
                " where  m.game_id = g.game_id and g.IsFinished = false" +
                " Group by g.game_id ,m.player_id" +
                " having m.player_id =?";
        try (Connection con = connectDB.getConnection(); PreparedStatement st = con.prepareStatement(sql1)) {
            st.setInt(1, plyer1Id);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                game = new PausedGames();
                game.setGameid(rs.getInt(1));
                pausedGames.add(game);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (PausedGames pausedGame : pausedGames) {
            int id = pausedGame.gameid;
            //String sql2 = "SELECT player_id FROM moves where game_id = ? group by player_id;";
            String sql2 = " SELECT player_id " +
                    " FROM moves " +
                    " where  game_id = ? and player_id != ? " +
                    " Group by player_id";
            try (Connection con = connectDB.getConnection(); PreparedStatement st = con.prepareStatement(sql2)) {
                st.setInt(1, id);
                st.setInt(2, plyer1Id);
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    player = new Player();
                    pausedGame.setOpponentId(rs.getInt(1));
                    pausedGame.setOpponent(player.findID(rs.getInt(1)).getUsername());
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return pausedGames;
    }

    public int getGameID() {
        return gameID;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public int getPlayerID() {
        return playerID;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public int getMove() {
        return move;
    }

    public void setMove(int move) {
        this.move = move;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public GameRecord create(int gameID, int playerID, int move, int position) {
        ConnectDB connectDB = new ConnectDB();
        GameRecord gameRecord = new GameRecord();

        String sql = "insert into moves (game_id, player_id, sign, pos) values (?, ?, ?, ?)";

        try (Connection con = connectDB.getConnection(); PreparedStatement st = con.prepareStatement(sql)) {

            //st.setInt(1, stepNumber);
            st.setInt(1, gameID);
            st.setInt(2, playerID);
            st.setInt(3, move);
            st.setInt(4, position);

            st.executeUpdate();
            gameRecord.setGameID(gameID);
            gameRecord.setStepNumber(stepNumber);
            gameRecord.setMove(move);
            gameRecord.setPlayerID(playerID);
            gameRecord.setPosition(position);

        } catch (SQLException e) {
            e.printStackTrace();
            gameRecord = null;
        }

        return gameRecord;
    }

    public ArrayList<GameRecord> findByGameID(int id) {

        ConnectDB connectDB = new ConnectDB();

        String sql = "select * from moves where game_id = ?";

        GameRecord gameRecord = null;

        ArrayList<GameRecord> moves = new ArrayList<>();

        try (Connection con = connectDB.getConnection(); PreparedStatement st = con.prepareStatement(sql)) {

            st.setInt(1, id);
            ResultSet rs = st.executeQuery();

            gameRecord = new GameRecord();

            while (rs.next()) {
                gameRecord = new GameRecord();
                gameRecord.setGameID(rs.getInt(2));
                gameRecord.setPlayerID(rs.getInt(3));
                gameRecord.setMove(rs.getInt(4));
                gameRecord.setPosition(rs.getInt(5));
                moves.add(gameRecord);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return moves;
    }


}
