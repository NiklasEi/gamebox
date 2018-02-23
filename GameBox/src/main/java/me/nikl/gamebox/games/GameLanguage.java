package me.nikl.gamebox.games;

import me.nikl.gamebox.Language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Hold some default messages that are needed by any game
 *
 * The messages are overwritten in the specific Language files
 */
public abstract class GameLanguage extends Language {

    public List<String> GAME_HELP = new ArrayList<>(Arrays.asList("Have fun playing"));

    public String GAME_PAYED = " You payed %cost%", GAME_NOT_ENOUGH_MONEY = " Not enough money (%cost%)";

    public GameLanguage(Game game) {
        super(game.getGameBox(), game.getModule());

        this.GAME_HELP = getStringList("gameHelp");
    }
}
