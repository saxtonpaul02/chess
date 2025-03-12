package dataaccess;

import model.AuthData;
import java.util.UUID;

public class MySqlAuthDAO implements AuthDAO {

    public MySqlAuthDAO() {
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

    public void clearAuth() {
    }

    private void configureDatabase() {

    }
}
