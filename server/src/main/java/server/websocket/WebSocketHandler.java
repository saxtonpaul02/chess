package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.MakeMoveCommand;
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
            command.getAuthToken()
            saveSession(command.getGameID(), session);
            switch (command.getCommandType()) {
                case CONNECT -> connect(command);
                case MAKE_MOVE -> makeMove(command);
                case LEAVE -> leaveGame(command);
                case RESIGN -> resignGame(command);
            }
        } catch () {}
    }

    private void connect(UserGameCommand command) {

    }

    private void makeMove(UserGameCommand command) {

    }

    private void leaveGame(UserGameCommand command) {

    }

    private void resignGame(UserGameCommand command) {

    }

    private void saveSession(int gameID, Session session) {
        if (webSocketSessions.get(gameID) == null) {
            webSocketSessions.add(gameID, session);
        }
    }

    public void sendMessage(Session session, String message) {

    }

    public void broadcastMessage(String gameID, String message, Session notThisSession) {

    }
}
