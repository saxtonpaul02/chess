package server;

import com.google.gson.JsonSyntaxException;
import dataaccess.*;
import exception.ErrorException;
import org.eclipse.jetty.server.Authentication;
import service.*;
import spark.*;
import com.google.gson.Gson;
import request.*;
import result.*;
import java.util.Map;

public class Server {

    private final AuthDAO authDao = new MemoryAuthDAO();
    private final GameDAO gameDao = new MemoryGameDAO();
    private final UserDAO userDao = new MemoryUserDAO();
    private final ClearService clearService = new ClearService(authDao, gameDao, userDao);
    private final GameService gameService = new GameService(authDao, gameDao);
    private final UserService userService = new UserService(authDao, userDao);

    public Server() {
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.post("/game", this::create);
        Spark.put("/game", this::join);
        Spark.get("/game", this::list);
        Spark.delete("/db", this::clear);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object register(Request req, Response res) throws DataAccessException {
        try {
            RegisterRequest registerRequest = new Gson().fromJson(req.body(), RegisterRequest.class);
            if (registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null) {
                res.status(400);
                return new Gson().toJson(new ErrorException("Error: bad request"));
            } else if (registerRequest.username().isEmpty()) {
                res.status(500);
                return new Gson().toJson(new ErrorException("Error: username too short"));
            } else if (registerRequest.password().isEmpty()) {
                res.status(500);
                return new Gson().toJson(new ErrorException("Error: password too short"));
            } else if (registerRequest.email().isEmpty()) {
                res.status(500);
                return new Gson().toJson(new ErrorException("Error: invalid email"));
            }
            RegisterResult registerResult = userService.register(registerRequest);
            if (registerResult != null) {
                return new Gson().toJson(registerResult);
            } else {
                res.status(403);
                return new Gson().toJson(new ErrorException("Error: already taken"));
            }
        } catch (JsonSyntaxException e) {
            res.status(400);
            return new Gson().toJson(new ErrorException("Error: bad request"));
        }
    }

    private Object login(Request req, Response res) throws DataAccessException {
        LoginRequest loginRequest = new Gson().fromJson(req.body(), LoginRequest.class);
        LoginResult loginResult = userService.login(loginRequest);
        if (loginResult == null) {
            res.status(401);
            return new Gson().toJson(new ErrorException("Error: unauthorized"));
        } else if (loginResult.authToken() == null) {
            res.status(401);
            return new Gson().toJson(new ErrorException("Error: unauthorized"));
        } else {
            return new Gson().toJson(loginResult);
        }
    }

    private Object logout(Request req, Response res) throws DataAccessException {
        String authToken = req.headers("Authorization");
        if (userService.logout(authToken)) {
            res.status(200);
            return "{}";
        } else {
            res.status(401);
            return new Gson().toJson(new ErrorException("Error: unauthorized"));
        }
    }

    private Object create(Request req, Response res) throws DataAccessException {
        try {
            String authToken = req.headers("Authorization");
            CreateRequest createRequest = new Gson().fromJson(req.body(), CreateRequest.class);
            createRequest = createRequest.setAuthToken(authToken);
            if (createRequest.authToken() == null || createRequest.gameName() == null) {
                res.status(400);
                return new Gson().toJson(new ErrorException("Error: bad request"));
            }
            CreateResult createResult = gameService.create(createRequest);
            if (createResult == null) {
                res.status(401);
                return new Gson().toJson(new ErrorException("Error: unauthorized"));
            } else {
                return new Gson().toJson(createResult);
            }
        } catch (JsonSyntaxException e) {
            res.status(400);
            return new Gson().toJson(new ErrorException("Error: bad request"));
        }
    }

    private Object join(Request req, Response res) throws DataAccessException {
        try {
            String authToken = req.headers("Authorization");
            JoinRequest joinRequest = new Gson().fromJson(req.body(), JoinRequest.class);
            joinRequest = joinRequest.setAuthToken(authToken);
            if (joinRequest.authToken() == null || joinRequest.playerColor() == null) {
                res.status(400);
                return new Gson().toJson(new ErrorException("Error: bad request"));
            }
            int joinResult = gameService.join(joinRequest);
            if (joinResult == 0) {
                res.status(401);
                return new Gson().toJson(new ErrorException("Error: unauthorized"));
            } else if (joinResult == 1) {
                return "{}";
            } else if (joinResult == 2) {
                res.status(403);
                return new Gson().toJson(new ErrorException("Error: already taken"));
            } else {
                res.status(400);
                return new Gson().toJson(new ErrorException("Error: bad request"));
            }
        } catch (JsonSyntaxException e) {
            res.status(400);
            return new Gson().toJson(new ErrorException("Error: bad request"));
        }
    }

    private Object list(Request req, Response res) throws DataAccessException {
        String authToken = req.headers("Authorization");
        var listResult = gameService.list(authToken);
        if (listResult == null) {
            res.status(401);
            return new Gson().toJson(new ErrorException("Error: unauthorized"));
        } else {
            return new Gson().toJson(Map.of("games", listResult));
        }
    }

    private Object clear(Request req, Response res) throws DataAccessException {
        clearService.clear();
        res.status(200);
        return "{}";
    }
}
