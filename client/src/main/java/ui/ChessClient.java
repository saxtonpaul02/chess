package ui;

import chess.ChessGame;
import com.google.gson.Gson;
import server.ServerFacade;
import java.util.Arrays;

public class ChessClient {
    private String visitorName = null;
    private final ServerFacade server;
    public State state = State.LOGGED_OUT;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> playGame(params);
                case "observe" -> observeGame(params);
                case "logout" -> logout();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    public String register(String... params) throws Exception {
        if (params.length == 3) {
            server.register(params);
            state = State.LOGGED_IN;
            visitorName = params[0];
            return String.format("Logged in as %s.", visitorName);
        }
        throw new Exception("Error registering, please try again.");
    }

    public String login(String... params) throws Exception {
        if (params.length == 2) {
            server.login(params);
            state = State.LOGGED_IN;
            visitorName = params[0];
            return String.format("Logged in as %s.", visitorName);
        }
        throw new Exception("Error logging in, please try again.");
    }

    public String createGame(String... params) throws Exception {
        assertLoggedIn();
        if (params.length == 2) {
            server.createGame(params);
            return String.format("Successfully created game %s.", params[0]);
        }
        throw new Exception("Error creating game, please try again.");
    }

    public String listGames() throws Exception {
        assertLoggedIn();
        try {
            var games = server.listGames();
            var result = new StringBuilder();
            var gson = new Gson();
            for (var game : games) {
                result.append(gson.toJson(game)).append('\n');
            }
            return result.toString();
        } catch (Exception ex) {
            throw new Exception("Error listing games, please try again.");
        }
    }

    public String playGame(String... params) throws Exception {
        assertLoggedIn();
        if (params.length == 2) {
            ChessGame game = server.joinGame(params);
            if (params[1].equals("BLACK")) {
                return null;
            } else { return game.toString(); }
        }
        throw new Exception("Error joining game, please try again.");
    }

    public String observeGame(String... params) throws Exception {
        assertLoggedIn();
        if (params.length == 1) {
            ChessGame game = server.getGame();
            return game.toString();
        }
        throw new Exception("Error getting game, please try again.");
    }

    public String logout() throws Exception {
        assertLoggedIn();
        try {
            server.logout();
            state = State.LOGGED_OUT;
            visitorName = null;
            return "Successfully logged out.";
        } catch (Exception ex) {
            throw new Exception("Error logging out, please try again");
        }
    }

    public String help() {
        if (state == State.LOGGED_OUT) {
            return """
                    register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                    login <USERNAME> <PASSWORD> - to play chess
                    quit - playing chess
                    help - with possible commands
                    """;
        } else {
            return """
                    create <NAME> - a game
                    list - games
                    join <ID> [WHITE|BLACK] - a game
                    observe <ID> - a game
                    logout - when you are done
                    quit - playing chess
                    help - with possible commands
                    """;
        }
    }

    private void assertLoggedIn() throws Exception {
        if (state == State.LOGGED_OUT) {
            throw new Exception("You must log in");
        }
    }
}