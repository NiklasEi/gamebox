package me.nikl.gamebox.game.exceptions;

/**
 * @author Niklas Eicker
 */
public class GameLoadException extends GameException {
    public GameLoadException(String message, Throwable e) {
        super(message, e);
    }

    public GameLoadException(String message) {
        super(message);
    }
}
