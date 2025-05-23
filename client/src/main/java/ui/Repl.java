package ui;

import java.util.Scanner;

public class Repl {

    private final ChessClient client;

    public Repl(String serverUrl) { client = new ChessClient(serverUrl); }

    public void run() {
        System.out.println("♕ Welcome to 240 chess. Type 'help' to get started.");
        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            try { Thread.sleep(1000); }
            catch (InterruptedException ex) { System.out.println("Error waiting for websocket."); }
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

    private void printPrompt() {
        System.out.printf("\n[%s] >>> ", client.state);
    }
}
