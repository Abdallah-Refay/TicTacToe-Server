package Models;

public class PausedGames {
    public int getGameid() {
        return gameid;
    }
    public void setGameid(int gameid) {
        this.gameid = gameid;
    }


    public int gameid ;
    public String opponent ;

    public String getOpponent() {
        return opponent;
    }

    public void setOpponent(String opponent) {
        this.opponent = opponent;
    }

    public int getOpponentId() {
        return opponentId;
    }

    public void setOpponentId(int opponentId) {
        this.opponentId = opponentId;
    }

    public int opponentId ;

}
