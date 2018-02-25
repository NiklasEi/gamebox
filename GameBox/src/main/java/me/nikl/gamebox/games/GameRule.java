package me.nikl.gamebox.games;

import me.nikl.gamebox.data.toplist.SaveType;

/**
 * @author Niklas Eicker
 */
public abstract class GameRule {
    protected SaveType saveType;
    protected boolean saveStats;
    protected String key;
    protected double moneyToPay;

    GameRule(String key, boolean saveStats, SaveType saveType, double moneyToPay) {
        this.saveType = saveType;
        this.saveStats = saveStats;
        this.key = key;
        this.moneyToPay = moneyToPay;
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

    public double getMoneyToPay() {
        return moneyToPay;
    }
}
