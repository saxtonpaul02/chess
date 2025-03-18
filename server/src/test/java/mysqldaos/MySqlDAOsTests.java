package mysqldaos;

import chess.ChessGame;
import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
import request.RegisterRequest;
import result.ListResult;
import server.Server;
import java.util.Collection;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MySqlDAOsTests {
    private static Server server;
    private static final AuthDAO AUTH_DAO = new MySqlAuthDAO();
    private static final GameDAO GAME_DAO = new MySqlGameDAO();
    private static final UserDAO USER_DAO = new MySqlUserDAO();
    private static String existingAuth;
    private static int existingGameID;

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test service classes on " + port);
    }

    @Test
    @Order(1)
    @DisplayName("Success MySqlUserDAO createUser()")
    public void successCreateUser() throws DataAccessException {
        RegisterRequest registerRequest = new RegisterRequest("username1", "password1", "fake1@email.com");
        UserData userData = USER_DAO.createUser(registerRequest);
        Assertions.assertEquals("username1", userData.username(), "createUser() returned incorrect username");
        Assertions.assertEquals("fake1@email.com", userData.email(), "createUser() return incorrect email");
    }

    @Test
    @Order(2)
    @DisplayName("Fail MySqlUserDAO createUser()")
    public void failCreateUser() {
        RegisterRequest registerRequest = new RegisterRequest("username1", "password1", "fake1@email.com");
        Assertions.assertThrows(DataAccessException.class, () -> USER_DAO.createUser(registerRequest));
    }

    @Test
    @Order(3)
    @DisplayName("Success MySqlAuthDAO createAuth()")
    public void successCreateAuth() throws DataAccessException {
        AuthData authData = AUTH_DAO.createAuth("username1");
        Assertions.assertEquals("username1", authData.username(), "createAuth() return incorrect username");
        existingAuth = authData.authToken();
        Assertions.assertInstanceOf(String.class, existingAuth);
    }

    @Test
    @Order(4)
    @DisplayName("Fail MySqlAuthDAO createAuth()")
    public void failCreateAuth() {
        Assertions.assertThrows(DataAccessException.class, () -> AUTH_DAO.createAuth(null));
    }

    @Test
    @Order(5)
    @DisplayName("Success MySqlUserDAO getUser()")
    public void successGetUser() throws DataAccessException {
        UserData userData = USER_DAO.getUser("username1");
        Assertions.assertNotNull(userData, "getUser() returned null");
        Assertions.assertEquals("username1", userData.username(), "getUser() returned incorrect username");
        Assertions.assertTrue(BCrypt.checkpw("password1", userData.password()), "getUser() returned incorrect password");
        Assertions.assertEquals("fake1@email.com", userData.email(), "getUser() return incorrect email");
    }

    @Test
    @Order(6)
    @DisplayName("Fail MySqlUserDAO getUser()")
    public void failGetUser() throws DataAccessException {
        UserData userData = USER_DAO.getUser("username2");
        Assertions.assertNull(userData, "getUser() did not return null");
    }

    @Test
    @Order(7)
    @DisplayName("Success MySqlAuthDAO getAuth()")
    public void successGetAuth() throws DataAccessException {
        AuthData authData = AUTH_DAO.getAuth(existingAuth);
        Assertions.assertNotNull(authData, "getAuth() returned null");
        Assertions.assertEquals("username1", authData.username(), "getAuth() returned incorrect username");
        Assertions.assertEquals(existingAuth, authData.authToken(), "getAuth() return incorrect authToken");
    }

    @Test
    @Order(8)
    @DisplayName("Fail MySqlAuthDAO getAuth()")
    public void failGetAuth() throws DataAccessException {
        AuthData authData = AUTH_DAO.getAuth("username2");
        Assertions.assertNull(authData, "getAuth() did not return null");
    }

    @Test
    @Order(9)
    @DisplayName("Success MySqlGameDAO create()")
    public void successCreateGame() throws DataAccessException {
        GameData gameData = GAME_DAO.create("game1");
        existingGameID = gameData.gameID();
        Assertions.assertEquals(1, existingGameID, "create() returned incorrect gameID");
        Assertions.assertNull(gameData.whiteUsername(), "create() did not return null whiteUsername");
        Assertions.assertNull(gameData.blackUsername(), "create() did not return null blackUsername");
        Assertions.assertEquals("game1", gameData.gameName(), "create() returned incorrect gameName");
        Assertions.assertInstanceOf(ChessGame.class, gameData.game(), "create() did not return an instance of ChessGame");
    }

    @Test
    @Order(10)
    @DisplayName("Fail MySqlGameDAO create()")
    public void failCreateGame() {
        Assertions.assertThrows(DataAccessException.class, () -> GAME_DAO.create(null));
    }

    @Test
    @Order(11)
    @DisplayName("Success MySqlGameDAO getGame()")
    public void successGetGame() throws DataAccessException {
        GameData gameData = GAME_DAO.getGame(existingGameID);
        Assertions.assertEquals(1, existingGameID, "getGame() returned incorrect gameID");
        Assertions.assertNull(gameData.whiteUsername(), "getGame() returned incorrect whiteUsername");
        Assertions.assertNull(gameData.blackUsername(), "getGame() returned incorrect blackUsername");
        Assertions.assertEquals("game1", gameData.gameName(), "getGame() returned incorrect gameName");
        Assertions.assertInstanceOf(ChessGame.class, gameData.game(), "getGame() did not return an instance of ChessGame");
    }

    @Test
    @Order(12)
    @DisplayName("Fail MySqlGameDAO getGame()")
    public void failGetGame() throws DataAccessException {
        Assertions.assertNull(GAME_DAO.getGame(2));
    }

    @Test
    @Order(13)
    @DisplayName("Success MySqlGameDAO updateGame()")
    public void successUpdateGame() throws DataAccessException {
        GameData gameData = GAME_DAO.getGame(existingGameID);
        AuthData authData = AUTH_DAO.getAuth(existingAuth);
        GAME_DAO.updateGame(gameData, authData, ChessGame.TeamColor.WHITE);
        gameData = GAME_DAO.getGame(existingGameID);
        Assertions.assertEquals("username1", gameData.whiteUsername(), "updateGame() did not update game");
    }

    @Test
    @Order(14)
    @DisplayName("Fail MySqlGameDAO updateGame()")
    public void failUpdateGame() throws DataAccessException {
        GameData gameData = new GameData(2, null, null, "", new ChessGame());
        AuthData authData = new AuthData("unknown", "username2");
        Assertions.assertEquals(0, GAME_DAO.updateGame(gameData, authData, ChessGame.TeamColor.WHITE), "updateGame() incorrectly updated game");
    }

    @Test
    @Order(15)
    @DisplayName("Success MySqlGameDAO listGames()")
    public void successListGames() throws DataAccessException {
        Collection<ListResult> list = GAME_DAO.listGames();
        Assertions.assertEquals(1,list.size(), "listGames() returned list of wrong size");
    }

    @Test
    @Order(16)
    @DisplayName("Fail MySqlGameDAO listGames()")
    public void failListGames() throws DataAccessException {
        GameDAO memoryGameDao = new MemoryGameDAO();
        Assertions.assertEquals(0, memoryGameDao.listGames().size(), "listGames() return list of wrong size");
    }

    @Test
    @Order(17)
    @DisplayName("Success MySqlAuthDAO deleteAuth()")
    public void successDeleteAuth() throws DataAccessException {
        AuthData authData = AUTH_DAO.getAuth(existingAuth);
        AUTH_DAO.deleteAuth(authData);
        Assertions.assertNull(AUTH_DAO.getAuth(existingAuth), "deleteAuth() does not delete AuthData");
    }

    @Test
    @Order(18)
    @DisplayName("Fail MySqlAuthDAO deleteAuth()")
    public void failDeleteAuth() throws DataAccessException {
        AuthData authData = AUTH_DAO.getAuth(existingAuth);
        Assertions.assertThrows(NullPointerException.class, () -> AUTH_DAO.deleteAuth(authData), "deleteAuth() does not return error");
    }

    @Test
    @Order(19)
    @DisplayName("Success MySqlAuthDAO clearAuth()")
    public void successClearAuth() throws DataAccessException {
        AuthData authData = AUTH_DAO.createAuth("username1");
        existingAuth = authData.authToken();
        AUTH_DAO.clearAuth();
        Assertions.assertNull(AUTH_DAO.getAuth(existingAuth), "clearAuth() does not clear authData database");
    }

    @Test
    @Order(20)
    @DisplayName("Success MySqlGameDAO clearGame()")
    public void successClearGame() throws DataAccessException {
        GAME_DAO.clearGame();
        Assertions.assertEquals(0, GAME_DAO.listGames().size(), "clearGame() does not clear gameData database");
    }

    @Test
    @Order(21)
    @DisplayName("Success MySqlUserDAO clearUser()")
    public void successClearUser() throws DataAccessException {
        USER_DAO.clearUser();
        Assertions.assertNull(USER_DAO.getUser("username1"), "clearUser() does not clear userdata database");
    }
}
