package model;

import chess.ChessGame;

public record GameData(int gameID,
                       String whiteUsername,
                       String blackUsername,
                       String gameName,
                       ChessGame game) {

    public GameData setWhiteUsername(String newUsername) {
        return new GameData(gameID, newUsername, blackUsername, gameName, game);
    }

    public GameData setBlackUsername(String newUsername) {
        return new GameData(gameID, whiteUsername, newUsername, gameName, game);
    }
}
