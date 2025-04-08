package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class WebSocketHandler {

    private final WebSocketSessions webSocketSessions = new WebSocketSessions();

    @OnWebSocketError
    public void onError(Throwable throwable) {
        System.out.println("Error: Invalid entry");
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {

    }

    public void sendMessage(Session session, String message) {

    }

    public void broadcastMessage(String gameID, String message, Session notThisSession) {
        
    }
}
