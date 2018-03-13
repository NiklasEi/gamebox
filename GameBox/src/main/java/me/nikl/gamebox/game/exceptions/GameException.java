package me.nikl.gamebox.game.exceptions;

/**
 * @author Niklas Eicker
 */
public class GameException extends Exception {
    public GameException(String message, Throwable e) {
        super(message, e);
    }

    // default constructor
    public GameException() {}

    public GameException(String message) {
        super(message);
    }
}
