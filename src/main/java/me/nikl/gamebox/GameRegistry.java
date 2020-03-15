package me.nikl.gamebox;

import me.nikl.gamebox.game.Game;
import me.nikl.gamebox.game.exceptions.GameLoadException;
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
  private Map<String, GameBoxModule> modules = new HashMap<>();
  private Map<String, GameBoxModule> declinedModules = new HashMap<>();
  private Map<String, GameBoxModule> subCommands = new HashMap<>();
  private Map<String, Integer> preferredMainMenuSlots = new HashMap<>();
  private Map<GameBoxModule, Set<String>> bundledSubCommands = new HashMap<>();
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

  public boolean registerModule(GameBoxModule gameBoxModule) {
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
    if (!gameBoxModule.getModuleID().equals(GameBox.MODULE_GAMEBOX))
      handleModuleSettings(gameBoxModule);
    modules.put(gameBoxModule.getModuleID(), gameBoxModule);
    if (gameBoxModule.getExternalPlugin() != null) {
      if (!FileUtility.copyExternalResources(gameBox, gameBoxModule)) {
        gameBox.info(" Failed to register the external module '" + gameBoxModule.getModuleID() + "'");
        modules.remove(gameBoxModule.getModuleID());
        return false;
      }
    }
    if (gameBoxModule.isGame()) {
      loadGame(gameBoxModule);
      registerSubCommands(gameBoxModule);
    }
    return true;
  }

  private void handleModuleSettings(GameBoxModule gameBoxModule) {
    String moduleID = gameBoxModule.getModuleID();
    if (!gamesConfiguration.isSet("games." + moduleID)) {
      setDefaultModuleSettings(gameBoxModule);
      return;
    } else {
      // overwrite default sub commands
      if (gameBoxModule.isGame() && gamesConfiguration.isList("games." + moduleID + ".subCommands")) {
        List<String> subCommands = gamesConfiguration.getStringList("games." + moduleID + ".subCommands");
        if (subCommands != null && !subCommands.isEmpty()) gameBoxModule.setSubCommands(subCommands);
      }
      preferredMainMenuSlots.put(moduleID, gamesConfiguration.getInt("games." + moduleID + ".preferredSlot", -1));
    }
  }

  private void setDefaultModuleSettings(GameBoxModule gameBoxModule) {
    String moduleID = gameBoxModule.getModuleID();
    gamesConfiguration.set("games." + moduleID + ".enabled", enableNewGamesByDefault);
    gamesConfiguration.set("games." + moduleID + ".subCommands", gameBoxModule.getSubCommands());
    gamesConfiguration.set("games." + moduleID + ".preferredSlot", -1);
    saveGameSettings();
  }

  public boolean isRegistered(GameBoxModule gameBoxModule) {
    return isRegistered(gameBoxModule.getModuleID());
  }

  public boolean isRegistered(String moduleID) {
    return modules.containsKey(moduleID.toLowerCase());
  }

  public GameBoxModule getModule(String moduleID) {
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
    Iterator<GameBoxModule> iterator = modules.values().iterator();
    while (iterator.hasNext()) {
      GameBoxModule gameBoxModule = iterator.next();
      if (disabledModules.contains(gameBoxModule.getModuleID())) {
        iterator.remove();
        declinedModules.put(gameBoxModule.getModuleID(), gameBoxModule);
        gameBox.warning("The game " + gameBoxModule.getModuleID() + " is disabled in 'games.yml'");
        continue;
      }
      if (gameBoxModule.isGame()) {
        reloadGameData(gameBoxModule);
        loadGame(gameBoxModule);
      }
    }
  }

  private void reloadGameData(GameBoxModule gameBoxModule) {
    String moduleID = gameBoxModule.getModuleID();
    if (gamesConfiguration.isList("games." + moduleID + ".subCommands")) {
      List<String> subCommands = gamesConfiguration.getStringList("games." + moduleID + ".subCommands");
      if (subCommands != null && !subCommands.isEmpty()) gameBoxModule.setSubCommands(subCommands);
    }
    preferredMainMenuSlots.put(moduleID, gamesConfiguration.getInt("games." + moduleID + ".preferredSlot", -1));
    registerSubCommands(gameBoxModule);
  }

  private void loadGame(GameBoxModule gameBoxModule) {
    Class<Game> clazz = null;
    try {
      clazz = (Class<Game>) Class.forName(gameBoxModule.getClassPath());
    } catch (ClassNotFoundException | ClassCastException e) {
      e.printStackTrace();
    }
    if (clazz == null) return;
    try {
      Constructor<Game> ctor = clazz.getConstructor(GameBox.class);
      Game game = ctor.newInstance(gameBox);
      gameBox.getPluginManager().registerGame(game);
      game.onEnable();
    } catch (NoSuchMethodException | IllegalAccessException
            | InstantiationException | InvocationTargetException e) {
      gameBox.warning(" The game class of '" + gameBoxModule.getModuleID() + "' needs a public constructor taking only a GameBox object!");
      e.printStackTrace();
      gameBox.getPluginManager().unregisterGame(gameBoxModule.getModuleID());
    } catch (GameLoadException e) {
      gameBox.warning(" Exception while loading '" + gameBoxModule.getModuleID() + "'!");
      e.printStackTrace();
      gameBox.getPluginManager().unregisterGame(gameBoxModule.getModuleID());
    } catch (Throwable throwable) {
      gameBox.warning("unexpected error while loading '" + gameBoxModule.getModuleID() + "'...");
      throwable.printStackTrace();
      gameBox.getPluginManager().unregisterGame(gameBoxModule.getModuleID());
    }
  }

  public Set<String> getModuleIDs() {
    return Collections.unmodifiableSet(modules.keySet());
  }

  public Set<GameBoxModule> getModules() {
    return Collections.unmodifiableSet(new HashSet<>(modules.values()));
  }

  public Set<String> getModuleSubCommands(GameBoxModule gameBoxModule) {
    return Collections.unmodifiableSet(bundledSubCommands.get(gameBoxModule));
  }

  private void registerSubCommands(GameBoxModule gameBoxModule) {
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

  private void addSubCommandToBundle(GameBoxModule gameBoxModule, String subCommand) {
    bundledSubCommands.putIfAbsent(gameBoxModule, new HashSet<>());
    bundledSubCommands.get(gameBoxModule).add(subCommand);
  }

  public GameBoxModule getModuleBySubCommand(String subCommand) {
    GameBox.debug("grab module of " + subCommand);
    return subCommands.get(subCommand);
  }

  public void unregisterGame(String gameID) {
    GameBoxModule gameBoxModule = modules.get(gameID);
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
