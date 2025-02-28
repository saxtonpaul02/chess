package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import result.ListResult;
import java.util.Collection;

public interface GameDAO {

    GameData create(String gameName) throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    void updateGame(GameData gameData, AuthData authData, ChessGame.TeamColor playerColor) throws DataAccessException;

    Collection<ListResult> listGames() throws DataAccessException;

    void clearGame() throws DataAccessException;
}
