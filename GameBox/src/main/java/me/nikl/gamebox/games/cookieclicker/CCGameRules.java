package me.nikl.gamebox.games.cookieclicker;

import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.game.rules.GameRuleMultiRewards;

/**
 * Created by Niklas
 *
 * Game rules container for Cookie Clicker
 */
public class CCGameRules extends GameRuleMultiRewards {
    private int moveCookieAfterClicks;

    public CCGameRules(String key, double cost, int moveCookieAfterClicks, boolean saveStats) {
        super(key, saveStats, SaveType.HIGH_NUMBER_SCORE, cost);
        this.moveCookieAfterClicks = moveCookieAfterClicks;
    }

    public int getMoveCookieAfterClicks() {
        return moveCookieAfterClicks;
    }
}
