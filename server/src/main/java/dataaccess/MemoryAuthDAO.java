package dataaccess;

import model.AuthData;
import java.util.HashSet;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {

    private final HashSet<AuthData> authDataSet = new HashSet<>();

    public AuthData createAuth(String username) {
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);
        authDataSet.add(authData);
        return authData;
    }

    public AuthData getAuth(String authToken) {
        for (AuthData authData : authDataSet) {
            if (authData.authToken().equals(authToken)) {
                return authData;
            }
        }
        return null;
    }

    public void deleteAuth(AuthData authData) {
        authDataSet.remove(authData);
    }

    public void clearAuth() {
        authDataSet.clear();
    }
}
