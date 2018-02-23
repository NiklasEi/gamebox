package me.nikl.gamebox.games.cookieclicker;

import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.games.GameRule;

import java.util.HashSet;

/**
 * Created by Niklas
 *
 * Game rules container for Cookie Clicker
 */
public class CCGameRules extends GameRule {

    private double cost;
    private int moveCookieAfterClicks;

    public CCGameRules(String key, double cost, int moveCookieAfterClicks, boolean saveStats) {
        super(key, saveStats, SaveType.HIGH_NUMBER_SCORE);
        this.cost = cost;
        this.saveStats = saveStats;
        this.moveCookieAfterClicks = moveCookieAfterClicks;
    }


    public double getCost() {
        return cost;
    }

    public int getMoveCookieAfterClicks() {
        return moveCookieAfterClicks;
    }
}
