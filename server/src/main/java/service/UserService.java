package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;

public class UserService {

    private final AuthDAO authDao;
    private final UserDAO userDao;

    public UserService(AuthDAO authDao, UserDAO userDao) {
        this.authDao = authDao;
        this.userDao = userDao;
    }

//    public RegisterResult register(RegisterRequest registerRequest) {}
//
//    public LoginResult login(LoginRequest loginRequest) {}
//
//    public void logout(LogoutRequest logoutRequest) {}

}
