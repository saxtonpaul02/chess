package dataaccess;

import model.AuthData;

public interface AuthDAO {

    AuthData createAuth(String username) throws DataAccessException;

    AuthData getAuth(String authToken) throws DataAccessException;

    void deleteAuth(AuthData authData) throws DataAccessException;

    void clearAuth() throws DataAccessException;
}
