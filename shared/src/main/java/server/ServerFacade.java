package server;

import chess.ChessGame;
import com.google.gson.Gson;
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

    public void register(String... params) throws Exception {
        var path = "/user";
        this.makeRequest("POST", path, params,null);
    }

    public void login(String... params) throws Exception {
        var path = "/session";
        this.makeRequest("POST", path, params, null);
    }

    public void createGame(String... params) throws Exception {
        var path = "/game";
        this.makeRequest("POST", path, params, ChessGame.class);
    }

    public ChessGame[] listGames() throws Exception {
        var path = "/game";
        record listGamesResponse(ChessGame[] game) {}
        var response = this.makeRequest("GET", path, null, listGamesResponse.class);
        return response.game();
    }

    public String joinGame(String... params) throws Exception {
        var path = "/game";
        if (params[1].equals("WHITE")) {
            return drawGame(this.makeRequest("PUT", path, params, ChessGame.class), false);
        } else {
            return drawGame(this.makeRequest("PUT", path, params, ChessGame.class), true);
        }
    }

    public String getGame(String...params) throws Exception {
        var path = "/game";
        return drawGame(this.makeRequest("GET", path, params, ChessGame.class), false);
    }

    public void logout() throws Exception {
        var path = "/session";
        this.makeRequest("DELETE", path, null, null);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws Exception {
        URL url = (new URI(serverUrl + path)).toURL();
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod(method);
        http.setDoOutput(true);
        writeBody(request, http);
        http.connect();
        return readBody(http, responseClass);
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content_Type", "application/json");
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
