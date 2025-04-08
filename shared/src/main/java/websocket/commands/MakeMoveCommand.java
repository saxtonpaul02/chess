package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand {

    private final UserGameCommand.CommandType type;
    private final String authToken;
    private final int gameID;
    private final String move;

    public MakeMoveCommand(UserGameCommand.CommandType type, String authToken, Integer gameID, String move) {
        this.type = type;
        this.authToken = authToken;
        this.gameID = gameID;
        this.move = move;
    }

    public UserGameCommand.CommandType getCommandType() { return this.type; }

    public String getAuthToken() { return this.authToken; }

    public int getGameID() { return this.gameID; }

    public String getMove() { return this.move; }
}
