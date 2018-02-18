package me.nikl.gamebox.games;

import me.nikl.gamebox.data.toplist.SaveType;

import java.util.HashSet;

/**
 * Created by nikl on 02.11.17.
 */
public class GameRule {
    protected HashSet<SaveType> saveTypes;
    protected boolean saveStats;
    protected String key;

    public GameRule(boolean saveStats, HashSet<SaveType> saveTypes, String key){
        this.saveTypes = saveTypes;
        this.saveStats = saveStats;
        this.key = key;
    }

    public HashSet<SaveType> getSaveTypes(){
        return this.saveTypes;
    }

    public boolean isSaveStats(){
        return saveStats;
    }

    public String getKey(){
        return this.key;
    }
}
