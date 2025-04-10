package ui;

import ui.websocket.ServerMessageObserver;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.Scanner;

public class Repl implements ServerMessageObserver {

    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl, this);
    }

    public void run() {
        System.out.println("♕ Welcome to 240 chess. Type 'help' to get started.");
        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();
            try {
                result = client.eval(line);
                System.out.print(result);
            } catch (Throwable e) {
                System.out.print(e.getMessage());
            }
        }
        System.out.println();
    }

    @Override
    public void notify(NotificationMessage message) {
        printPrompt();
    }

    @Override
    public void notifyError(ErrorMessage message) {
        printPrompt();
    }

    @Override
    public void loadGame(LoadGameMessage message) {
        printPrompt();
    }

    private void printPrompt() {
        System.out.printf("\n[%s] >>> ", client.state);
    }
}
