package me.nikl.gamebox.games.connectfour;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Module;
import me.nikl.gamebox.games.GameLanguage;

/**
 * Class loading and storing messages.
 *
 * All messages are loaded from the configured language file
 * and if not present there from the english file included in the jar.
 * Color codes are translated on load.
 */
public class CFLanguage extends GameLanguage{

	public String GAME_NOT_ENOUGH_MONEY, GAME_PAYED, GAME_WON_MONEY, GAME_WON_MONEY_GAVE_UP
			, GAME_WON, GAME_LOSE, GAME_GAVE_UP, GAME_OTHER_GAVE_UP, TITLE_DRAW;
	public String TITLE_IN_GAME_YOUR_TURN, TITLE_IN_GAME_OTHERS_TURN, TITLE_WON, TITLE_LOST;
	
	public CFLanguage(GameBox gameBox){
		super(gameBox, "connectfour");
	}

	@Override
	protected void loadMessages() {
		getGameLang();
	}

	private void getGameLang() {
		this.GAME_NOT_ENOUGH_MONEY = getString("game.econ.notEnoughMoney");
		this.GAME_PAYED = getString("game.econ.payed");
		this.GAME_WON_MONEY = getString("game.econ.wonMoney");
		this.GAME_WON_MONEY_GAVE_UP = getString("game.econ.wonMoneyGaveUp");

		this.GAME_WON = getString("game.won");
		this.GAME_LOSE = getString("game.lost");
		this.GAME_GAVE_UP = getString("game.gaveUp");
		this.GAME_OTHER_GAVE_UP = getString("game.otherGaveUp");

		this.TITLE_IN_GAME_YOUR_TURN = getString("game.inventoryTitles.ingame1");
		this.TITLE_IN_GAME_OTHERS_TURN = getString("game.inventoryTitles.ingame2");
		this.TITLE_WON = getString("game.inventoryTitles.won");
		this.TITLE_LOST = getString("game.inventoryTitles.lost");
		this.TITLE_DRAW = getString("game.inventoryTitles.draw");
	}
}

