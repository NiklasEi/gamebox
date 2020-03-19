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
  private String moduleID;
  private Class<Game> gameClass;
  private boolean isGame = false;
  private File jarFile;
  private List<String> subCommands;

  public GameBoxGame(GameBox gameBox, String moduleID, Class<Game> gameClass, File jarFile, String... subCommands) {
    Validate.isTrue(moduleID != null && !moduleID.isEmpty()
            , " moduleID cannot be null or empty!");
    if (gameClass != null) {
      this.isGame = true;
    }
    if (subCommands != null && !(subCommands.length < 1)) {
      this.subCommands = Arrays.asList(subCommands);
    }
    this.gameClass = gameClass;
    this.moduleID = moduleID.toLowerCase();
    this.jarFile = jarFile;
    gameBox.getGameRegistry().registerModule(this);
  }

  public String getModuleID() {
    return moduleID;
  }

  public Class<Game> getGameClass() {
    return gameClass;
  }

  public boolean isGame() {
    return isGame;
  }

  @Override
  public boolean equals(Object module) {
    if (!(module instanceof GameBoxGame)) {
      return false;
    }
    return moduleID.equalsIgnoreCase(((GameBoxGame) module).moduleID);
  }

  @Override
  public int hashCode() {
    return moduleID.hashCode();
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
