package me.nikl.gamebox.module;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.game.Game;
import org.apache.commons.lang.Validate;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author Niklas Eicker
 */
public class GameBoxGame {
  private String gameId;
  private Class<Game> gameClass;
  private File jarFile;
  private List<String> subCommands;

  public GameBoxGame(GameBox gameBox, String gameId, Class<Game> gameClass, File jarFile, String... subCommands) {
    Validate.isTrue(gameId != null && !gameId.isEmpty()
            , " moduleID cannot be null or empty!");
    Validate.isTrue(gameClass != null, " gameClass cannot be null!");
    if (subCommands != null && !(subCommands.length < 1)) {
      this.subCommands = Arrays.asList(subCommands);
    }
    this.gameClass = gameClass;
    this.gameId = gameId.toLowerCase();
    this.jarFile = jarFile;
    gameBox.getGameRegistry().registerGame(this);
  }

  public String getGameId() {
    return gameId;
  }

  public Class<Game> getGameClass() {
    return gameClass;
  }

  @Override
  public boolean equals(Object module) {
    if (!(module instanceof GameBoxGame)) {
      return false;
    }
    return gameId.equalsIgnoreCase(((GameBoxGame) module).gameId);
  }

  @Override
  public int hashCode() {
    return gameId.hashCode();
  }

  public List<String> getSubCommands() {
    return subCommands;
  }

  public void setSubCommands(List<String> subCommands) {
    this.subCommands = subCommands;
  }

  public File getJarFile() {
    return jarFile;
  }
}
