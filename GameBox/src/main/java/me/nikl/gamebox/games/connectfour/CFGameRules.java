package me.nikl.gamebox.games.connectfour;

import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.games.GameRuleRewards;

/**
 * @author Niklas Eicker
 *
 *         This class stores settings for a game mode
 */
public class CFGameRules extends GameRuleRewards {
    private int timePerMove, minNumberOfPlayedChips;
    private double cost;

    public CFGameRules(String key, int timePerMove, int minNumberOfPlayedChips, double cost, double reward, int tokens, boolean saveStats) {
        super(key, saveStats, SaveType.WINS, cost, reward, tokens);
        this.cost = cost;
        this.timePerMove = timePerMove;
        this.minNumberOfPlayedChips = minNumberOfPlayedChips;
    }

    public double getCost() {
        return cost;
    }

    public int getTimePerMove() {
        return timePerMove;
    }

    public int getMinNumberOfPlayedChips() {
        return minNumberOfPlayedChips;
    }
}
