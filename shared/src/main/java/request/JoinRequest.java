package request;

import chess.ChessGame;

public record JoinRequest(String authToken, ChessGame.TeamColor playerColor, int gameID) {
    public JoinRequest setAuthToken(String newAuthToken) {
        return new JoinRequest(newAuthToken, playerColor, gameID);
    }
}
