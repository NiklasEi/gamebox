package me.nikl.gamebox.games;

import me.nikl.gamebox.data.SaveType;

import java.util.HashSet;

/**
 * Created by nikl on 02.11.17.
 */
public class GameRule {

    protected HashSet<SaveType> saveTypes;

    protected boolean saveStats;

    public GameRule(boolean saveStats, HashSet<SaveType> saveTypes){
        this.saveTypes = saveTypes;
        this.saveStats = saveStats;
    }

    public HashSet<SaveType> getSaveTypes(){
        return this.saveTypes;
    }

    public boolean isSaveStats(){
        return saveStats;
    }
}
