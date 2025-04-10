package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import request.CreateRequest;
import request.JoinRequest;
import result.CreateResult;
import result.ListResult;

import java.util.Collection;

public class GameService {

    private final AuthDAO authDao;
    private final GameDAO gameDao;

    public GameService(AuthDAO authDao, GameDAO gameDao) {
        this.authDao = authDao;
        this.gameDao = gameDao;
    }

    public CreateResult create(CreateRequest createRequest) throws DataAccessException {
        String authToken = createRequest.authToken();
        AuthData authData = authDao.getAuth(authToken);
        if (authData != null) {
            String gameName = createRequest.gameName();
            GameData gameData = gameDao.create(gameName);
            return new CreateResult(gameData.gameID());
        } else {
            return null;
        }
    }

    public int join(JoinRequest joinRequest) throws DataAccessException {
        String authToken = joinRequest.authToken();
        AuthData authData = authDao.getAuth(authToken);
        if (authData == null) {
            return 0;
        } else {
            GameData gameData = gameDao.getGame(joinRequest.gameID());
            ChessGame.TeamColor playerColor = joinRequest.playerColor();
            if (gameData == null) {
                return 3;
            } else if (playerColor == ChessGame.TeamColor.WHITE && gameData.whiteUsername() != null) {
                return 2;
            } else if (playerColor == ChessGame.TeamColor.BLACK && gameData.blackUsername() != null) {
                return 2;
            } else if (playerColor != ChessGame.TeamColor.WHITE && playerColor != ChessGame.TeamColor.BLACK) {
                return 4;
            } else {
                gameDao.updateGame(gameData, authData, joinRequest.playerColor());
                return 1;
            }
        }
    }

    public Collection<ListResult> list(String authToken) throws DataAccessException {
        AuthData authData = authDao.getAuth(authToken);
        if (authData == null) {
            return null;
        } else {
            return gameDao.listGames();
        }
    }

    public GameData getGame(int gameID) throws DataAccessException {
        return gameDao.getGame(gameID);
    }

    public void updateGame(GameData gameData, String authToken, ChessGame.TeamColor playerColor) throws DataAccessException {
        AuthData authData = null;
        if (authToken != null) authData = authDao.getAuth(authToken);
        gameDao.updateGame(gameData, authData, playerColor);
    }
}
