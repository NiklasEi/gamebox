package me.nikl.gamebox.games.cookieclicker;

/**
 * Created by Niklas
 *
 * Game rules container for Whac a mole
 */
public class GameRules {

    private double cost;
    private boolean saveStats;
    private String key;
    private int moveCookieAfterClicks;

    public GameRules(Main plugin, String key, double cost, int moveCookieAfterClicks, boolean saveStats){
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
