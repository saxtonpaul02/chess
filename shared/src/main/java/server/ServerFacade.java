package server;

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

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String register(String... params) throws Exception {
        var path = "/user";
        RegisterRequest registerRequest = new RegisterRequest(params[0], params[1], params[2]);
        RegisterResult registerResult = this.makeRequest("POST", path, registerRequest, null, RegisterResult.class);
        return registerResult.authToken();
    }

    public String login(String... params) throws Exception {
        var path = "/session";
        LoginRequest loginRequest = new LoginRequest(params[0], params[1]);
        LoginResult loginResult = this.makeRequest("POST", path, loginRequest, null, LoginResult.class);
        return loginResult.authToken();
    }

    public void createGame(String authToken, String... params) throws Exception {
        var path = "/game";
        CreateRequest createRequest = new CreateRequest(params[1], params[0]);
        this.makeRequest("POST", path, createRequest, authToken, ChessGame.class);
    }

    public ChessGame[] listGames(String authToken) throws Exception {
        var path = "/game";
        record listGamesResponse(ChessGame[] game) {}
        var response = this.makeRequest("GET", path, null, authToken, listGamesResponse.class);
        return response.game();
    }

    public String joinGame(String authToken, String... params) throws Exception {
        var path = "/game";
        ChessGame.TeamColor teamColor;
        if (params[1].equals("WHITE")) {
            teamColor = ChessGame.TeamColor.WHITE;
        } else if (params[1].equals("BLACK")) {
            teamColor = ChessGame.TeamColor.BLACK;
        } else {
            throw new Exception("Invalid team color (must be all caps).");
        }
        JoinRequest joinRequest = new JoinRequest(params[2], teamColor, Integer.parseInt(params[0]));
        if (teamColor == ChessGame.TeamColor.WHITE) {
            return drawGame(this.makeRequest("PUT", path, joinRequest, authToken, ChessGame.class), false);
        } else {
            return drawGame(this.makeRequest("PUT", path, joinRequest, authToken, ChessGame.class), true);
        }
    }

    public String getGame(String authToken, String...params) throws Exception {
        var path = "/game";
        record GetRequest(String gameID) {}
        GetRequest getRequest = new GetRequest(params[0]);
        return drawGame(this.makeRequest("POST", path, getRequest, authToken, ChessGame.class), false);
    }

    public void logout(String authToken) throws Exception {
        var path = "/session";
        this.makeRequest("DELETE", path, null, authToken, null);
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

    private static String drawGame(ChessGame game, boolean flip) {
        StringBuilder response = new StringBuilder(" \u2003 ");
        if (flip) {
            for (int i = 9; i >= 0; i--) {
                if (i == 0 || i == 9) {
                    response.append(getRowBorder(i, flip));
                } else {
                    for (int j = 9; j >= 0; j--) {
                        response.append(getColumnBorder(j, flip));
                        response.append(getSquareString(i, j));
                        response.append(getPieceString(i, j, flip));
                    }
                }
            }
        } else {
            for (int i = 0; i < 10; i++) {
                if (i == 0 || i == 9) {
                    response.append(getRowBorder(i, flip));
                } else {
                    for (int j = 0; j < 10; j++) {
                        response.append(getColumnBorder(j, flip));
                        response.append(getSquareString(i, j));
                        response.append(getPieceString(i, j, flip));
                    }
                }
            }
        }
        return response.toString();
    }

    private static String getRowBorder(int row, boolean flip) {
        if (!flip) {
            if (row == 0) {
                return " a  b  c  d  e  f  g  h  \u2003 \n";
            } else {
                return " \u2003  a  b  c  d  e  f  g  h  \u2003 \n";
            }
        } else {
            if (row == 0) {
                return " h  g  f  e  d  c  b  a  \u2003 \n";
            } else {
                return " \u2003  h  g  f  e  d  c  b  a  \u2003 \n";
            }
        }
    }

    private static String getColumnBorder(int column, boolean flip) {
        if (column > 0 && column < 9) {
            String border = " ";
            if (!flip) {
                border = border + String.valueOf(column) + " ";
            } else {
                border = border + String.valueOf(9 - column) + " ";
            }
            return border;
        }
        return "";
    }

    private static String getSquareString(int row, int column) {
        String square = "\u001b[100m";
        if ((row + column) % 2 != 0) {
            square = "\u001b[107m";
        }
        return square;
    }

    private static String getPieceString(int row, int column, boolean flip) {
        if (row > 1 && row < 6) {
            return " \u2003 ";
        } else if (row == 1 && !flip) {
            return whiteBackRowToString(column);
        } else if (row == 1 && flip) {
            return blackBackRowToString(column);
        } else if (row == 8 && !flip) {
            return blackBackRowToString(column);
        } else if (row == 8 && flip) {
            return whiteBackRowToString(column);
        } else if (row == 2 && flip) {
            return " ♟ ";
        } else if (row == 7 && !flip) {
            return " ♟ ";
        } else {
            return " ♙ ";
        }
    }

    private static String blackBackRowToString(int column) {
        if (column == 1 || column == 8) {
            return " ♜ ";
        } else if (column == 2 || column == 7) {
            return " ♞ ";
        } else if (column == 3 || column == 6) {
            return " ♝ ";
        } else if (column == 4) {
            return " ♛ ";
        } else {
            return " ♚ ";
        }
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
        } else {
            return " ♔ ";
        }
    }
}
