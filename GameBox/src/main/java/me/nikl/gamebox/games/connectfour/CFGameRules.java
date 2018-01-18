package me.nikl.gamebox.games.connectfour;

import me.nikl.gamebox.data.SaveType;
import me.nikl.gamebox.games.GameRule;

import java.util.HashSet;

/**
 * @author Niklas Eicker
 *
 * This class stores settings for a game mode
 */
public class CFGameRules extends GameRule{

    private int tokens, timePerMove, minNumberOfPlayedChips;
    private double cost, reward;

    public CFGameRules(String key, int timePerMove, int minNumberOfPlayedChips, double cost, double reward, int tokens, boolean saveStats){
        super(saveStats, new HashSet<>(), key);
        this.saveTypes.add(SaveType.WINS);
        this.cost = cost;
        this.reward = reward;
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
