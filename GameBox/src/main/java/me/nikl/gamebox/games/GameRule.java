package me.nikl.gamebox.games;

import me.nikl.gamebox.data.toplist.SaveType;

/**
 * @author Niklas Eicker
 */
public abstract class GameRule {
    protected SaveType saveType;
    protected boolean saveStats;
    protected String key;
    protected double cost;

    GameRule(String key, boolean saveStats, SaveType saveType, double cost) {
        this.saveType = saveType;
        this.saveStats = saveStats;
        this.key = key;
        this.cost = cost;
    }

    public SaveType getSaveType() {
        return this.saveType;
    }

    public boolean isSaveStats() {
        return saveStats;
    }

    public String getKey() {
        return this.key;
    }

    public double getCost() {
        return cost;
    }
}
