package ui.websocket;

import chess.ChessGame;
import websocket.messages.ServerMessage;

public interface ServerMessageObserver {
    static void updateGame(ChessGame game) {}

    static void printMessage(ServerMessage message) {}
}
