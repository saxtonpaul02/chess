package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import request.RegisterRequest;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlUserDAO implements UserDAO {

    public MySqlUserDAO() {
        try {
            configureDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public UserData createUser(RegisterRequest registerRequest) throws DataAccessException {
        String username = registerRequest.username();
        String password = registerRequest.password();
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String email = registerRequest.email();
        var statement = "INSERT INTO userdata (username, password, email) VALUES (?, ?, ?)";
        executeUpdate(statement, username, hashedPassword, email);
        return new UserData(username, password, email);
    }

    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM gamedata WHERE username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readUser(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    public void clearUser() throws DataAccessException {
        var statement = "TRUNCATE userdata";
        executeUpdate(statement);
    }

    private UserData readUser(ResultSet rs) throws SQLException {
        String username = rs.getString("username");
        String hashedPassword = rs.getString("password");
        String email = rs.getString("email");
        return new UserData(username, hashedPassword, email);
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
            CREATE TABLE IF NOT EXISTS userdata (
              'username' varchar(256) NOT NULL,
              'password' varchar(256) NOT NULL,
              'email' varchar(256) NOT NULL,
              PRIMARY KEY ('username'),
              INDEX (password),
              INDEX (email)
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
