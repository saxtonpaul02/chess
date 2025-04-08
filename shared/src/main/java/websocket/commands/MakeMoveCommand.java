package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand {

    private final String move;

    public MakeMoveCommand(UserGameCommand command, String move) {
        this.move = move;
    }

    public String getMove() { return this.move; }
}
