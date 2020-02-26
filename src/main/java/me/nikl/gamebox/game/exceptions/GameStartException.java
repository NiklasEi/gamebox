package me.nikl.gamebox.game.exceptions;

/**
 * @author Niklas Eicker
 * <p>
 * Exception thrown by a game if starting a new game fails
 */
public class GameStartException extends GameException {
  private Reason reason;

  public GameStartException(Reason reason) {
    super();
    this.reason = reason;
  }

  public Reason getReason() {
    return this.reason;
  }

  public enum Reason {
    NOT_ENOUGH_MONEY,
    NOT_ENOUGH_MONEY_FIRST_PLAYER,
    NOT_ENOUGH_MONEY_SECOND_PLAYER,
    ERROR
  }
}
