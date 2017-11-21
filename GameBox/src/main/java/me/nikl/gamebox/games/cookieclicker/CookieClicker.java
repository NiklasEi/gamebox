package me.nikl.gamebox.games.cookieclicker;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Module;
import me.nikl.gamebox.games.Game;
import me.nikl.gamebox.games.GameSettings;

/**
 * Created by Niklas
 *
 * Main class of the GameBox game Cookie Clicker
 */
public class CookieClicker extends Game {

    public CookieClicker(GameBox gameBox) {
        super(gameBox, "cookieclicker", new String[]{"cookies", "cc"});
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void loadSettings() {
        gameSettings.setGameType(GameSettings.GameType.SINGLE_PLAYER);
        gameSettings.setGameGuiSize(54);
        gameSettings.setHandleClicksOnHotbar(false);
    }

    @Override
    public void loadLanguage() {
        this.gameLang = new CCLanguage(gameBox);
    }

    @Override
    public void loadGameManager() {
        this.gameManager = new CCGameManager(this);
    }
}
