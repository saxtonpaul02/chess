package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand {

    private final ChessMove move;

    public MakeMoveCommand(UserGameCommand.CommandType commandType, String authToken, Integer gameID, ChessMove move) {
        this.move = move;
    }

    public ChessMove getMove() { return move; }
}
