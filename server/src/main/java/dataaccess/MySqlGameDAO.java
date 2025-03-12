package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import result.ListResult;
import java.util.Collection;

public class MySqlGameDAO implements GameDAO {

    public MySqlGameDAO() {
        configureDatabase();
    }

    public GameData create(String gameName) {
        return null;
    }

    public GameData getGame(int gameID) {
        return null;
    }

    public void updateGame(GameData gameData, AuthData authData, ChessGame.TeamColor playerColor) {
    }

    public Collection<ListResult> listGames() {
        return null;
    }

    public void clearGame() {
    }

    private void configureDatabase() {

    }
}
