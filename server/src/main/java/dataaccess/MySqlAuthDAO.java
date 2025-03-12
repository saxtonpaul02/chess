package dataaccess;

import model.AuthData;
import java.util.UUID;

public class MySqlAuthDAO implements AuthDAO {

    public MySqlAuthDAO() throws DataAccessException {
        configureDatabase();
    }

    public AuthData createAuth(String username) {
        return null;
    }

    public AuthData getAuth(String authToken) {
        return null;
    }

    public void deleteAuth(AuthData authData) {
    }

    public void clearAuth() throws DataAccessException {
    }

    private void configureDatabase() throws DataAccessException {

    }
}
