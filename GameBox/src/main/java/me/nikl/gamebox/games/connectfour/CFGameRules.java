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

    public CFGameRules(String key, int timePerMove, int minNumberOfPlayedChips, double cost, double reward, int tokens, boolean saveStats) {
        super(key, saveStats, SaveType.WINS, cost, reward, tokens);
        this.timePerMove = timePerMove;
        this.minNumberOfPlayedChips = minNumberOfPlayedChips;
    }

    public int getTimePerMove() {
        return timePerMove;
    }

    public int getMinNumberOfPlayedChips() {
        return minNumberOfPlayedChips;
    }
}
