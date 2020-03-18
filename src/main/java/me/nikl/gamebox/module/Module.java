package me.nikl.gamebox.module;

import me.nikl.gamebox.GameBox;
import org.apache.commons.lang.Validate;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author Niklas Eicker
 */
public class Module {
  private String moduleID, classPath;
  private boolean isGame = false;
  private File jarFile;
  private List<String> subCommands;

  public Module(GameBox gameBox, String moduleID, String classPath, File jarFile, String... subCommands) {
    Validate.isTrue(moduleID != null && !moduleID.isEmpty()
            , " moduleID cannot be null or empty!");
    if (classPath != null && !classPath.isEmpty()) {
      this.isGame = true;
    }
    if (subCommands != null && !(subCommands.length < 1)) {
      this.subCommands = Arrays.asList(subCommands);
    }
    this.classPath = classPath;
    this.moduleID = moduleID.toLowerCase();
    this.jarFile = jarFile;
    gameBox.getGameRegistry().registerModule(this);
  }

  public String getModuleID() {
    return moduleID;
  }

  public String getClassPath() {
    return classPath;
  }

  public boolean isGame() {
    return isGame;
  }

  @Override
  public boolean equals(Object module) {
    if (!(module instanceof Module)) {
      return false;
    }
    return moduleID.equalsIgnoreCase(((Module) module).moduleID);
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
