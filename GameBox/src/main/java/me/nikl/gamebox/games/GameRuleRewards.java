package me.nikl.gamebox.games;

import me.nikl.gamebox.data.toplist.SaveType;

/**
 * Created by nikl on 25.02.18.
 */
public class GameRuleRewards extends GameRule {
    protected double minOrMaxScore;
    protected double moneyToWin;
    protected int tokenToWin;

    public GameRuleRewards(String key, boolean saveStats, SaveType saveType, double cost, double moneyToWin, int tokenToWin) {
        super(key, saveStats, saveType, cost);
        minOrMaxScore = saveType.isHigherScore()?0:Double.MAX_VALUE;
        this.moneyToWin = moneyToWin;
        this.tokenToWin = tokenToWin;
    }

    public int getTokenToWin(){
        return tokenToWin;
    }

    public double getMoneyToWin(){
        return moneyToWin;
    }

    public void setMinOrMaxScore(double minOrMaxScore){
        this.minOrMaxScore = minOrMaxScore;
    }

    public double getMinOrMaxScore(){
        return minOrMaxScore;
    }
}
