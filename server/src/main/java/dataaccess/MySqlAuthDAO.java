package dataaccess;

import model.AuthData;
import java.util.UUID;

public class MySqlAuthDAO implements AuthDAO {

    public MySqlAuthDAO() {
        try {
            String[] statements = {
                    """
            CREATE TABLE IF NOT EXISTS authdata (
              `authToken` varchar(256) NOT NULL,
              `username` varchar(256) NOT NULL,
              PRIMARY KEY (`username`),
              INDEX (authToken)
            )
            """
            };
            ConfigureDatabase.run(statements);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public AuthData createAuth(String username) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        var statement = "INSERT INTO authdata (authToken, username) VALUES (?, ?, ?)";
        ConfigureDatabase.executeUpdate(statement, authToken, username);
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
        ConfigureDatabase.executeUpdate(statement, username);
    }

    public void clearAuth() throws DataAccessException {
        var statement = "TRUNCATE authdata";
        ConfigureDatabase.executeUpdate(statement);
    }
}
