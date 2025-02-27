package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;

public interface GameDAO {

    GameData create(String gameName) throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    void updateGame(GameData gameData, AuthData authData, ChessGame.TeamColor playerColor) throws DataAccessException;

    listGames() throws DataAccessException;

    void clearGame() throws DataAccessException;
}
