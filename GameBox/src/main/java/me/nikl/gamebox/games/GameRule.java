package me.nikl.gamebox.games;

import me.nikl.gamebox.data.toplist.SaveType;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Niklas Eicker
 */
public class GameRule {
    protected HashSet<SaveType> saveTypes = new HashSet<>();
    protected boolean saveStats;
    protected String key;

    public GameRule(boolean saveStats, Set<SaveType> saveTypes, String key) {
        this.saveTypes.addAll(saveTypes);
        this.saveStats = saveStats;
        this.key = key;
    }

    public HashSet<SaveType> getSaveTypes() {
        return this.saveTypes;
    }

    public boolean isSaveStats() {
        return saveStats;
    }

    public String getKey() {
        return this.key;
    }
}
