package me.nikl.gamebox.game;

import me.nikl.gamebox.Language;

import java.util.List;

/**
 * Hold some default messages that are needed by any game
 * <p>
 * The messages are overwritten in the specific Language files
 */
public abstract class GameLanguage extends Language {
  public List<String> GAME_HELP;
  public String GAME_PAYED;
  public String GAME_NOT_ENOUGH_MONEY;

  public GameLanguage(Game game) {
    super(game.getGameBox(), game.getModule().getGameId(), game.getModule().getJarFile());

    this.GAME_PAYED = getString("game.econ.payed");
    this.GAME_NOT_ENOUGH_MONEY = getString("game.econ.notEnoughMoney");
    this.GAME_HELP = getStringList("gameHelp");
  }
}
