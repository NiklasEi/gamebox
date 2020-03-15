package me.nikl.gamebox;

import me.nikl.gamebox.game.Game;
import me.nikl.gamebox.game.exceptions.GameLoadException;
import me.nikl.gamebox.module.NewGameBoxModule;
import me.nikl.gamebox.utility.FileUtility;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author Niklas Eicker
 */
public class GameRegistry {
  private final Set<String> forbiddenIDs =
          new HashSet<>(Arrays.asList("all, game, games, info, token, t"));
  private final Set<String> forbiddenSubCommands =
          new HashSet<>(Arrays.asList("all, game, games, info, token, t"));
  private final Set<String> disabledModules = new HashSet<>();
  private GameBox gameBox;
  private Map<String, NewGameBoxModule> modules = new HashMap<>();
  private Map<String, NewGameBoxModule> declinedModules = new HashMap<>();
  private Map<String, NewGameBoxModule> subCommands = new HashMap<>();
  private Map<String, Integer> preferredMainMenuSlots = new HashMap<>();
  private Map<NewGameBoxModule, Set<String>> bundledSubCommands = new HashMap<>();
  private boolean enableNewGamesByDefault;
  private FileConfiguration gamesConfiguration;
  private File gamesFile;

  public GameRegistry(GameBox plugin) {
    this.gameBox = plugin;
  }

  private void loadDisabledModules() {
    disabledModules.clear();
    enableNewGamesByDefault = gamesConfiguration.getBoolean("enableNewGamesByDefault", true);
    ConfigurationSection gamesSection = gamesConfiguration.getConfigurationSection("games");
    if (gamesSection == null) return;
    for (String moduleID : gamesSection.getKeys(false)) {
      if (!gamesSection.getBoolean(moduleID + ".enabled", true)) {
        GameBox.debug("Set " + moduleID + " as disabled");
        disabledModules.add(moduleID);
      }
    }
  }

  public void reloadGamesConfiguration() {
    gamesFile = new File(gameBox.getDataFolder().toString() + File.separatorChar + "games.yml");
    if (!gamesFile.exists()) {
      gameBox.saveResource("games.yml", false);
    }
    try {
      gamesConfiguration = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(gamesFile), "UTF-8"));
    } catch (UnsupportedEncodingException | FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public boolean registerModule(NewGameBoxModule gameBoxModule) {
    if (isRegistered(gameBoxModule.getModuleID())) {
      gameBox.getLogger().log(Level.WARNING, "A Module tried registering with an already in use ID!");
      return false;
    }
    if (forbiddenIDs.contains(gameBoxModule.getModuleID())) {
      gameBox.getLogger().log(Level.WARNING, "A Module tried registering with a forbidden ID (" + gameBoxModule.getModuleID() + ")");
      return false;
    }
    if (disabledModules.contains(gameBoxModule.getModuleID())) {
      declinedModules.put(gameBoxModule.getModuleID(), gameBoxModule);
      gameBox.warning("The game " + gameBoxModule.getModuleID() + " is disabled in 'games.yml'");
      return false;
    }
    if (!gameBoxModule.getModuleID().equals(GameBoxSettings.getGameBoxModuleInfo().getId()))
      handleModuleSettings(gameBoxModule);
    modules.put(gameBoxModule.getModuleID(), gameBoxModule);
    // Todo: copy default files
    /*if (gameBoxModule.getExternalPlugin() != null) {
      if (!FileUtility.copyExternalResources(gameBox, gameBoxModule)) {
        gameBox.info(" Failed to register the external module '" + gameBoxModule.getModuleID() + "'");
        modules.remove(gameBoxModule.getModuleID());
        return false;
      }
    }*/
    registerSubCommands(gameBoxModule);
    return true;
  }

  private void handleModuleSettings(NewGameBoxModule gameBoxModule) {
    String moduleID = gameBoxModule.getModuleID();
    if (!gamesConfiguration.isSet("games." + moduleID)) {
      setDefaultModuleSettings(gameBoxModule);
      return;
    } else {
      // overwrite default sub commands
      if (gamesConfiguration.isList("games." + moduleID + ".subCommands")) {
        List<String> subCommands = gamesConfiguration.getStringList("games." + moduleID + ".subCommands");
        // Todo: overwrite predefined subcommands from local gamesConfiguration
        //if (subCommands != null && !subCommands.isEmpty()) gameBoxModule.setSubCommands(subCommands);
      }
      preferredMainMenuSlots.put(moduleID, gamesConfiguration.getInt("games." + moduleID + ".preferredSlot", -1));
    }
  }

  private void setDefaultModuleSettings(NewGameBoxModule gameBoxModule) {
    String moduleID = gameBoxModule.getModuleID();
    gamesConfiguration.set("games." + moduleID + ".enabled", enableNewGamesByDefault);
    gamesConfiguration.set("games." + moduleID + ".subCommands", gameBoxModule.getSubCommands());
    gamesConfiguration.set("games." + moduleID + ".preferredSlot", -1);
    saveGameSettings();
  }

  public boolean isRegistered(NewGameBoxModule gameBoxModule) {
    return isRegistered(gameBoxModule.getModuleID());
  }

  public boolean isRegistered(String moduleID) {
    return modules.containsKey(moduleID.toLowerCase());
  }

  public NewGameBoxModule getModule(String moduleID) {
    return modules.get(moduleID);
  }

  /**
   * Reload the settings. Then go through all modules and
   * try getting game instances through their class paths
   */
  public void reload() {
    reloadGamesConfiguration();
    loadDisabledModules();
    modules.putAll(declinedModules);
    declinedModules.clear();
    subCommands.clear();
    bundledSubCommands.clear();
    Iterator<NewGameBoxModule> iterator = modules.values().iterator();
    while (iterator.hasNext()) {
      NewGameBoxModule gameBoxModule = iterator.next();
      if (disabledModules.contains(gameBoxModule.getModuleID())) {
        iterator.remove();
        declinedModules.put(gameBoxModule.getModuleID(), gameBoxModule);
        gameBox.warning("The game " + gameBoxModule.getModuleID() + " is disabled in 'games.yml'");
        continue;
      }
      // Todo Move reloading to ModulesManager
      GameBox.debug("Reloading not supported atm");
    }
  }

  private void reloadGameData(NewGameBoxModule gameBoxModule) {
    String moduleID = gameBoxModule.getModuleID();
    if (gamesConfiguration.isList("games." + moduleID + ".subCommands")) {
      List<String> subCommands = gamesConfiguration.getStringList("games." + moduleID + ".subCommands");
      // Todo: overwrite predefined subcommands from local gamesConfiguration
      //if (subCommands != null && !subCommands.isEmpty()) gameBoxModule.setSubCommands(subCommands);
    }
    preferredMainMenuSlots.put(moduleID, gamesConfiguration.getInt("games." + moduleID + ".preferredSlot", -1));
    registerSubCommands(gameBoxModule);
  }

  public Set<String> getModuleIDs() {
    return Collections.unmodifiableSet(modules.keySet());
  }

  public Set<NewGameBoxModule> getModules() {
    return Collections.unmodifiableSet(new HashSet<>(modules.values()));
  }

  public Set<String> getModuleSubCommands(NewGameBoxModule gameBoxModule) {
    return Collections.unmodifiableSet(bundledSubCommands.get(gameBoxModule));
  }

  private void registerSubCommands(NewGameBoxModule gameBoxModule) {
    if (gameBoxModule.getSubCommands() == null || gameBoxModule.getSubCommands().isEmpty()) {
      bundledSubCommands.put(gameBoxModule, new HashSet<>());
      return;
    }
    List<String> subCommands = gameBoxModule.getSubCommands();
    for (int i = 0; i < subCommands.size(); i++) {
      subCommands.set(i, subCommands.get(i).toLowerCase());
    }
    // ensure that sub commands are unique and valid
    for (int i = 0; i < subCommands.size(); i++) {
      if (forbiddenSubCommands.contains(subCommands.get(i)))
        throw new IllegalArgumentException("Forbidden sub command: " + subCommands.get(i));
      if (this.subCommands.keySet().contains(subCommands.get(i)))
        continue;
      this.subCommands.put(subCommands.get(i), gameBoxModule);
      addSubCommandToBundle(gameBoxModule, subCommands.get(i));
    }
  }

  private void addSubCommandToBundle(NewGameBoxModule gameBoxModule, String subCommand) {
    bundledSubCommands.putIfAbsent(gameBoxModule, new HashSet<>());
    bundledSubCommands.get(gameBoxModule).add(subCommand);
  }

  public NewGameBoxModule getModuleBySubCommand(String subCommand) {
    GameBox.debug("grab module of " + subCommand);
    return subCommands.get(subCommand);
  }

  public void unregisterGame(String gameID) {
    NewGameBoxModule gameBoxModule = modules.get(gameID);
    if (gameBoxModule == null) return;
    Set<String> subCommands = bundledSubCommands.get(gameBoxModule);
    modules.remove(gameID);
    if (subCommands == null || subCommands.isEmpty()) return;
    for (String subCommand : subCommands) {
      GameBox.debug("   remove " + subCommand);
      this.subCommands.remove(subCommand);
    }
    bundledSubCommands.remove(gameID);
  }

  public void disableGame(String gameID) {
    disabledModules.add(gameID);
    gamesConfiguration.set("games." + gameID + ".enabled", false);
    saveGameSettings();
  }

  public void enableGame(String gameID) {
    disabledModules.remove(gameID);
    gamesConfiguration.set("games." + gameID + ".enabled", true);
    saveGameSettings();
  }

  private void saveGameSettings() {
    try {
      gamesConfiguration.save(gamesFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public int getPreferredMainMenuSlot(String moduleID) {
    return preferredMainMenuSlots.getOrDefault(moduleID, -1);
  }

  public boolean isDisabledModule(String moduleID) {
    return disabledModules.contains(moduleID);
  }

  public Set<String> getSubcommands() {
    return Collections.unmodifiableSet(subCommands.keySet());
  }
}
