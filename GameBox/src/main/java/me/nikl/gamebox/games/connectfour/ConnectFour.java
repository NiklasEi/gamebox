package me.nikl.gamebox.games.connectfour;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.games.Game;
import me.nikl.gamebox.games.GameSettings;
import me.nikl.gamebox.util.Module;

/**
 * Created by Niklas on 14.04.2017.
 *
 * Main class for the GameBox game 2048
 */
public class ConnectFour extends Game {

    public ConnectFour(GameBox gameBox) {
        super(gameBox, Module.CONNECTFOUR, new String[]{"connectfour", "connect4", "c4"});
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void loadSettings() {
        gameSettings.setGameType(GameSettings.GameType.TWO_PLAYER);
        gameSettings.setGameGuiSize(54);
        gameSettings.setHandleClicksOnHotbar(false);
    }

    @Override
    public void loadLanguage() {
        this.gameLang = new CFLanguage(gameBox);
    }

    @Override
    public void loadGameManager() {
        this.gameManager = new CFGameManager(this);
    }
}
