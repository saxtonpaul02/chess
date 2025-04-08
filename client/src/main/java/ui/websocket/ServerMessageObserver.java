package ui.websocket;

import chess.ChessGame;
import websocket.messages.ServerMessage;

public interface ServerMessageObserver {
    void notify(ServerMessage message);
}
