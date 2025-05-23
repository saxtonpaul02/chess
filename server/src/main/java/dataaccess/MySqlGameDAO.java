package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import result.ListResult;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import static java.sql.Types.NULL;

public class MySqlGameDAO implements GameDAO {

    public MySqlGameDAO() {
        try {
            String[] statements = {
                    """
            CREATE TABLE IF NOT EXISTS gameData (
              `gameID` int NOT NULL AUTO_INCREMENT,
              `whiteUsername` varchar(256) DEFAULT NULL,
              `blackUsername` varchar(256) DEFAULT NULL,
              `gameName` varchar(256) NOT NULL,
              `game` TEXT NOT NULL,
              PRIMARY KEY (`gameID`),
              INDEX (whiteUsername),
              INDEX (blackUsername),
              INDEX (gameName)
            )
            """
            };
            ConfigureDatabase.run(statements);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public GameData create(String gameName) throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        var jsonGame = new Gson().toJson(chessGame);
        var statement = "INSERT INTO gameData (gameName, game) VALUES (?, ?)";
        int gameID = executeUpdate(statement, gameName, jsonGame);
        return new GameData(gameID, null, null, gameName, chessGame);
    }

    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM gameData WHERE gameID=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    public int updateGame(GameData gameData, AuthData authData,
                           ChessGame.TeamColor playerColor) throws DataAccessException {
        String username;
        if (authData == null) { username = null; }
        else { username = authData.username(); }
        int id = gameData.gameID();
        String statement;
        if (playerColor == ChessGame.TeamColor.WHITE) {
            statement = "UPDATE gameData SET whiteUsername=? WHERE gameID=?";
            return executeUpdate(statement, username, id);
        } else if (playerColor == ChessGame.TeamColor.BLACK) {
            statement = "UPDATE gameData SET blackUsername=? WHERE gameID=?";
            return executeUpdate(statement, username, id);
        } else {
            var jsonGame = new Gson().toJson(gameData.game());
            statement = "UPDATE gameData SET game=? WHERE gameID=?";
            return executeUpdate(statement, jsonGame, id);
        }
    }

    public Collection<ListResult> listGames() throws DataAccessException {
        Collection<ListResult> result = new ArrayList<>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM gameData";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(convertGameDataToListResult(readGame(rs)));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return result;
    }

    public void clearGame() throws DataAccessException {
        var statement = "TRUNCATE gameData";
        executeUpdate(statement);
    }

    private ListResult convertGameDataToListResult(GameData gameData) {
        return new ListResult(gameData.gameID(),
                gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName());
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        int gameID = rs.getInt("gameID");
        String whiteUsername = rs.getString("whiteUsername");
        String blackUsername = rs.getString("blackUsername");
        String gameName = rs.getString("gameName");
        String jsonGame = rs.getString("game");
        ChessGame game = new Gson().fromJson(jsonGame, ChessGame.class);
        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
    }

    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) {ps.setString(i + 1, p);}
                    else if (param instanceof Integer p) {ps.setInt(i + 1, p);}
                    else if (param instanceof ChessGame p) {ps.setString(i + 1, p.toString());}
                    else if (param == null) {ps.setNull(i + 1, NULL);}
                    }
                ps.executeUpdate();
                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to access database: %s", ex.getMessage()));
        }
    }
}
