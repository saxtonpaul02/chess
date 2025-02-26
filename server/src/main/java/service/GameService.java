package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;

public class GameService {

    private final AuthDAO authDao;
    private final GameDAO gameDao;

    public GameService(AuthDAO authDao, GameDAO gameDao) {
        this.authDao = authDao;
        this.gameDao = gameDao;
    }

//    public ListResult list(ListRequest listRequest) {}
//
//    public CreateResult create(CreateRequest createRequest) {}
//
//    public JoinResult join(JoinRequest joinRequest) {}

}
