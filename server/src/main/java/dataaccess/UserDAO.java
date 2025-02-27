package dataaccess;

import model.UserData;
import request.RegisterRequest;

public interface UserDAO {

    UserData getUser(String username) throws DataAccessException;

    UserData createUser(RegisterRequest registerRequest) throws DataAccessException;

    void clearUserDatabase() throws DataAccessException;
}
