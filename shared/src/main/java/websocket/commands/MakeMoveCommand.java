package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand {

    private final UserGameCommand.CommandType type;
    private final String authToken;
    private final int gameID;
    private final ChessMove move;

    public MakeMoveCommand(UserGameCommand.CommandType type, String authToken, Integer gameID, ChessMove move) {
        this.type = type;
        this.authToken = authToken;
        this.gameID = gameID;
        this.move = move;
    }

    public UserGameCommand.CommandType getCommandType() { return this.type; }

    public String getAuthToken() { return this.authToken; }

    public int getGameID() { return this.gameID; }

    public ChessMove getMove() { return this.move; }
}
