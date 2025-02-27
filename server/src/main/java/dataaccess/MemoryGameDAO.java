package dataaccess;

import model.GameData;
import java.util.HashSet;

public class MemoryGameDAO implements GameDAO {

    private final HashSet<GameData> gameDataSet = new HashSet<>();

    public void clearGameDatabase() {
        gameDataSet.clear();
    }
}
