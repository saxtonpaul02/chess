package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import java.util.HashMap;
import java.util.HashSet;

public class WebSocketSessions {

    public final HashMap<String, HashSet<Session>> sessionMap = new HashMap<>();

    public void add(String gameID, Session session) { sessionMap.get(gameID).add(session); }

    public void remove(String gameID, Session session) { sessionMap.get(gameID).remove(session); }

    public HashSet<Session> get(String gameID) { return sessionMap.get(gameID); }
}
