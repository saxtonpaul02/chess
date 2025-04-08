package ui.websocket;

import websocket.messages.ServerMessage;

public interface ServerMessageObserver {
    static void notify(ServerMessage message) {}
}
