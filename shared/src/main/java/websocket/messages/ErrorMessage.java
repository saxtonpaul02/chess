package websocket.messages;

public class ErrorMessage extends ServerMessage {

    private final String message;

    public ErrorMessage(String message) {
        super(ServerMessageType.NOTIFICATION);
        this.message = message;
    }

    public String getMessage() { return this.message; }
}
