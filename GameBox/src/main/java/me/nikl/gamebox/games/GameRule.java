package me.nikl.gamebox.games;

import me.nikl.gamebox.data.toplist.SaveType;

/**
 * @author Niklas Eicker
 */
public class GameRule {
    protected SaveType saveType;
    protected boolean saveStats;
    protected String key;

    protected double moneyToPay;
    protected double minOrMaxScore;
    protected double moneyToWin;
    protected int tokenToWin;

    public GameRule(String key, boolean saveStats, SaveType saveType, double moneyToPay) {
        this.saveType = saveType;
        this.saveStats = saveStats;
        this.key = key;
        this.moneyToPay = moneyToPay;
        minOrMaxScore = saveType.isHigherScore()?0:Double.MAX_VALUE;
        moneyToWin = 0;
        tokenToWin = 0;
    }

    public void setToken(int token) {
        this.tokenToWin = token;
    }

    public int getTokenToWin(){
        return tokenToWin;
    }

    public void setMoneyToWin(double money){
        this.moneyToWin = money;
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

    public SaveType getSaveType() {
        return this.saveType;
    }

    public boolean isSaveStats() {
        return saveStats;
    }

    public String getKey() {
        return this.key;
    }

    public double getMoneyToPay() {
        return moneyToPay;
    }
}
