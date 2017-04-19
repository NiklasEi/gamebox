package me.nikl.connectfour;

/**
 * Created by Niklas on 16.02.2017.
 */
public class GameRules {

    private int tokens;
    private double cost, reward;
    private boolean saveStats;
    private String key;

    public GameRules(String key, double cost, double reward, int tokens, boolean saveStats){
        this.cost = cost;
        this.reward = reward;
        this.saveStats = saveStats;
        this.key = key;
        this.tokens = tokens;
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
}
