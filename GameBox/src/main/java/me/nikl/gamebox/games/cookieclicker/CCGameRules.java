package me.nikl.gamebox.games.cookieclicker;

/**
 * Created by Niklas
 *
 * Game rules container for Cookie Clicker
 */
public class CCGameRules {

    private double cost;
    private boolean saveStats;
    private String key;
    private int moveCookieAfterClicks;

    public CCGameRules(String key, double cost, int moveCookieAfterClicks, boolean saveStats){
        this.cost = cost;
        this.saveStats = saveStats;
        this.key = key;
        this.moveCookieAfterClicks = moveCookieAfterClicks;
    }



    public double getCost() {
        return cost;
    }

    public boolean isSaveStats() {
        return saveStats;
    }

    public String getKey() {
        return key;
    }

    public int getMoveCookieAfterClicks() {
        return moveCookieAfterClicks;
    }
}
