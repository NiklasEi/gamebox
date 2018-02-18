package me.nikl.gamebox.games.exceptions;

import me.nikl.gamebox.games.exceptions.GameException;

/**
 * Created by nikl on 17.02.18.
 */
public class GameStartException extends GameException {
    private Reason reason;

    public GameStartException(Reason reason){
        this.reason = reason;
    }

    public Reason getReason() {
        return this.reason;
    }

    public enum Reason{
        NOT_ENOUGH_MONEY,
        NOT_ENOUGH_MONEY_FIRST_PLAYER,
        NOT_ENOUGH_MONEY_SECOND_PLAYER,
        ERROR
    }
}
