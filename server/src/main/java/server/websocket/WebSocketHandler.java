package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;

@WebSocket
public class WebSocketHandler {

    private final WebSocketSessions webSocketSessions = new WebSocketSessions();

    @OnWebSocketError
    public void onError(Throwable throwable) {
        System.out.println("Error: Invalid entry");
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
            String username = getUsername(command.getAuthToken());
            saveSession(command.getGameID(), session);
            switch (command.getCommandType()) {
                case CONNECT -> connect(session, username, command);
                case MAKE_MOVE -> makeMove(session, username, command);
                case LEAVE -> leaveGame(session, username, command);
                case RESIGN -> resignGame(session, username, command);
            }
        } catch (Exception ex) {

        }
    }

    private void connect(Session session, String username, UserGameCommand command) {

    }

    private void makeMove(Session session, String username, UserGameCommand command) {

    }

    private void leaveGame(Session session, String username, UserGameCommand command) {

    }

    private void resignGame(Session session, String username, UserGameCommand command) {

    }

    private String getUsername(String authToken) {

    }

    private void saveSession(int gameID, Session session) {
        if (webSocketSessions.get(gameID) == null) {
            webSocketSessions.add(gameID, session);
        }
    }

    private void sendMessage(Session session, String message) {

    }

    private void broadcastMessage(String gameID, String message, Session notThisSession) {

    }
}
