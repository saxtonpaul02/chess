package client;

import org.junit.jupiter.api.*;
import result.ListResult;
import server.Server;
import ui.websocket.ServerFacade;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerFacadeTests {

    private static Server server;
    static ServerFacade serverFacade;
    private static String existingAuth;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        String url = "http://localhost:" + port;
        serverFacade = new ServerFacade(url, null);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    @Order(1)
    @DisplayName("Success register()")
    public void successRegister() throws Exception {
        String authToken = serverFacade.register("username", "password", "email");
        Assertions.assertTrue(authToken.length() > 10, "register() did not correctly return authToken");
    }

    @Test
    @Order(2)
    @DisplayName("Fail register()")
    public void failRegister() {
        Assertions.assertThrows(Exception.class, () -> serverFacade.register("username", "password"),
                "register() did not correctly throw exception");
    }

    @Test
    @Order(3)
    @DisplayName("Success login()")
    public void successLogin() throws Exception {
        String authToken = serverFacade.login("username", "password");
        Assertions.assertTrue(authToken.length() > 10, "login() did not correctly return authToken");
        existingAuth = authToken;
    }

    @Test
    @Order(4)
    @DisplayName("Fail login()")
    public void failLogin() {
        Assertions.assertThrows(Exception.class, () -> serverFacade.login("username", "pass"),
                "login() did not correctly throw exception");
    }

    @Test
    @Order(5)
    @DisplayName("Success createGame()")
    public void successCreateGame() throws Exception {
        serverFacade.createGame(existingAuth, "game1");
        ListResult[] gamesList = serverFacade.listGames(existingAuth);
        Assertions.assertEquals(1, gamesList.length, "createGame() did not correctly create game");
        Assertions.assertEquals("game1", gamesList[0].gameName(), "createGame() created a game with the wrong name");
    }

    @Test
    @Order(6)
    @DisplayName("Fail createGame()")
    public void failCreateGame() {
        Assertions.assertThrows(Exception.class, () -> serverFacade.createGame("fakeAuthToken", "game2"),
                "createGame() did not correctly throw exception");
        Assertions.assertThrows(Exception.class, () -> serverFacade.createGame(existingAuth),
                "createGame() did not correctly throw exception");
    }

    @Test
    @Order(7)
    @DisplayName("Success listGames()")
    public void successListGames() throws Exception {
        serverFacade.createGame(existingAuth, "game2");
        ListResult[] gamesList = serverFacade.listGames(existingAuth);
        Assertions.assertEquals(2, gamesList.length, "listGames() did not get correct number of games");
    }

    @Test
    @Order(8)
    @DisplayName("Fail listGames()")
    public void failListGames() {
        Assertions.assertThrows(Exception.class, () -> serverFacade.listGames("fakeAuthToken"),
                "createGame() did not correctly throw exception");
    }

    @Test
    @Order(9)
    @DisplayName("Success joinGame()")
    public void successJoinGame() throws Exception {
        serverFacade.joinGame(existingAuth, "1", "white");
        ListResult[] gamesList = serverFacade.listGames(existingAuth);
        Assertions.assertNotNull(gamesList[0].whiteUsername(), "joinGame() did not correctly join game");
        Assertions.assertEquals("username", gamesList[0].whiteUsername(), "joinGame() joined game with wrong user");
    }

    @Test
    @Order(10)
    @DisplayName("Fail joinGame()")
    public void failJoinGame() {
        Assertions.assertThrows(Exception.class, () -> serverFacade.joinGame(existingAuth, "1"),
                "joinGame() did not correctly throw exception");
    }

    @Test
    @Order(11)
    @DisplayName("Success observeGame()")
    public void successObserveGame() {
        Assertions.assertDoesNotThrow(() -> serverFacade.observeGame(existingAuth),
                "observeGame() incorrectly threw an exception");
    }

    @Test
    @Order(12)
    @DisplayName("Fail observeGame()")
    public void failObserveGame() {
        Assertions.assertThrows(Exception.class, () -> serverFacade.observeGame("fakeAuthToken"),
                "observeGame() did not correctly throw exception");
    }

    @Test
    @Order(13)
    @DisplayName("Fail logout()")
    public void failLogout() {
        Assertions.assertThrows(Exception.class, () -> serverFacade.logout("fakeAuthToken"),
                "logout() did not correctly throw exception");
    }

    @Test
    @Order(14)
    @DisplayName("Success logout()")
    public void successLogout() throws Exception {
        serverFacade.logout(existingAuth);
        Assertions.assertThrows(Exception.class, () -> serverFacade.listGames(existingAuth),
                "old authToken still works, logout() did not work correctly");
    }
}
