package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
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
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
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
            UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
            String username = getUsername(command.getAuthToken());
            saveSession(command.getGameID(), session);
            switch (command.getCommandType()) {
                case CONNECT -> connect(session, username, command);
                case LEAVE -> leaveGame(session, username, command);
                case RESIGN -> resignGame(session, username, command);
                case MAKE_MOVE -> {
                    MakeMoveCommand moveCommand = new Gson().fromJson(message, MakeMoveCommand.class);
                    makeMove(session, username, moveCommand);
                }
            }
        } catch (Exception ex) {
            ErrorMessage errorMessage = new ErrorMessage(ex.getMessage());
            sendErrorMessage(errorMessage, session);
        }
    }

    private void connect(Session session, String username, UserGameCommand command) throws IOException {
        try {
            int gameID = command.getGameID();
            webSocketSessions.add(gameID, session);
            GameData gameData = gameService.getGame(gameID);
            LoadGameMessage message1 = new LoadGameMessage(gameData);
            sendLoadGameMessage(message1, session);
            broadcastLoadGameMessage(gameID, message1, session);
            NotificationMessage message2;
            if (username.equals(gameData.whiteUsername())) {
                message2 = new NotificationMessage(String.format("%s has joined the game as white.", username));
            } else if (username.equals(gameData.blackUsername())) {
                message2 = new NotificationMessage(String.format("%s has joined the game as black.", username));
            } else {
                message2 = new NotificationMessage(String.format("%s has joined the game as an observer.", username));
            }
            broadcastNotificationMessage(gameID, message2, session);
        } catch (Exception ex) {
            ErrorMessage message = new ErrorMessage(ex.getMessage());
            sendErrorMessage(message, session);
        }
    }

    private void makeMove(Session session, String username, MakeMoveCommand command) throws InvalidMoveException, IOException {
        try {
            int gameID = command.getGameID();
            GameData gameData = gameService.getGame(command.getGameID());
            ChessGame game = gameData.game();
            ChessMove move = command.getMove();
            game.makeMove(move);
            LoadGameMessage message1 = new LoadGameMessage(gameData);
            sendLoadGameMessage(message1, session);
            broadcastLoadGameMessage(gameID, message1, session);
            NotificationMessage message2 =
                    new NotificationMessage(String.format("%s has made move %s", username, moveToString(move)));
            broadcastNotificationMessage(gameID, message2, session);
            if (isInCheckmate(gameData)) {
                String opponent = opponentUsername(gameData);
                NotificationMessage message3 =
                        new NotificationMessage(String.format("%s has been checkmated", opponent));
                broadcastNotificationMessage(gameID, message3, session);
            } else if (isinCheck(gameData)) {
                String opponent = opponentUsername(gameData);
                NotificationMessage message4 =
                        new NotificationMessage(String.format("%s is in check", opponent));
                broadcastNotificationMessage(gameID, message4, session);
            } else if (isInStalemate(gameData)) {

            }
        } catch (Exception ex) {
            ErrorMessage message = new ErrorMessage(ex.getMessage());
            sendErrorMessage(message, session);
        }
    }

    private void resignGame(Session session, String username, UserGameCommand command) {

    }

    private void leaveGame(Session session, String username, UserGameCommand command) {

    }

    private String getUsername(String authToken) throws DataAccessException {
        return userService.getUsername(authToken);
    }

    private String opponentUsername(GameData gameData) {
        ChessGame.TeamColor teamTurn = gameData.game().getTeamTurn();
        if (teamTurn == ChessGame.TeamColor.WHITE) {
            return gameData.whiteUsername();
        } else {
            return gameData.blackUsername();
        }
    }

    private boolean isInCheckmate(GameData gameData) {
        ChessGame.TeamColor teamTurn = gameData.game().getTeamTurn();
        return gameData.game().isInCheckmate(teamTurn);
    }

    private boolean isinCheck(GameData gameData) {
        ChessGame.TeamColor teamTurn = gameData.game().getTeamTurn();
        return gameData.game().isInCheck(teamTurn);
    }

    private boolean isInStalemate(GameData gameData) {
        ChessGame.TeamColor teamTurn = gameData.game().getTeamTurn();
        return gameData.game().isInStalemate(teamTurn);
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

    private void sendLoadGameMessage(LoadGameMessage message, Session session) throws IOException {
        session.getRemote().sendString(new Gson().toJson(message));
    }

    private void sendErrorMessage(ErrorMessage message, Session session) throws IOException {
        session.getRemote().sendString(new Gson().toJson(message));
    }

    private void broadcastNotificationMessage(int gameID, NotificationMessage message, Session notThisSession) throws IOException {
        for (Session session : webSocketSessions.get(gameID)) {
            if (session != notThisSession) {
                session.getRemote().sendString(new Gson().toJson(message));
            }
        }
    }

    private void broadcastLoadGameMessage(int gameID, LoadGameMessage message, Session notThisSession) throws IOException {
        for (Session session : webSocketSessions.get(gameID)) {
            if (session != notThisSession) {
                session.getRemote().sendString(new Gson().toJson(message));
            }
        }
    }
}
