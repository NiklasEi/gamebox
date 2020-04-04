package me.nikl.gamebox.game.rules;

import me.nikl.gamebox.data.toplist.SaveType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Niklas Eicker
 */
public class TestGameRuleMultiRewards {
  @Test
  @DisplayName("Win zero token for empty game rule")
  void returnZeroTokenForEmptyRule() {
    GameRuleMultiRewards gameRuleMultiRewards = new GameRuleMultiRewards("test", false, SaveType.HIGH_NUMBER_SCORE, 0);
    assertEquals(0, gameRuleMultiRewards.getTokenToWin(1.));
  }

  @Test
  @DisplayName("Fall back to zero if score too low")
  void returnZeroIfScoreTooLow() {
    GameRuleMultiRewards gameRuleMultiRewards = new GameRuleMultiRewards("test", false, SaveType.HIGH_NUMBER_SCORE, 0);
    gameRuleMultiRewards.addMoneyReward(5., 1.);
    gameRuleMultiRewards.addTokenReward(5., 1);
    assertEquals(0, gameRuleMultiRewards.getMoneyToWin(1.));
    assertEquals(0, gameRuleMultiRewards.getTokenToWin(1.));
  }

  @Test
  @DisplayName("Get money and token if score is high enough")
  void returnTokenAndMoneyIfScoreHighEnough() {
    GameRuleMultiRewards gameRuleMultiRewards = new GameRuleMultiRewards("test", false, SaveType.HIGH_NUMBER_SCORE, 0);
    gameRuleMultiRewards.addMoneyReward(5., 5.);
    gameRuleMultiRewards.addTokenReward(5., 1);
    assertEquals(5, gameRuleMultiRewards.getMoneyToWin(5.));
    assertEquals(1, gameRuleMultiRewards.getTokenToWin(5.));
  }

  @Test
  @DisplayName("Get money and token if score is between rules")
  void returnCorrectTokenAndMoney() {
    GameRuleMultiRewards gameRuleMultiRewards = new GameRuleMultiRewards("test", false, SaveType.HIGH_NUMBER_SCORE, 0);
    gameRuleMultiRewards.addMoneyReward(5., 5.);
    gameRuleMultiRewards.addTokenReward(5., 1);
    gameRuleMultiRewards.addMoneyReward(100, 50.);
    gameRuleMultiRewards.addTokenReward(100., 10);
    gameRuleMultiRewards.addMoneyReward(1000., 500.);
    gameRuleMultiRewards.addTokenReward(1000., 100);
    assertEquals(50, gameRuleMultiRewards.getMoneyToWin(200));
    assertEquals(10, gameRuleMultiRewards.getTokenToWin(200));
  }

  @Test
  @DisplayName("Get correct reward from extensive multi rewards")
  void getCorrectRewardsFromExtensiveMultiReward() {
    GameRuleMultiRewards gameRuleMultiRewards = new GameRuleMultiRewards("test", false, SaveType.HIGH_NUMBER_SCORE, 0);
    gameRuleMultiRewards.addMoneyReward(0, 0);
    gameRuleMultiRewards.addMoneyReward(750, 10);
    gameRuleMultiRewards.addMoneyReward(900, 15);
    gameRuleMultiRewards.addMoneyReward(1000, 20);
    gameRuleMultiRewards.addMoneyReward(1250, 25);
    gameRuleMultiRewards.addMoneyReward(1500, 30);
    gameRuleMultiRewards.addMoneyReward(1750, 35);
    gameRuleMultiRewards.addMoneyReward(2000, 40);
    gameRuleMultiRewards.addMoneyReward(2250, 45);
    gameRuleMultiRewards.addMoneyReward(2500, 50);
    assertEquals(25, gameRuleMultiRewards.getMoneyToWin(1250));
  }
}
