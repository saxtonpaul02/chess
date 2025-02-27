package request;

public record CreateRequest(String authToken, String gameName) {
    public CreateRequest setAuthToken(String newAuthToken) {
        return new CreateRequest(newAuthToken, gameName);
    }
}
