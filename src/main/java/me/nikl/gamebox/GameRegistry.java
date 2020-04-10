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
  private final Set<String> disabledGames = new HashSet<>();
  private GameBox gameBox;
  private Map<String, GameBoxGame> games = new HashMap<>();
  private Map<String, GameBoxGame> subCommands = new HashMap<>();
  private Map<String, Set<String>> bundledSubCommands = new HashMap<>();
  private boolean enableNewGamesByDefault;
  private FileConfiguration gamesConfiguration;
  private File gamesFile;

  public GameRegistry(GameBox plugin) {
    this.gameBox = plugin;
  }

  private void loadDisabledGames() {
    disabledGames.clear();
    ConfigurationSection gamesSection = gamesConfiguration.getConfigurationSection("games");
    if (gamesSection == null) return;
    for (String gameId : gamesSection.getKeys(false)) {
      if (!gamesSection.getBoolean(gameId + ".enabled", true)) {
        GameBox.debug("Set " + gameId + " as disabled");
        disabledGames.add(gameId);
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
      enableNewGamesByDefault = gamesConfiguration.getBoolean("enableNewGamesByDefault", true);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public boolean registerGame(GameBoxGame game) {
    if (isRegistered(game.getGameId())) {
      gameBox.getLogger().log(Level.WARNING, "A Module tried registering with an already in use ID!");
      return false;
    }
    if (forbiddenIDs.contains(game.getGameId())) {
      gameBox.getLogger().log(Level.WARNING, "A Module tried registering with a forbidden ID (" + game.getGameId() + ")");
      return false;
    }
    if (disabledGames.contains(game.getGameId())) {
      gameBox.warning("The game " + game.getGameId() + " is disabled in 'games.yml'");
      return false;
    }
    if (!game.getGameId().equals(GameBox.MODULE_GAMEBOX))
      handleGameSettings(game);
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

  private void handleGameSettings(GameBoxGame game) {
    String gameId = game.getGameId();
    if (!gamesConfiguration.isSet("games." + gameId)) {
      setDefaultGameSettings(game);
    } else {
      // overwrite default sub commands
      if (gamesConfiguration.isList("games." + gameId + ".subCommands")) {
        List<String> subCommands = gamesConfiguration.getStringList("games." + gameId + ".subCommands");
        if (!subCommands.isEmpty()) game.setSubCommands(subCommands);
      }
    }
  }

  private void setDefaultGameSettings(GameBoxGame game) {
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

  public GameBoxGame getGame(String gameId) {
    return games.get(gameId);
  }

  /**
   * Reload game registry
   */
  public void reload() {
    reloadGamesConfiguration();
    games.clear();
    disabledGames.clear();
    subCommands.clear();
    bundledSubCommands.clear();
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

  public Set<String> getGameIds() {
    return Collections.unmodifiableSet(games.keySet());
  }

  public Set<GameBoxGame> getGames() {
    return Collections.unmodifiableSet(new HashSet<>(games.values()));
  }

  public Set<String> getGameSubCommands(GameBoxGame game) {
    return Collections.unmodifiableSet(bundledSubCommands.get(game.getGameId()));
  }

  private void registerSubCommands(GameBoxGame game) {
    if (game.getSubCommands() == null || game.getSubCommands().isEmpty()) {
      bundledSubCommands.put(game.getGameId(), new HashSet<>());
      return;
    }
    List<String> subCommands = game.getSubCommands();
    for (int i = 0; i < subCommands.size(); i++) {
      subCommands.set(i, subCommands.get(i).toLowerCase());
    }
    // ensure that sub commands are unique and valid
    for (String subCommand : subCommands) {
      if (forbiddenSubCommands.contains(subCommand))
        throw new IllegalArgumentException("Forbidden sub command: " + subCommand);
      if (this.subCommands.containsKey(subCommand))
        continue;
      this.subCommands.put(subCommand, game);
      addSubCommandToBundle(game, subCommand);
    }
  }

  private void addSubCommandToBundle(GameBoxGame game, String subCommand) {
    bundledSubCommands.putIfAbsent(game.getGameId(), new HashSet<>());
    bundledSubCommands.get(game.getGameId()).add(subCommand);
  }

  public GameBoxGame getGameBySubCommand(String subCommand) {
    GameBox.debug("grab game for sub command " + subCommand);
    return subCommands.get(subCommand);
  }

  public void unregisterGame(String gameId) {
    GameBoxGame game = games.get(gameId);
    if (game == null) return;
    Set<String> subCommands = bundledSubCommands.get(game.getGameId());
    games.remove(gameId);
    if (subCommands == null || subCommands.isEmpty()) return;
    for (String subCommand : subCommands) {
      GameBox.debug("   remove " + subCommand);
      this.subCommands.remove(subCommand);
    }
    bundledSubCommands.remove(gameId);
  }

  public void disableGame(String gameID) {
    disabledGames.add(gameID);
    gamesConfiguration.set("games." + gameID + ".enabled", false);
    saveGameSettings();
  }

  public void enableGame(String gameID) {
    disabledGames.remove(gameID);
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

  public boolean isDisabledGame(String gameId) {
    return disabledGames.contains(gameId);
  }

  public Set<String> getSubCommands() {
    return Collections.unmodifiableSet(subCommands.keySet());
  }
}
