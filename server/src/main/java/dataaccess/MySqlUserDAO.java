package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import request.RegisterRequest;

public class MySqlUserDAO implements UserDAO {

    public MySqlUserDAO() throws DataAccessException {
        configureDatabase();
    }

    public UserData getUser(String username) {
        return null;
    }

    public UserData createUser(RegisterRequest registerRequest) {
        return null;
    }

    public void clearUser() {
    }

    private void configureDatabase() throws DataAccessException {

    }
}
