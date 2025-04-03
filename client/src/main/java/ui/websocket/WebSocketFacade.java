package ui.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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
                    ServerMessageObserver.notify(serverMessage);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new Exception("Error setting up websocket connection");
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void playGame(String authToken, String...params) throws Exception {
        int gameID = Integer.parseInt(params[0]);
        ChessGame.TeamColor teamColor;
        if (params[1].equals("white")) {
            teamColor = ChessGame.TeamColor.WHITE;
        } else if (params[1].equals("black")) {
            teamColor = ChessGame.TeamColor.BLACK;
        } else {
            throw new Exception("Error: Invalid team color.");
        }
    }

    public void observeGame(String authToken, String...params) throws Exception {
        int gameID = Integer.parseInt(params[0]);
    }
}
