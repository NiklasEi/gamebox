package me.nikl.gamebox.games.matchit;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.games.Game;
import me.nikl.gamebox.games.GameSettings;

/**
 * Created by nikl on 02.12.17.
 */
public class MatchIt extends Game{

    public MatchIt(GameBox gameBox) {
        super(gameBox, GameBox.MODULE_MATCHIT
                , new String[]{GameBox.MODULE_MATCHIT, "mi"});
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
        this.gameLang = new MILanguage(gameBox);
    }

    @Override
    public void loadGameManager() {
        this.gameManager = new MIGameManager(this);
    }
}
