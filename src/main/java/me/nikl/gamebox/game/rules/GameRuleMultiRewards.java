package me.nikl.gamebox.game.rules;

import me.nikl.gamebox.data.toplist.SaveType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nikl on 25.02.18.
 */
public class GameRuleMultiRewards extends GameRule {
  protected Map<Double, Double> moneyToWin = new HashMap<>();
  protected Map<Double, Integer> tokenToWin = new HashMap<>();

  public GameRuleMultiRewards(String key, boolean saveStats, SaveType saveType, double cost) {
    super(key, saveStats, saveType, cost);
  }

  public void addMoneyReward(double minScore, double reward) {
    moneyToWin.put(minScore, reward);
  }

  public void addTokenReward(double minScore, int token) {
    tokenToWin.put(minScore, token);
  }

  public int getTokenToWin(double score) {
    double tokenKey = saveType.isHigherScore() ? 0 : Double.MAX_VALUE;
    for (double key : tokenToWin.keySet()) {
      if (scoreIsBetterThen(key, score)) continue;
      if (scoreIsBetterThen(tokenKey, score)) continue;
      tokenKey = key;
    }
    Integer token = tokenToWin.get(tokenKey);
    if (token == null) return 0;
    return token;
  }

  public double getMoneyToWin(double score) {
    double moneyKey = saveType.isHigherScore() ? 0 : Double.MAX_VALUE;
    for (double key : moneyToWin.keySet()) {
      if (scoreIsBetterThen(key, score)) continue;
      if (scoreIsBetterThen(moneyKey, score)) continue;
      moneyKey = key;
    }
    Double money = moneyToWin.get(moneyKey);
    if (money == null) return 0;
    return money;
  }

  private boolean scoreIsBetterThen(double score, double compare) {
    return ((saveType.isHigherScore() && compare < score)
            || (!saveType.isHigherScore() && compare > score));
  }
}
