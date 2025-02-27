package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import java.util.HashSet;

public class MemoryGameDAO implements GameDAO {

    private int nextID = 1;
    private final HashSet<GameData> gameDataSet = new HashSet<>();

    public GameData create(String gameName) {
        ChessGame chessGame = new ChessGame();
        GameData gameData = new GameData(nextID++, null, null, gameName, chessGame);
        gameDataSet.add(gameData);
        return gameData;
    }

    public GameData getGame(int gameID) {
        for (GameData gameData : gameDataSet) {
            if (gameData.gameID() == gameID) {
                return gameData;
            }
        }
        return null;
    }

    public void updateGame(GameData gameData, AuthData authData, ChessGame.TeamColor playerColor) {
        GameData newGameData;
        if (playerColor == ChessGame.TeamColor.WHITE) {
            newGameData = gameData.setWhiteUsername(authData.username());
        } else {
            newGameData = gameData.setBlackUsername(authData.username());
        }
        gameDataSet.remove(gameData);
        gameDataSet.add(newGameData);
    }

    public listGames() {

    }

    public void clearGame() {
        gameDataSet.clear();
    }
}
