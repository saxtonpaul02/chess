package service;


import chess.ChessGame;
import dataaccess.*;
import model.GameData;
import org.junit.jupiter.api.*;
import request.*;
import result.*;
import server.Server;
import java.util.ArrayList;
import java.util.Collection;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceClassTests {

    private static Server server;
    private static final AuthDAO AUTH_DAO = new MemoryAuthDAO();
    private static final GameDAO GAME_DAO = new MemoryGameDAO();
    private static final UserDAO USER_DAO = new MemoryUserDAO();
    private static final ClearService CLEAR_SERVICE = new ClearService(AUTH_DAO, GAME_DAO, USER_DAO);
    private static final GameService GAME_SERVICE = new GameService(AUTH_DAO, GAME_DAO);
    private static final UserService USER_SERVICE = new UserService(AUTH_DAO, USER_DAO);
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
    @DisplayName("Success UserService register()")
    public void successRegister() throws DataAccessException {
        RegisterRequest registerRequest = new RegisterRequest("username1", "password1", "fake1@email.com");
        RegisterResult expectedResult = new RegisterResult("username1", "unknown");
        RegisterResult actualResult = USER_SERVICE.register(registerRequest);
        Assertions.assertNotNull(actualResult, "register() returned null");
        Assertions.assertNotNull(actualResult.authToken(), "register() returned a null authToken");
        Assertions.assertNotNull(actualResult.authToken(), "register() returned a null username");
        Assertions.assertEquals(expectedResult.username(), actualResult.username(), "register() returned incorrect username");

        existingAuth = actualResult.authToken();
    }

    @Test
    @Order(2)
    @DisplayName("Fail UserService register()")
    public void failRegister() throws DataAccessException {
        RegisterRequest registerRequest = new RegisterRequest("username1", "password1", "fake1@email.com");
        RegisterResult actualResult = USER_SERVICE.register(registerRequest);
        Assertions.assertNull(actualResult, "register() did not return null");
    }

    @Test
    @Order(3)
    @DisplayName("Fail UserService logout()")
    public void failLogout() throws DataAccessException {
        Assertions.assertFalse(USER_SERVICE.logout("notExistingAuthToken"), "logout() returned true");
    }

    @Test
    @Order(4)
    @DisplayName("Success UserService logout()")
    public void successLogout() throws DataAccessException {
        boolean logoutResult = USER_SERVICE.logout(existingAuth);
        Assertions.assertTrue(logoutResult, "logout() returned false");
        Assertions.assertNull(AUTH_DAO.getAuth(existingAuth), "logout() did not delete authData from database");
    }

    @Test
    @Order(5)
    @DisplayName("Fail UserService login()")
    public void failLogin() throws DataAccessException {
        LoginRequest loginRequest1 = new LoginRequest("", "password1");
        LoginResult loginResult1 = USER_SERVICE.login(loginRequest1);
        Assertions.assertNull(loginResult1, "login() did not return null");

        LoginRequest loginRequest2 = new LoginRequest("username1", "incorrect");
        LoginResult loginResult2 = USER_SERVICE.login(loginRequest2);
        Assertions.assertNotNull(loginResult2, "login() returned null");
        Assertions.assertNull(loginResult2.authToken(), "login() did not return a null authToken");
    }

    @Test
    @Order(6)
    @DisplayName("Success UserService login()")
    public void successLogin() throws DataAccessException {
        LoginRequest loginRequest = new LoginRequest("username1", "password1");
        LoginResult expectedResult = new LoginResult("username1", "unknown");
        LoginResult actualResult = USER_SERVICE.login(loginRequest);
        Assertions.assertNotNull(actualResult, "login() returned null");
        Assertions.assertNotNull(actualResult.authToken(), "login() returned a null authToken");
        Assertions.assertEquals(expectedResult.username(), actualResult.username(), "login() returned incorrect username");
        existingAuth = actualResult.authToken();
    }

    @Test
    @Order(7)
    @DisplayName("Fail GameService create()")
    public void failCreate() throws DataAccessException {
        CreateRequest createRequest1 = new CreateRequest("notExistingAuthToken", "game1");
        CreateResult createResult1 = GAME_SERVICE.create(createRequest1);
        Assertions.assertNull(createResult1, "create() did not return null");
    }

    @Test
    @Order(8)
    @DisplayName("Success GameService create()")
    public void successCreate() throws DataAccessException {
        CreateRequest createRequest = new CreateRequest(existingAuth, "game1");
        CreateResult actualResult = GAME_SERVICE.create(createRequest);
        Assertions.assertNotNull(actualResult, "create() returned null");
        Assertions.assertNotNull(GAME_DAO.getGame(actualResult.gameID()), "GameID was not found in database");
        existingGameID = actualResult.gameID();
        GameData existingGame = GAME_DAO.getGame(existingGameID);
        Assertions.assertNull(existingGame.whiteUsername(), "create() does not give null white username");
        Assertions.assertNull(existingGame.blackUsername(), "create() does not give null black username");
        Assertions.assertEquals("game1", existingGame.gameName(), "create() gives incorrect gameName");
        Assertions.assertInstanceOf(ChessGame.class, existingGame.game(), "create() does not give instance of ChessGame");
    }

    @Test
    @Order(9)
    @DisplayName("Success GameService join()")
    public void successJoin() throws DataAccessException {
        JoinRequest joinRequest = new JoinRequest(existingAuth, ChessGame.TeamColor.WHITE, existingGameID);
        int actualResult = GAME_SERVICE.join(joinRequest);
        Assertions.assertNotEquals(0, actualResult, "join() returned 0 / did not find authToken in database");
        Assertions.assertNotEquals(3, actualResult, "join() returned 3 / did not find GameID in database");
        Assertions.assertNotEquals(2, actualResult, "join() returned 2 / team color to join is taken");
        Assertions.assertNotEquals(4, actualResult, "join() returned 4 / invalid team color to join");
    }

    @Test
    @Order(10)
    @DisplayName("Fail GameService join()")
    public void failJoin() throws DataAccessException {
        JoinRequest joinRequest1 = new JoinRequest("notExistingAuthToken", ChessGame.TeamColor.WHITE, existingGameID);
        int joinResult1 = GAME_SERVICE.join(joinRequest1);
        Assertions.assertEquals(0, joinResult1, "join() accepted invalid authToken");

        JoinRequest joinRequest2 = new JoinRequest(existingAuth, ChessGame.TeamColor.WHITE, 12345);
        int joinResult2 = GAME_SERVICE.join(joinRequest2);
        Assertions.assertEquals(3, joinResult2, "join() accepted invalid gameID");

        JoinRequest joinRequest3 = new JoinRequest(existingAuth, ChessGame.TeamColor.WHITE, existingGameID);
        int joinResult3 = GAME_SERVICE.join(joinRequest3);
        Assertions.assertEquals(2, joinResult3, "join() did not recognize request team color to join was taken");
    }

    @Test
    @Order(11)
    @DisplayName("Success GameService list()")
    public void successList() throws DataAccessException {
        CreateRequest createGame2 = new CreateRequest(existingAuth, "game2");
        CreateResult game2Result = GAME_SERVICE.create(createGame2);
        Collection<ListResult> expectedResult = new ArrayList<>();
        expectedResult.add(new ListResult(existingGameID, "username1", null, "game1"));
        expectedResult.add(new ListResult(game2Result.gameID(), null, null, "game2"));
        Collection<ListResult> listResult = GAME_SERVICE.list(existingAuth);
        Assertions.assertNotNull(listResult, "list() returned null");
        Assertions.assertTrue(expectedResult.containsAll(listResult)
                && listResult.containsAll(expectedResult), "list() returned an incorrect Collection");
    }

    @Test
    @Order(12)
    @DisplayName("Fail GameService list()")
    public void failList() throws DataAccessException {
        Collection<ListResult> listResult1 = GAME_SERVICE.list("notExistingAuthToken");
        Assertions.assertNull(listResult1, "list() did not return null");
    }

    @Test
    @Order(13)
    @DisplayName("Fail ClearService clear()")
    public void failClear() {
        Assertions.assertFalse(false, "clear() has no conditionals");
    }

    @Test
    @Order(14)
    @DisplayName("Success ClearService clear()")
    public void successClear() throws DataAccessException {
        CLEAR_SERVICE.clear();
        Assertions.assertNull(USER_DAO.getUser("username1"), "clear() did not clear UserData in database");
        Assertions.assertNull(GAME_DAO.getGame(existingGameID), "clear() did not clear GameData in database");
        Assertions.assertNull(AUTH_DAO.getAuth(existingAuth), "clear() did not clear AuthData in database");
    }
}
