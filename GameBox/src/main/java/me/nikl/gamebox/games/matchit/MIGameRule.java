package me.nikl.gamebox.games.matchit;

import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.games.GameRule;
import me.nikl.gamebox.games.GameRuleRewards;

import java.util.HashSet;

/**
 * Created by nikl on 02.12.17.
 */
public class MIGameRule extends GameRuleRewards {
    private double timeVisible;
    private MatchIt.GridSize gridSize;

    public MIGameRule(boolean saveStats, double cost, String key, MatchIt.GridSize gridSize, double timeVisible, double moneyToWin, int tokenToWin) {
        super(key, saveStats, SaveType.TIME_LOW, cost, moneyToWin, tokenToWin);
        this.gridSize = gridSize;
        this.timeVisible = timeVisible;
    }

    public MatchIt.GridSize getGridSize() {
        return gridSize;
    }

    public double getTimeVisible() {
        return timeVisible;
    }
}
