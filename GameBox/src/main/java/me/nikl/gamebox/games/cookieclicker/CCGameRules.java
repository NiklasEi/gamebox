package me.nikl.gamebox.games.cookieclicker;

import me.nikl.gamebox.data.SaveType;
import me.nikl.gamebox.games.GameRule;

import java.util.HashSet;

/**
 * Created by Niklas
 *
 * Game rules container for Cookie Clicker
 */
public class CCGameRules extends GameRule{

    private double cost;
    private String key;
    private int moveCookieAfterClicks;

    public CCGameRules(String key, double cost, int moveCookieAfterClicks, boolean saveStats){
        super(saveStats, new HashSet<>());
        this.saveTypes.add(SaveType.HIGH_NUMBER_SCORE);
        this.cost = cost;
        this.saveStats = saveStats;
        this.key = key;
        this.moveCookieAfterClicks = moveCookieAfterClicks;
    }



    public double getCost() {
        return cost;
    }

    public String getKey() {
        return key;
    }

    public int getMoveCookieAfterClicks() {
        return moveCookieAfterClicks;
    }
}
