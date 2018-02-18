package me.nikl.gamebox.games;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Language;
import me.nikl.gamebox.Module;

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

    public GameLanguage(GameBox plugin, Module module) {
        super(plugin, module);

        this.GAME_HELP = getStringList("gameHelp");
    }

    public GameLanguage(GameBox plugin, String moduleID) {
        super(plugin, moduleID);

        this.GAME_HELP = getStringList("gameHelp");
    }
}
