package dataaccess;

import model.AuthData;

import java.sql.SQLException;
import java.util.UUID;
import static java.sql.Statement.*;

public class MySqlAuthDAO implements AuthDAO {

    public MySqlAuthDAO() throws DataAccessException {
        configureDatabase();
    }

    public AuthData createAuth(String username) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        var statement = "INSERT INTO authdata (authToken, username) VALUES (?, ?, ?)";
        executeUpdate(statement, authToken, username);
        return new AuthData(authToken, username);
        }

    public AuthData getAuth(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, authToken FROM authdata WHERE username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String authToken = rs.getString("authToken");
                        return new AuthData(authToken, username);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to access database: %s", e.getMessage()));
        }
        return null;
    }

    public void deleteAuth(AuthData authData) throws DataAccessException {
        String username = authData.username();
        var statement = "DELETE FROM authdata WHERE username=?";
        executeUpdate(statement, username);
    }

    public void clearAuth() throws DataAccessException {
        var statement = "TRUNCATE authdata";
        executeUpdate(statement);
    }

    private void executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                }
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to access database: %s", ex.getMessage()));
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS authdata (
              'authToken' varchar(256) NOT NULL,
              'username' varchar(256) NOT NULL,
              PRIMARY KEY ('username'),
              INDEX (authToken),
            );
            """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}
