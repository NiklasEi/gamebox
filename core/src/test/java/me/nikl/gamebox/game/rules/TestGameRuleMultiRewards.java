package me.nikl.gamebox.game.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import me.nikl.gamebox.data.toplist.SaveType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    @DisplayName("Win zero money for empty game rule")
    void returnZeroMoneyForEmptyRule() {
        GameRuleMultiRewards gameRuleMultiRewards = new GameRuleMultiRewards("test", false, SaveType.HIGH_NUMBER_SCORE, 0);
        assertEquals(0, gameRuleMultiRewards.getMoneyToWin(1.));
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
}
