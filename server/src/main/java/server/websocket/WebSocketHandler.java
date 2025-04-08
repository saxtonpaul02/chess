package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.DataAccessException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import service.UserService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private final WebSocketSessions webSocketSessions = new WebSocketSessions();
    private final GameService gameService;
    private final UserService userService;

    public WebSocketHandler(GameService gameService, UserService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    @OnWebSocketError
    public void onError(Throwable throwable) {
        System.out.println("Error: Invalid entry");
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        try {
            String username;
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            if (json.get("move").getAsString() == null) {
                UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
                username = getUsername(command.getAuthToken());
                saveSession(command.getGameID(), session);
                switch (command.getCommandType()) {
                    case CONNECT -> connect(session, username, command);
                    case LEAVE -> leaveGame(session, username, command);
                    case RESIGN -> resignGame(session, username, command);
                }
            } else {
                MakeMoveCommand command = new Gson().fromJson(message, MakeMoveCommand.class);
                username = getUsername(command.getAuthToken());
                makeMove(session, username, command);
            }
        } catch (Exception ex) {
            ServerMessage serverMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, ex.getMessage());
            sendMessage(serverMessage, session);
        }
    }

    private void connect(Session session, String username, UserGameCommand command) throws IOException {
        try {
            int gameID = command.getGameID();
            webSocketSessions.add(gameID, session);
            GameData gameData = gameService.getGame(gameID);
            ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, "");
            sendMessage(message, session);
            broadcastMessage(gameID, message, session);
            if (username.equals(gameData.whiteUsername())) {
                message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                        String.format("%s has joined the game as white.", username));
            } else if (username.equals(gameData.blackUsername())) {
                message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                        String.format("%s has joined the game as black.", username));
            } else {
                message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                        String.format("%s has joined the game as an observer.", username));
            }
            broadcastMessage(gameID, message, session);
        } catch (Exception ex) {
            ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.ERROR, ex.getMessage());
            sendMessage(message, session);
        }
    }

    private void makeMove(Session session, String username, MakeMoveCommand command) throws InvalidMoveException, IOException {
        try {
            int gameID = command.getGameID();
            GameData gameData = gameService.getGame(command.getGameID());
            ChessGame game = gameData.game();
            ChessMove move = command.getMove();
            game.makeMove(move);
            ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, "");
            sendMessage(message, session);
            broadcastMessage(gameID, message, session);
            message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    String.format("%s has made move %s", username, moveToString(move)));
            broadcastMessage(gameID, message, session);
        } catch (Exception ex) {
            ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.ERROR, ex.getMessage());
            sendMessage(message, session);
        }
    }

    private void resignGame(Session session, String username, UserGameCommand command) {

    }

    private void leaveGame(Session session, String username, UserGameCommand command) {

    }

    private String getUsername(String authToken) throws DataAccessException {
        return userService.getUsername(authToken);
    }

    private void saveSession(int gameID, Session session) {
        if (webSocketSessions.get(gameID) == null) {
            webSocketSessions.add(gameID, session);
        }
    }

    private String moveToString(ChessMove move) {
        return columnToString(move.getStartPosition().getColumn()) +
                String.valueOf(move.getStartPosition().getRow()) +
                columnToString(move.getEndPosition().getColumn()) +
                String.valueOf(move.getEndPosition().getRow()) +
                switch (move.getPromotionPiece()) {
                    case KNIGHT -> " promote to knight";
                    case BISHOP -> " promote to bishop";
                    case ROOK -> " promote to rook";
                    case QUEEN -> " promote to queen";
                    default -> "";
                };
    }

    private String columnToString(int col) {
        return switch (col) {
            case 1 -> "a";
            case 2 -> "b";
            case 3 -> "c";
            case 4 -> "d";
            case 5 -> "e";
            case 6 -> "f";
            case 7 -> "g";
            case 8 -> "h";
            default -> "";
        };
    }

    private void sendMessage(ServerMessage message, Session session) throws IOException {
        session.getRemote().sendString(message.getMessage());
    }

    private void broadcastMessage(int gameID, ServerMessage message, Session notThisSession) throws IOException {
        for (Session session : webSocketSessions.get(gameID)) {
            if (session != notThisSession) {
                session.getRemote().sendString(message.getMessage());
            }
        }
    }
}
