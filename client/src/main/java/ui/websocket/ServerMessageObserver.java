package ui.websocket;

import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

public interface ServerMessageObserver {
    void loadGame(LoadGameMessage message);
    void notify(NotificationMessage message);
    void notifyError(ErrorMessage message);
}
