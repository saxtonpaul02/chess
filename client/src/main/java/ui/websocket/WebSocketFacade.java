package ui.websocket;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

import static chess.ChessPiece.PieceType.*;

public class WebSocketFacade extends Endpoint {

    Session session;
    ServerMessageObserver messageObserver;

    public WebSocketFacade(String url, ServerMessageObserver messageObserver) throws Exception {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.messageObserver = messageObserver;
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                    switch (serverMessage.getServerMessageType()) {
                        case LOAD_GAME -> {
                            LoadGameMessage loadGameMessage = new Gson().fromJson(message, LoadGameMessage.class);
                            messageObserver.loadGame(loadGameMessage);
                        } case NOTIFICATION -> {
                            NotificationMessage notificationMessage = new Gson().fromJson(message, NotificationMessage.class);
                            messageObserver.notify(notificationMessage);
                        } case ERROR -> {
                            ErrorMessage errorMessage = new Gson().fromJson(message, ErrorMessage.class);
                            messageObserver.notifyError(errorMessage);
                        }
                    }
                }
            });
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void connect(String authToken, String param) throws Exception {
        int gameID = Integer.parseInt(param);
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new Exception("Error: Unable to connect to game");
        }
    }

    public void resignGame(String authToken, String param) throws Exception {
        int gameID = Integer.parseInt(param);
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new Exception("Error: Unable to resign the game");
        }
    }

    public void makeMove(int gameID, String... params) throws Exception {
        try {
            String moveString = params[0] + params[1];
            ChessPosition startPosition = null;
            ChessPosition endPosition = null;
            ChessPiece.PieceType promotionPiece = null;
            int row = 0;
            int col = 0;
            int counter = 1;
            for (char c : moveString.toCharArray()) {
                if (counter % 2 == 1) {
                    row = switch (c) {
                        case 'a' -> 1;
                        case 'b' -> 2;
                        case 'c' -> 3;
                        case 'd' -> 4;
                        case 'e' -> 5;
                        case 'f' -> 6;
                        case 'g' -> 7;
                        case 'h' -> 8;
                        default -> throw new Exception("Error: invalid move entry. Please try again.");
                    };
                } else {
                    if (c >= '1' && c <= '8') {
                        col = Character.getNumericValue(c);
                    } else {
                        throw new Exception("Error: invalid move entry. Please try again.");
                    }
                    if (counter == 2) {
                        startPosition = new ChessPosition(row, col);
                    } else {
                        endPosition = new ChessPosition(row, col);
                    }
                }
                counter++;
            }
            if (params.length == 4) {
                promotionPiece = convertStringToPiece(params[3]);
            }
            ChessMove move = new ChessMove(startPosition, endPosition, promotionPiece);
            MakeMoveCommand command = new MakeMoveCommand(params[2], gameID, move);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new Exception("Error: Unable to make move.");
        }
    }

    public void leaveGame(String authToken, String param) throws Exception {
        int gameID = Integer.parseInt(param);
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new Exception("Error: Unable to leave the game");
        }
    }

    private ChessPiece.PieceType convertStringToPiece(String pieceString) throws Exception {
        return switch(pieceString) {
            case "knight", "n" -> KNIGHT;
            case "bishop", "b" -> BISHOP;
            case "rook", "r" -> ROOK;
            case "queen", "q" -> QUEEN;
            default -> throw new Exception("Error: Invalid promotion piece.");
        };
    }
}
