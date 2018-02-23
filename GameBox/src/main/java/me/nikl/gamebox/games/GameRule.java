package me.nikl.gamebox.games;

import me.nikl.gamebox.data.toplist.SaveType;

import java.util.Arrays;
import java.util.HashSet;

/**
 * @author Niklas Eicker
 */
public class GameRule {
    protected HashSet<SaveType> saveTypes = new HashSet<>();
    protected boolean saveStats;
    protected String key;

    public GameRule(String key, boolean saveStats, SaveType... saveTypes) {
        if(saveTypes != null){
            this.saveTypes.addAll(Arrays.asList(saveTypes));
        }
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
