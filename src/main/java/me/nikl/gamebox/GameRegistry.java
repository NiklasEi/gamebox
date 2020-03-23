package me.nikl.gamebox;

import me.nikl.gamebox.game.Game;
import me.nikl.gamebox.game.exceptions.GameLoadException;
import me.nikl.gamebox.module.GameBoxGame;
import me.nikl.gamebox.utility.FileUtility;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
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
          new HashSet<>(Arrays.asList("all", "game", "games", "info", "token", "t"));
  private final Set<String> forbiddenSubCommands =
          new HashSet<>(Arrays.asList("all", "game", "games", "info", "token", "t"));
  private final Set<String> disabledModules = new HashSet<>();
  private GameBox gameBox;
  private Map<String, GameBoxGame> modules = new HashMap<>();
  private Map<String, GameBoxGame> declinedModules = new HashMap<>();
  private Map<String, GameBoxGame> subCommands = new HashMap<>();
  private Map<GameBoxGame, Set<String>> bundledSubCommands = new HashMap<>();
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
      gamesConfiguration = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(gamesFile), StandardCharsets.UTF_8));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public boolean registerModule(GameBoxGame module) {
    if (isRegistered(module.getModuleID())) {
      gameBox.getLogger().log(Level.WARNING, "A Module tried registering with an already in use ID!");
      return false;
    }
    if (forbiddenIDs.contains(module.getModuleID())) {
      gameBox.getLogger().log(Level.WARNING, "A Module tried registering with a forbidden ID (" + module.getModuleID() + ")");
      return false;
    }
    if (disabledModules.contains(module.getModuleID())) {
      declinedModules.put(module.getModuleID(), module);
      gameBox.warning("The game " + module.getModuleID() + " is disabled in 'games.yml'");
      return false;
    }
    if (!module.getModuleID().equals(GameBox.MODULE_GAMEBOX))
      handleModuleSettings(module);
      modules.put(module.getModuleID(), module);
    if (module.getJarFile() != null) {
      if (!FileUtility.copyExternalResources(gameBox, module)) {
        gameBox.info(" Failed to register the external module '" + module.getModuleID() + "'");
        modules.remove(module.getModuleID());
        return false;
      }
    }
    if (module.isGame()) {
      loadGame(module);
      registerSubCommands(module);
    }
    return true;
  }

  private void handleModuleSettings(GameBoxGame module) {
    String moduleID = module.getModuleID();
    if (!gamesConfiguration.isSet("games." + moduleID)) {
      setDefaultModuleSettings(module);
    } else {
      // overwrite default sub commands
      if (module.isGame() && gamesConfiguration.isList("games." + moduleID + ".subCommands")) {
        List<String> subCommands = gamesConfiguration.getStringList("games." + moduleID + ".subCommands");
        if (subCommands != null && !subCommands.isEmpty()) module.setSubCommands(subCommands);
      }
    }
  }

  private void setDefaultModuleSettings(GameBoxGame module) {
    String moduleID = module.getModuleID();
    gamesConfiguration.set("games." + moduleID + ".enabled", enableNewGamesByDefault);
    gamesConfiguration.set("games." + moduleID + ".subCommands", module.getSubCommands());
    saveGameSettings();
  }

  public boolean isRegistered(GameBoxGame module) {
    return isRegistered(module.getModuleID());
  }

  public boolean isRegistered(String moduleID) {
    return modules.containsKey(moduleID.toLowerCase());
  }

  public GameBoxGame getModule(String moduleID) {
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
    Iterator<GameBoxGame> iterator = modules.values().iterator();
    while (iterator.hasNext()) {
      GameBoxGame module = iterator.next();
      if (disabledModules.contains(module.getModuleID())) {
        iterator.remove();
        declinedModules.put(module.getModuleID(), module);
        gameBox.warning("The game " + module.getModuleID() + " is disabled in 'games.yml'");
        continue;
      }
      if (module.isGame()) {
        reloadGameData(module);
        loadGame(module);
      }
    }
  }

  private void reloadGameData(GameBoxGame module) {
    String moduleID = module.getModuleID();
    if (gamesConfiguration.isList("games." + moduleID + ".subCommands")) {
      List<String> subCommands = gamesConfiguration.getStringList("games." + moduleID + ".subCommands");
      if (subCommands != null && !subCommands.isEmpty()) module.setSubCommands(subCommands);
    }
    registerSubCommands(module);
  }

  private void loadGame(GameBoxGame module) {
    Class<Game> clazz = null;
    try {
      clazz = module.getGameClass();
    } catch ( ClassCastException e) {
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
      gameBox.warning(" The game class of '" + module.getModuleID() + "' needs a public constructor taking only a GameBox object!");
      e.printStackTrace();
      gameBox.getPluginManager().unregisterGame(module.getModuleID());
    } catch (GameLoadException e) {
      gameBox.warning(" Exception while loading '" + module.getModuleID() + "'!");
      e.printStackTrace();
      gameBox.getPluginManager().unregisterGame(module.getModuleID());
    } catch (Throwable throwable) {
      gameBox.warning("unexpected error while loading '" + module.getModuleID() + "'...");
      throwable.printStackTrace();
      gameBox.getPluginManager().unregisterGame(module.getModuleID());
    }
  }

  public Set<String> getModuleIDs() {
    return Collections.unmodifiableSet(modules.keySet());
  }

  public Set<GameBoxGame> getModules() {
    return Collections.unmodifiableSet(new HashSet<>(modules.values()));
  }

  public Set<String> getModuleSubCommands(GameBoxGame module) {
    return Collections.unmodifiableSet(bundledSubCommands.get(module));
  }

  private void registerSubCommands(GameBoxGame module) {
    if (module.getSubCommands() == null || module.getSubCommands().isEmpty()) {
      bundledSubCommands.put(module, new HashSet<>());
      return;
    }
    List<String> subCommands = module.getSubCommands();
    for (int i = 0; i < subCommands.size(); i++) {
      subCommands.set(i, subCommands.get(i).toLowerCase());
    }
    // ensure that sub commands are unique and valid
    for (String subCommand : subCommands) {
      if (forbiddenSubCommands.contains(subCommand))
        throw new IllegalArgumentException("Forbidden sub command: " + subCommand);
      if (this.subCommands.containsKey(subCommand))
        continue;
      this.subCommands.put(subCommand, module);
      addSubCommandToBundle(module, subCommand);
    }
  }

  private void addSubCommandToBundle(GameBoxGame module, String subCommand) {
    bundledSubCommands.putIfAbsent(module, new HashSet<>());
    bundledSubCommands.get(module).add(subCommand);
  }

  public GameBoxGame getModuleBySubCommand(String subCommand) {
    GameBox.debug("grab module of " + subCommand);
    return subCommands.get(subCommand);
  }

  public void unregisterGame(String gameID) {
    GameBoxGame module = modules.get(gameID);
    if (module == null) return;
    Set<String> subCommands = bundledSubCommands.get(module);
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

  public boolean isDisabledModule(String moduleID) {
    return disabledModules.contains(moduleID);
  }

  public Set<String> getSubcommands() {
    return Collections.unmodifiableSet(subCommands.keySet());
  }
}
