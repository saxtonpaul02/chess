package ui.websocket;

import chess.*;
import com.google.gson.Gson;
import request.*;
import result.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.Collection;

public class ServerFacade {

    private final String serverUrl;
    private final ServerMessageObserver messageObserver;
    private WebSocketFacade ws;

    public ServerFacade(String serverUrl, ServerMessageObserver messageObserver) {
        this.serverUrl = serverUrl;
        this.messageObserver = messageObserver;
        try {
            ws = new WebSocketFacade(this.serverUrl, this.messageObserver);
        } catch (Exception ex) {
            System.out.println("Error: Websocket issues.\n");
        }
    }



    public String register(String... params) throws Exception {
        try {
            var path = "/user";
            RegisterRequest registerRequest = new RegisterRequest(params[0], params[1], params[2]);
            RegisterResult registerResult = this.makeRequest("POST", path, registerRequest, null, RegisterResult.class);
            return registerResult.authToken();
        } catch (Exception ex) {
            throw new Exception("Error registering, please try again. Enter help if assistance is needed.\n");
        }
    }

    public String login(String... params) throws Exception {
        try {
            var path = "/session";
            LoginRequest loginRequest = new LoginRequest(params[0], params[1]);
            LoginResult loginResult = this.makeRequest("POST", path, loginRequest, null, LoginResult.class);
            return loginResult.authToken();
        } catch (Exception ex) {
            throw new Exception("Error logging in, please try again. Enter help if assistance is needed.\n");
        }
    }

    public void createGame(String authToken, String... params) throws Exception {
        try {
            var path = "/game";
            CreateRequest createRequest = new CreateRequest(authToken, params[0]);
            this.makeRequest("POST", path, createRequest, authToken, ChessGame.class);
        } catch (Exception ex) {
            throw new Exception("Error creating game, please try again. Enter help if assistance is needed.\n");
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
            throw new Exception("Error listing games, please try again. Enter help if assistance is needed.\n");
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
            throw new Exception("Error joining game, please try again. Enter help if assistance is needed.\n");
        }
    }

    public void observeGame(String authToken, String... params) throws Exception {
        try {
            ws = new WebSocketFacade(serverUrl, messageObserver);
            ws.connect(authToken, params[0]);
        } catch (Exception ex) {
            throw new Exception("Error observing game, please try again. Enter help if assistance is needed.\n");
        }
    }

    public void logout(String authToken) throws Exception {
        try {
            var path = "/session";
            this.makeRequest("DELETE", path, null, authToken, null);
        } catch (Exception ex) {
            throw new Exception("Error logging out, please try again. Enter help if assistance is needed.\n");
        }
    }

    public String redrawBoard(ChessGame game, boolean flip) throws Exception {
        try {
            return drawGame(game, flip, null);
        } catch (Exception ex) {
            throw new Exception("Error redrawing board, please try again. Enter help if assistance is needed.\n");
        }
    }

    public String highlightLegalMoves(ChessGame game, boolean flip, String... params) throws Exception {
        try {
            return drawGame(game, flip, stringToPosition(params[0]));
        } catch (Exception ex) {
            throw new Exception("Error highlighting legal moves, please try again. " +
                    "Enter help if assistance is needed.\n");
        }
    }

    public void makeMove(int gameID, String... params) throws Exception {
        try {
            ws.makeMove(gameID, params);
        } catch (Exception ex) {
            throw new Exception("Error making move, please try again. Enter help if assistance is needed.\n");
        }
    }

    public void resignGame(String authToken, int gameID) throws Exception {
        try {
            ws.resignGame(authToken, gameID);
        } catch (Exception ex) {
            throw new Exception("Error resigning game, please try again. Enter help if assistance is needed.\n");
        }
    }

    public void leaveGame(String authToken, int gameID) throws Exception {
        try {
            ws.leaveGame(authToken, gameID);
        } catch (Exception ex) {
            throw new Exception("Error leaving game, please try again. Enter help if assistance is needed.\n");
        }
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

    private static String drawGame(ChessGame game, boolean flip, ChessPosition position) {
        StringBuilder response = new StringBuilder("\n \u2003 ");
        for (int i = 0; i < 10; i++) {
            if (i == 0 || i == 9) {
                response.append(getRowBorder(i, flip));
            } else {
                for (int j = 0; j < 10; j++) {
                    response.append(getLeftColumnBorder(i, j, flip));
                    response.append(getSquareString(9 - i, j, flip, game, position));
                    response.append(getPieceString(i, j, game.getBoard(), flip));
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

    private static String getSquareString(int row, int column, boolean flip, ChessGame game, ChessPosition position) {
        if (position == null) {
            return getNormalSquares(row, column, flip);
        } else {
            Collection<ChessMove> validMoves = game.validMoves(position);
            String square;
            if (!flip) {
                if (position.getRow() == row && position.getColumn() == column) {
                    square = "\u001b[48;5;22m";
                } else if (isPositionAValidMove(validMoves, new ChessPosition(row, column))) {
                    square = getWhiteHighlightedSquares(row, column);
                } else {
                    square = getNormalSquares(row, column, false);
                }
            } else {
                if (position.getRow() == 9 - row && position.getColumn() == 9 - column) {
                    square = "\u001b[48;5;22m";
                } else if (isPositionAValidMove(validMoves, new ChessPosition(9 - row, 9 - column))) {
                    square = getBlackHighlightedSquares(row, column);
                } else {
                    square = getNormalSquares(row, column, true);
                }
            }
            return square;
        }
    }

    private static String getPieceString(int row, int column, ChessBoard board, boolean flip) {
        if (column != 0 && column != 9) {
            if (flip) {
                column = 9 - column;
            } else {
                row = 9 - row;
            }
            ChessPiece piece = board.getPiece(new ChessPosition(row, column));
            if (piece == null) {
                return " \u2003 ";
            } else {
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                    return whitePiecesToString(piece.getPieceType());
                } else {
                    return blackPiecesToString(piece.getPieceType());
                }
            }
        }
        return "";
    }

    private static String blackPiecesToString(ChessPiece.PieceType piece) {
        return switch (piece) {
            case ROOK -> " \u001b[38;5;94m♜\u001b[39m ";
            case KNIGHT -> " \u001b[38;5;94m♞\u001b[39m ";
            case BISHOP -> " \u001b[38;5;94m♝\u001b[39m ";
            case KING -> " \u001b[38;5;94m♚\u001b[39m ";
            case QUEEN -> " \u001b[38;5;94m♛\u001b[39m ";
            case PAWN -> " \u001b[38;5;94m♟\u001b[39m ";
        };
    }

    private static String whitePiecesToString(ChessPiece.PieceType piece) {
        return switch (piece) {
            case ROOK -> " \u001b[38;5;226m♜\u001b[39m ";
            case KNIGHT -> " \u001b[38;5;226m♞\u001b[39m ";
            case BISHOP -> " \u001b[38;5;226m♝\u001b[39m ";
            case KING -> " \u001b[38;5;226m♚\u001b[39m ";
            case QUEEN -> " \u001b[38;5;226m♛\u001b[39m ";
            case PAWN -> " \u001b[38;5;226m♟\u001b[39m ";
        };
    }

    private static String getNormalSquares(int row, int column, boolean flip) {
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

    private static String getWhiteHighlightedSquares(int row, int column) {
        String square = "\u001b[44m";
        if ((row + column) % 2 != 0) {
            square = "\u001b[104m";
        }
        return square;
    }

    private static String getBlackHighlightedSquares(int row, int column) {
        String square = "\u001b[44m";
        if ((row + column) % 2 == 0) {
            square = "\u001b[104m";
        }
        return square;
    }

    private ChessPosition stringToPosition(String pos) throws Exception {
        int row = 0;
        int col = 0;
        int counter = 1;
        ChessPosition position = null;
        for (char c : pos.toCharArray()) {
            if (counter % 2 == 1) {
                col = switch (c) {
                    case 'h' -> 8;
                    case 'g' -> 7;
                    case 'f' -> 6;
                    case 'e' -> 5;
                    case 'd' -> 4;
                    case 'c' -> 3;
                    case 'b' -> 2;
                    case 'a' -> 1;
                    default -> throw new Exception("Error: invalid position entry. Please try again.");
                };
            } else {
                if (c >= '1' && c <= '8') {
                    row = Character.getNumericValue(c);
                } else {
                    throw new Exception("Error: invalid position entry. Please try again.");
                }
                position = new ChessPosition(row, col);
            }
            counter++;
        }
        return position;
    }

    private static boolean isPositionAValidMove(Collection<ChessMove> validMoves, ChessPosition position) {
        for (ChessMove move : validMoves) {
            if (move.getEndPosition().getRow() == position.getRow() &&
                    move.getEndPosition().getColumn() == position.getColumn()) {
                return true;
            }
        }
        return false;
    }
}
