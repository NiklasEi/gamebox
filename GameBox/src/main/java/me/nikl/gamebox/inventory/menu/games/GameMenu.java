package me.nikl.gamebox.inventory.menu.games;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Module;
import me.nikl.gamebox.games.Game;
import me.nikl.gamebox.inventory.menu.PerPlayerMenu;
import me.nikl.gamebox.utility.StringUtility;

/**
 * Created by nikl on 21.02.18.
 */
public abstract class GameMenu extends PerPlayerMenu {
    private Module module;
    public GameMenu(GameBox gameBox, Game game) {
        super(gameBox);
        this.module = game.getModule();
        this.defaultTitle = StringUtility.center(game.getGameLang().NAME, 32);
    }

    public String getModuleID(){
        return module.getModuleID();
    }
}
