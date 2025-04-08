package ui.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import request.*;
import result.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;

public class ServerFacade {

    private final String serverUrl;
    private final ServerMessageObserver messageObserver;
    private WebSocketFacade ws;

    public ServerFacade(String serverUrl, ServerMessageObserver messageObserver) {
        this.serverUrl = serverUrl;
        this.messageObserver = messageObserver;
    }

    public String register(String... params) throws Exception {
        try {
            var path = "/user";
            RegisterRequest registerRequest = new RegisterRequest(params[0], params[1], params[2]);
            RegisterResult registerResult = this.makeRequest("POST", path, registerRequest, null, RegisterResult.class);
            return registerResult.authToken();
        } catch (Exception ex) {
            throw new Exception("Error registering, please try again. Enter help if assistance is needed.");
        }
    }

    public String login(String... params) throws Exception {
        try {
            var path = "/session";
            LoginRequest loginRequest = new LoginRequest(params[0], params[1]);
            LoginResult loginResult = this.makeRequest("POST", path, loginRequest, null, LoginResult.class);
            return loginResult.authToken();
        } catch (Exception ex) {
            throw new Exception("Error logging in, please try again. Enter help if assistance is needed.");
        }
    }

    public void createGame(String authToken, String... params) throws Exception {
        try {
            var path = "/game";
            CreateRequest createRequest = new CreateRequest(authToken, params[0]);
            this.makeRequest("POST", path, createRequest, authToken, ChessGame.class);
        } catch (Exception ex) {
            throw new Exception("Error creating game, please try again. Enter help if assistance is needed.");
        }
    }

    public ListResult[] listGames(String authToken) throws Exception {
        try {
            var path = "/game";
            record ListResultList(ListResult[] games) {
            }
            ListResultList lrl = this.makeRequest("GET", path, null, authToken, ListResultList.class);
            return lrl.games();
        } catch (Exception ex) {
            throw new Exception("Error listing games, please try again. Enter help if assistance is needed.");
        }
    }

    public void joinGame(String authToken, String... params) throws Exception {
        try {
            var path = "/game";
            ChessGame.TeamColor teamColor;
            if (params[1].equals("white")) {
                teamColor = ChessGame.TeamColor.WHITE;
            } else if (params[1].equals("black")) {
                teamColor = ChessGame.TeamColor.BLACK;
            } else {
                throw new Exception("Error: Invalid team color.");
            }
            JoinRequest joinRequest = new JoinRequest(authToken, teamColor, Integer.parseInt(params[0]));
            this.makeRequest("PUT", path, joinRequest, authToken, ChessGame.class);
            ws = new WebSocketFacade(serverUrl, messageObserver);
            ws.connect(authToken, params[0]);
        } catch (Exception ex) {
            throw new Exception("Error joining game, please try again. Enter help if assistance is needed.");
        }
    }

    public void observeGame(String authToken, String... params) throws Exception {
        try {
            ws = new WebSocketFacade(serverUrl, messageObserver);
            ws.connect(authToken, params[0]);
        } catch (Exception ex) {
            throw new Exception("Error observing game, please try again. Enter help if assistance is needed.");
        }
    }

    public void logout(String authToken) throws Exception {
        try {
            var path = "/session";
            this.makeRequest("DELETE", path, null, authToken, null);
        } catch (Exception ex) {
            throw new Exception("Error logging out, please try again. Enter help if assistance is needed.");
        }
    }

    public void redrawBoard() throws Exception {
        try {
            drawGame();
        } catch (Exception ex) {
            throw new Exception("Error redrawing board, please try again. Enter help if assistance is needed.");
        }
    }

    public void highlightLegalMoves(String... params) throws Exception {
        try {
            drawGame();
        } catch (Exception ex) {
            throw new Exception("Error highlighting legal moves, please try again. Enter help if assistance is needed.");
        }
    }

    public void makeMove(String... params) throws Exception {

    }

    private <T> T makeRequest(String method, String path, Object request, String authToken, Class<T> responseClass) throws Exception {
        URL url = (new URI(serverUrl + path)).toURL();
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod(method);
        if (authToken != null) {
            http.setRequestProperty("Authorization", authToken);
        }
        http.setDoOutput(true);
        writeBody(request, http);
        http.connect();
        return readBody(http, responseClass);
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String requestData = new Gson().toJson(request);
            try (OutputStream requestBody = http.getOutputStream()) {
                requestBody.write(requestData.getBytes());
            }
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream responseBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(responseBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    public static String drawGame(ChessGame game, boolean flip) {
        StringBuilder response = new StringBuilder(" \u2003 ");
        for (int i = 0; i < 10; i++) {
            if (i == 0 || i == 9) {
                response.append(getRowBorder(i, flip));
            } else {
                for (int j = 0; j < 10; j++) {
                    response.append(getLeftColumnBorder(i, j, flip));
                    response.append(getSquareString(i, j, flip));
                    response.append(getPieceString(i, j, flip));
                    response.append(getRightColumnBorder(i, j, flip));
                }
            }
            response.append("\n");
        }
        response.append("\u001B[0m");
        return response.toString();
    }

    private static String getRowBorder(int row, boolean flip) {
        if (!flip) {
            if (row == 0) {
                return "a\u2003 b\u2003 c\u2003 d\u2003 e\u2003 f\u2003 g\u2003 h  \u2003";
            } else {
                return "\u2003  a\u2003 b\u2003 c\u2003 d\u2003 e\u2003 f\u2003 g\u2003 h  \u2003";
            }
        } else {
            if (row == 0) {
                return "h\u2003 g\u2003 f\u2003 e\u2003 d\u2003 c\u2003 b\u2003 a  \u2003";
            } else {
                return "\u2003  h\u2003 g\u2003 f\u2003 e\u2003 d\u2003 c\u2003 b\u2003 a  \u2003";
            }
        }
    }

    private static String getLeftColumnBorder(int row, int column, boolean flip) {
        if (column == 0) {
            String border = " ";
            if (!flip) {
                border = border + String.valueOf(9 - row) + " ";
            } else {
                border = border + String.valueOf(row) + " ";
            }
            return border;
        }
        return "";
    }

    private static String getRightColumnBorder(int row, int column, boolean flip) {
        if (column == 9) {
            String border = "\u001B[0m ";
            if (!flip) {
                border = border + String.valueOf(9 - row) + " ";
            } else {
                border = border + String.valueOf(row) + " ";
            }
            return border;
        }
        return "";
    }

    private static String getSquareString(int row, int column, boolean flip) {
        String square = "\u001b[100m";
        if (!flip) {
            if ((row + column) % 2 != 0) {
                square = "\u001b[107m";
            }
        } else {
            if ((row + column) % 2 == 0) {
                square = "\u001b[107m";
            }
        }
        return square;
    }

    private static String getPieceString(int row, int column, boolean flip) {
        if (column != 0 && column != 9) {
            if (row > 2 && row < 7) {
                return " \u2003 ";
            } else if (row == 1 && flip) {
                return whiteBackRowToString(column);
            } else if (row == 1 && !flip) {
                return blackBackRowToString(column);
            } else if (row == 8 && flip) {
                return blackBackRowToString(column);
            } else if (row == 8 && !flip) {
                return whiteBackRowToString(column);
            } else if (row == 2 && !flip) {
                return " ♟ ";
            } else if (row == 7 && !flip) {
                return " ♙ ";
            } else if (row == 7 && flip) {
                return " ♟ ";
            } else {
                return " ♙ ";
            }
        }
        return "";
    }

    private static String blackBackRowToString(int column) {
        if (column == 1 || column == 8) {
            return " ♜ ";
        } else if (column == 2 || column == 7) {
            return " ♞ ";
        } else if (column == 3 || column == 6) {
            return " ♝ ";
        } else if (column == 4) {
            return " ♚ ";
        } else if (column == 5) {
            return " ♛ ";
        } else { return ""; }
    }

    private static String whiteBackRowToString(int column) {
        if (column == 1 || column == 8) {
            return " ♖ ";
        } else if (column == 2 || column == 7) {
            return " ♘ ";
        } else if (column == 3 || column == 6) {
            return " ♗ ";
        } else if (column == 4) {
            return " ♕ ";
        } else if (column == 5) {
            return " ♔ ";
        } else { return ""; }
    }
}
