package me.nikl.gamebox.games.matchit;

import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.games.GameRule;

import java.util.HashSet;

/**
 * Created by nikl on 02.12.17.
 */
public class MIGameRule extends GameRule {
    private double cost;
    private double timeVisible;
    private MatchIt.GridSize gridSize;

    public MIGameRule(boolean saveStats, double cost, String key, MatchIt.GridSize gridSize, double timeVisible) {
        super(saveStats, new HashSet<>(), key);
        this.saveTypes.add(SaveType.TIME_LOW);
        this.gridSize = gridSize;
        this.cost = cost;
        this.timeVisible = timeVisible;
    }

    public double getCost() {
        return cost;
    }

    public MatchIt.GridSize getGridSize() {
        return gridSize;
    }

    public double getTimeVisible() {
        return timeVisible;
    }
}
