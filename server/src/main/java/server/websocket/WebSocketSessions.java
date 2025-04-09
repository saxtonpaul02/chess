package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import java.util.HashMap;
import java.util.HashSet;

public class WebSocketSessions {

    public final HashMap<Integer, HashSet<Session>> sessionMap = new HashMap<>();

    public void add(int gameID, Session session) {
        if (get(gameID) != null) {
            sessionMap.get(gameID).add(session);
        } else {
            HashSet<Session> sessionSet = new HashSet<>();
            sessionSet.add(session);
            sessionMap.put(gameID, sessionSet);
        }
    }

    public void remove(int gameID, Session session) { sessionMap.get(gameID).remove(session); }

    public HashSet<Session> get(int gameID) { return sessionMap.get(gameID); }
}
