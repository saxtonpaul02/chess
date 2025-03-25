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

    public ChessGame joinGame(String... params) throws Exception {
        var path = "/game";
        return this.makeRequest("PUT", path, params, ChessGame.class);
    }

    public ChessGame getGame(String...params) throws Exception {
        var path = "/game";
        return this.makeRequest("GET", path, params, ChessGame.class);
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
}
