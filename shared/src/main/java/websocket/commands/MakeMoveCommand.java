package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand {

    private final ChessMove move;

    public MakeMoveCommand(UserGameCommand command) {
        this.move = move;
    }

    public ChessMove getMove() { return move; }
}
