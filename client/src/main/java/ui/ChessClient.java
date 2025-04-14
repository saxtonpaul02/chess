package ui;

import chess.ChessGame;
import model.GameData;
import ui.websocket.ServerFacade;
import ui.websocket.ServerMessageObserver;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.Arrays;
import java.util.Scanner;

public class ChessClient implements ServerMessageObserver {
    private String visitorName = null;
    private String visitorAuthToken = null;
    private GameData joinedGameData;
    private final ServerFacade server;
    public State state = State.LOGGED_OUT;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl, this);
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
                case "redraw" -> redrawBoard();
                case "highlight" -> highlightLegalMoves(params);
                case "move" -> makeMove(params);
                case "resign" -> resignGame();
                case "leave" -> leaveGame();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    public String register(String... params) throws Exception {
        if (params.length == 3) {
            visitorAuthToken = server.register(params);
            state = State.LOGGED_IN;
            visitorName = params[0];
            return String.format("Logged in as %s.\n", visitorName);
        }
        throw new Exception("Error registering, please try again.\n");
    }

    public String login(String... params) throws Exception {
        if (params.length == 2) {
            visitorAuthToken = server.login(params);
            state = State.LOGGED_IN;
            visitorName = params[0];
            return String.format("Logged in as %s.\n", visitorName);
        }
        throw new Exception("Error logging in, please try again.\n");
    }

    public String createGame(String... params) throws Exception {
        assertLoggedIn();
        if (params.length == 1) {
            server.createGame(visitorAuthToken, params);
            return String.format("Successfully created game %s.\n", params[0]);
        }
        throw new Exception("Error creating game, please try again.\n");
    }

    public String listGames() throws Exception {
        assertLoggedIn();
        try {
            var games = server.listGames(visitorAuthToken);
            var result = new StringBuilder();
            for (int i = 1; i <= games.length; i++) {
                result.append(String.valueOf(i)).append(". ");
                result.append("Name: ").append(games[i-1].gameName()).append(", ");
                result.append("White Player: ").append(games[i-1].whiteUsername()).append(", ");
                result.append("Black Player: ").append(games[i-1].blackUsername());
                result.append("\n");
            }
            return result.toString();
        } catch (Exception ex) {
            throw new Exception("Error listing games, please try again.\n");
        }
    }

    public String playGame(String... params) throws Exception {
        assertLoggedIn();
        if (params.length == 2) {
            server.joinGame(visitorAuthToken, params[0], params[1]);
            state = State.GAMEPLAY;
            return String.format("Successfully joined game %s as %s.\n", params[0], params[1]);
        }
        throw new Exception("Error joining game, please try again.\n");
    }

    public String observeGame(String... params) throws Exception {
        assertLoggedIn();
        if (params.length == 1) {
            server.observeGame(visitorAuthToken, params[0]);
            state = State.OBSERVATION;
            return String.format("Successfully joined game %s as observer.\n", params[0]);
        }
        throw new Exception("Error observing game, please try again.\n");
    }

    public String logout() throws Exception {
        assertLoggedIn();
        try {
            server.logout(visitorAuthToken);
            state = State.LOGGED_OUT;
            visitorName = null;
            return "Successfully logged out.\n";
        } catch (Exception ex) {
            throw new Exception("Error logging out, please try again.\n");
        }
    }

    public String redrawBoard() throws Exception {
        try {
            return server.redrawBoard(joinedGameData.game(), getVisitorTeamColor() == ChessGame.TeamColor.BLACK);
        } catch (Exception ex) {
            throw new Exception("Error redrawing board, please try again.\n");
        }
    }

    public String highlightLegalMoves(String... params) throws Exception {
        if (params.length == 1) {
            return server.highlightLegalMoves(joinedGameData.game(),
                    getVisitorTeamColor() == ChessGame.TeamColor.BLACK, params);
        }
        throw new Exception("Error highlighting legal moves, please try again.\n");
    }

    public String makeMove(String... params) throws Exception {
        if (params.length == 2) {
            server.makeMove(joinedGameData.gameID(), params[0], params[1], visitorAuthToken, "");
            return "";
        } else if (params.length == 3) {
            server.makeMove(joinedGameData.gameID(), params[0], params[1], visitorAuthToken, params[2]);
            return "";
        }
        throw new Exception("Error making move, please try again.\n");
    }

    public String resignGame() throws Exception {
        System.out.println("Are you sure you want to resign? <yes or no>");
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        if (line.equals("yes") || line.equals("y")) {
            try {
                server.resignGame(visitorAuthToken, joinedGameData.gameID());
                return "\n";
            } catch (Exception ex) {
                throw new Exception("Error resigning the game, please try again.\n");
            }
        }
        return "";
    }

    public String leaveGame() throws Exception {
        try {
            server.leaveGame(visitorAuthToken, joinedGameData.gameID());
            joinedGameData = null;
            state = State.LOGGED_IN;
            return "You have left the game.\n";
        } catch (Exception ex) {
            throw new Exception("Error leaving the game, please try again\n");
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
        } else if (state == State.LOGGED_IN) {
            return """
                    create <NAME> - a game
                    list - games
                    join <ID> [WHITE|BLACK] - a game
                    observe <ID> - a game
                    logout - when you are done
                    quit - playing chess
                    help - with possible commands
                    """;
        } else if (state == State.GAMEPLAY) {
            return """
                    redraw - the board
                    highlight <STARTING_POSITION> - all legal moves of piece at given position
                    move <STARTING_POSITION> <ENDING_POSITION> <PROMOTION_PIECE> - piece from one place to another (with piece to promote to)
                    resign - the game
                    leave - the game
                    help - with possible commands
                    """;
        } else { // state == State.Observation
            return """
                    redraw - the board
                    highlight <STARTING_POSITION> - all legal moves of piece at given position
                    leave - the game
                    help - with possible commands
                    """;
        }
    }

    private void assertLoggedIn() throws Exception {
        if (state == State.LOGGED_OUT) {
            throw new Exception("You must log in");
        }
    }

    private ChessGame.TeamColor getVisitorTeamColor() {
        ChessGame.TeamColor playerColor = null;
        if (joinedGameData.whiteUsername() != null) {
            if (joinedGameData.whiteUsername().equals(visitorName)) {
                playerColor = ChessGame.TeamColor.WHITE;
            }
        }
        if (joinedGameData.blackUsername() != null) {
            if (joinedGameData.blackUsername().equals(visitorName)) {
                playerColor = ChessGame.TeamColor.BLACK;
            }
        }
        return playerColor;
    }

    @Override
    public void loadGame(LoadGameMessage message) {
        joinedGameData = message.getGame();
        try {
            System.out.println(redrawBoard());
        } catch (Exception ex) {
            System.out.println("Error loading game, please try again.");
        }
        System.out.printf("\n[%s] >>> ", state);
    }

    @Override
    public void notify(NotificationMessage message) {
        System.out.println(message.getMessage());
        System.out.printf("\n[%s] >>> ", state);
    }

    @Override
    public void notifyError(ErrorMessage message) {
        System.out.println(message.getMessage());
        System.out.printf("\n[%s] >>> ", state);
    }
}