package me.nikl.gamebox.games.matchit;

import me.nikl.gamebox.game.Game;
import me.nikl.gamebox.game.GameLanguage;

/**
 * Created by nikl on 02.12.17.
 */
public class MILanguage extends GameLanguage {

    public String INV_TITLE_GAME, INV_TITLE_START, INV_TITLE_WON;

    public MILanguage(Game game) {
        super(game);
    }

    @Override
    protected void loadMessages() {
        INV_TITLE_GAME = getString("game.inventoryTitles.gameTitle");
        INV_TITLE_START = getString("game.inventoryTitles.gameTitleStart");
        INV_TITLE_WON = getString("game.inventoryTitles.gameTitleWon");
    }
}
