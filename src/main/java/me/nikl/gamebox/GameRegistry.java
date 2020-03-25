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
  private Map<String, GameBoxGame> games = new HashMap<>();
  private Map<String, GameBoxGame> declinedGames = new HashMap<>();
  private Map<String, GameBoxGame> subCommands = new HashMap<>();
  private Map<String, Set<String>> bundledSubCommands = new HashMap<>();
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

  public boolean registerModule(GameBoxGame game) {
    if (isRegistered(game.getGameId())) {
      gameBox.getLogger().log(Level.WARNING, "A Module tried registering with an already in use ID!");
      return false;
    }
    if (forbiddenIDs.contains(game.getGameId())) {
      gameBox.getLogger().log(Level.WARNING, "A Module tried registering with a forbidden ID (" + game.getGameId() + ")");
      return false;
    }
    if (disabledModules.contains(game.getGameId())) {
      declinedGames.put(game.getGameId(), game);
      gameBox.warning("The game " + game.getGameId() + " is disabled in 'games.yml'");
      return false;
    }
    if (!game.getGameId().equals(GameBox.MODULE_GAMEBOX))
      handleModuleSettings(game);
      games.put(game.getGameId(), game);
    if (game.getJarFile() != null) {
      if (!FileUtility.copyExternalResources(gameBox, game)) {
        gameBox.info(" Failed to register the external module '" + game.getGameId() + "'");
        games.remove(game.getGameId());
        return false;
      }
    }
    loadGame(game);
    registerSubCommands(game);
    return true;
  }

  private void handleModuleSettings(GameBoxGame game) {
    String moduleID = game.getGameId();
    if (!gamesConfiguration.isSet("games." + moduleID)) {
      setDefaultModuleSettings(game);
    } else {
      // overwrite default sub commands
      if (gamesConfiguration.isList("games." + moduleID + ".subCommands")) {
        List<String> subCommands = gamesConfiguration.getStringList("games." + moduleID + ".subCommands");
        if (!subCommands.isEmpty()) game.setSubCommands(subCommands);
      }
    }
  }

  private void setDefaultModuleSettings(GameBoxGame game) {
    String gameId = game.getGameId();
    gamesConfiguration.set("games." + gameId + ".enabled", enableNewGamesByDefault);
    gamesConfiguration.set("games." + gameId + ".subCommands", game.getSubCommands());
    saveGameSettings();
  }

  public boolean isRegistered(GameBoxGame game) {
    return isRegistered(game.getGameId());
  }

  public boolean isRegistered(String gameId) {
    return games.containsKey(gameId.toLowerCase());
  }

  public GameBoxGame getModule(String gameId) {
    return games.get(gameId);
  }

  /**
   * Reload the settings. Then go through all modules and
   * try getting game instances through their class paths
   */
  public void reload() {
    reloadGamesConfiguration();
    loadDisabledModules();
    games.putAll(declinedGames);
    declinedGames.clear();
    subCommands.clear();
    bundledSubCommands.clear();
    Iterator<GameBoxGame> iterator = games.values().iterator();
    while (iterator.hasNext()) {
      GameBoxGame module = iterator.next();
      if (disabledModules.contains(module.getGameId())) {
        iterator.remove();
        declinedGames.put(module.getGameId(), module);
        gameBox.warning("The game " + module.getGameId() + " is disabled in 'games.yml'");
        continue;
      }
      reloadGameData(module);
      loadGame(module);
    }
  }

  private void reloadGameData(GameBoxGame game) {
    String gameId = game.getGameId();
    if (gamesConfiguration.isList("games." + gameId + ".subCommands")) {
      List<String> subCommands = gamesConfiguration.getStringList("games." + gameId + ".subCommands");
      if (!subCommands.isEmpty()) game.setSubCommands(subCommands);
    }
    registerSubCommands(game);
  }

  private void loadGame(GameBoxGame game) {
    Class<Game> clazz = null;
    try {
      clazz = game.getGameClass();
    } catch ( ClassCastException e) {
      e.printStackTrace();
    }
    if (clazz == null) return;
    try {
      Constructor<Game> ctor = clazz.getConstructor(GameBox.class);
      Game gameInstance = ctor.newInstance(gameBox);
      gameBox.getPluginManager().registerGame(gameInstance);
      gameInstance.onEnable();
    } catch (NoSuchMethodException | IllegalAccessException
            | InstantiationException | InvocationTargetException e) {
      gameBox.warning(" The game class of '" + game.getGameId() + "' needs a public constructor taking only a GameBox object!");
      e.printStackTrace();
      gameBox.getPluginManager().unregisterGame(game.getGameId());
    } catch (GameLoadException e) {
      gameBox.warning(" Exception while loading '" + game.getGameId() + "'!");
      e.printStackTrace();
      gameBox.getPluginManager().unregisterGame(game.getGameId());
    } catch (Throwable throwable) {
      gameBox.warning("unexpected error while loading '" + game.getGameId() + "'...");
      throwable.printStackTrace();
      gameBox.getPluginManager().unregisterGame(game.getGameId());
    }
  }

  public Set<String> getModuleIDs() {
    return Collections.unmodifiableSet(games.keySet());
  }

  public Set<GameBoxGame> getGames() {
    return Collections.unmodifiableSet(new HashSet<>(games.values()));
  }

  public Set<String> getModuleSubCommands(GameBoxGame game) {
    return Collections.unmodifiableSet(bundledSubCommands.get(game.getGameId()));
  }

  private void registerSubCommands(GameBoxGame module) {
    if (module.getSubCommands() == null || module.getSubCommands().isEmpty()) {
      bundledSubCommands.put(module.getGameId(), new HashSet<>());
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
    bundledSubCommands.putIfAbsent(module.getGameId(), new HashSet<>());
    bundledSubCommands.get(module.getGameId()).add(subCommand);
  }

  public GameBoxGame getModuleBySubCommand(String subCommand) {
    GameBox.debug("grab module of " + subCommand);
    return subCommands.get(subCommand);
  }

  public void unregisterGame(String gameID) {
    GameBoxGame game = games.get(gameID);
    if (game == null) return;
    Set<String> subCommands = bundledSubCommands.get(game.getGameId());
    games.remove(gameID);
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
