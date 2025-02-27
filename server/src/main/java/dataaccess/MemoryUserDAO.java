package dataaccess;

import model.UserData;
import request.RegisterRequest;

import java.util.HashSet;

public class MemoryUserDAO implements UserDAO {

    private final HashSet<UserData> userDataSet = new HashSet<>();

    public UserData getUser(String username) {
        for (UserData userData : userDataSet) {
            if (userData.username().equals(username)) {
                return userData;
            }
        }
        return null;
    }

    public UserData createUser(RegisterRequest registerRequest) {
        UserData userData = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
        userDataSet.add(userData);
        return userData;
    }

    public void clearUser() {
        userDataSet.clear();
    }
}
