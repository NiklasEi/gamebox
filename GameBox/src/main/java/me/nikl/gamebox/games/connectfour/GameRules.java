package me.nikl.connectfour;

/**
 * Created by Niklas on 16.02.2017.
 *
 * This class stores settings for a game mode
 */
public class GameRules {

    private int tokens, timePerMove, minNumberOfPlayedChips;
    private double cost, reward;
    private boolean saveStats;
    private String key;

    public GameRules(String key, int timePerMove, int minNumberOfPlayedChips, double cost, double reward, int tokens, boolean saveStats){
        this.cost = cost;
        this.reward = reward;
        this.saveStats = saveStats;
        this.key = key;
        this.tokens = tokens;
        this.timePerMove = timePerMove;
        this.minNumberOfPlayedChips = minNumberOfPlayedChips;
    }

    public double getCost() {
        return cost;
    }

    public double getReward() {
        return reward;
    }

    public boolean isSaveStats() {
        return saveStats;
    }

    public String getKey() {
        return key;
    }

    public int getTokens() {
        return tokens;
    }

    public int getTimePerMove() {
        return timePerMove;
    }

    public int getMinNumberOfPlayedChips() {
        return minNumberOfPlayedChips;
    }
}
