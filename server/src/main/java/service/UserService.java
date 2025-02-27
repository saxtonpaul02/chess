package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import request.*;
import result.*;

public class UserService {

    private final AuthDAO authDao;
    private final UserDAO userDao;

    public UserService(AuthDAO authDao, UserDAO userDao) {
        this.authDao = authDao;
        this.userDao = userDao;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
        if (userDao.getUser(registerRequest.username()) == null) {
            UserData userData = userDao.createUser(registerRequest);
            AuthData authData = authDao.createAuth(userData.username());
            return new RegisterResult(userData.username(), authData.authToken());
        } else {
            return null;
        }
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        UserData userData = userDao.getUser(loginRequest.username());
        if (userData != null) {
            AuthData authData = authDao.createAuth(userData.username());
            if (userData.password().equals(loginRequest.password())) {
                return new LoginResult(userData.username(), authData.authToken());
            } else {
                return new LoginResult(userData.username(), null);
            }
        } else {
            return null;
        }
    }

    public boolean logout(String authToken) throws DataAccessException {
        AuthData authData = authDao.getAuth(authToken);
        if (authData != null) {
            authDao.deleteAuth(authData);
            return true;
        } else {
            return false;
        }
    }

}
