package me.nikl.gamebox.data.database;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.data.toplist.PlayerScore;
import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.data.toplist.TopList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author Niklas Eicker
 */
public class FileDB extends DataBase {
  private File dataFile;
  private FileConfiguration data;

  public FileDB(GameBox plugin) {
    super(plugin);
    this.dataFile = new File(plugin.getDataFolder().toString() + File.separatorChar + "data.yml");
  }

  @Override
  public boolean load() {
    if (!dataFile.exists()) {
      try {
        dataFile.getParentFile().mkdir();
        dataFile.createNewFile();
      } catch (IOException e) {
        plugin.getLogger().severe("Failed to create Datafile!");
        e.printStackTrace();
        return false;
      }
    }
    // load stats file
    try {
      this.data = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(this.dataFile), StandardCharsets.UTF_8));
    } catch (FileNotFoundException e) {
      plugin.getLogger().severe("Failed to read Datafile!");
      e.printStackTrace();
      return false;
    }
    return true;
  }

  @Override
  public void save(boolean async) {
    if (async) {
      new BukkitRunnable() {
        @Override
        public void run() {
          GameBox.debug(" saving to file async...");
          try {
            data.save(dataFile);
          } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "failed to save statistics (async)");
            e.printStackTrace();
          }
          GameBox.debug(" ...done");
        }
      }.runTaskAsynchronously(plugin);
    } else {
      try {
        this.data.save(dataFile);
      } catch (IOException e) {
        Bukkit.getLogger().log(Level.SEVERE, "failed to save statistics");
        e.printStackTrace();
      }
    }
  }

  @Override
  public void addStatistics(UUID uuid, String gameID, String gameTypeID, double value, SaveType saveType, boolean async) {
    String topListIdentifier = buildTopListIdentifier(gameID, gameTypeID, saveType);
    GameBox.debug("Adding statistics '" + uuid.toString() + "." + topListIdentifier + "' with the value: " + value);
    double oldScore;
    switch (saveType) {
      case SCORE:
      case TIME_HIGH:
      case HIGH_NUMBER_SCORE:
        oldScore = data.getDouble(uuid.toString() + "." + GAMES_STATISTICS_NODE + "." + topListIdentifier, 0.);
        if (oldScore >= value) return;
        data.set(uuid.toString() + "." + GAMES_STATISTICS_NODE + "." + topListIdentifier, value);
        break;
      case TIME_LOW:
        oldScore = data.getDouble(uuid.toString() + "." + GAMES_STATISTICS_NODE + "." + topListIdentifier, Double.MAX_VALUE);
        if (oldScore <= value) return;
        data.set(uuid.toString() + "." + GAMES_STATISTICS_NODE + "." + topListIdentifier, value);
        break;
      case WINS:
        oldScore = data.getDouble(uuid.toString() + "." + GAMES_STATISTICS_NODE + "." + topListIdentifier, 0.);
        data.set(uuid.toString() + "." + GAMES_STATISTICS_NODE + "." + topListIdentifier, value + oldScore);
        // new wins count to pass on to the top list
        value = value + oldScore;
        break;
      default:
        Bukkit.getLogger().log(Level.WARNING, "trying to save unsupported statistics: " + saveType.toString());
        return;
    }
    // top value of player was corrected
    updateCachedTopList(gameID, gameTypeID, saveType, new PlayerScore(uuid, value, saveType));
  }

  private String buildTopListIdentifier(String gameID, String gameTypeID, SaveType saveType) {
    return gameID + "." + gameTypeID + "." + saveType.toString().toLowerCase();
  }

  @Override
  public TopList getTopList(String gameID, String gameTypeID, SaveType saveType) {
    String topListIdentifier = buildTopListIdentifier(gameID, gameTypeID, saveType);
    if (cachedTopLists.containsKey(topListIdentifier)) return cachedTopLists.get(topListIdentifier);
    TopList newTopList = new TopList(topListIdentifier, new ArrayList<>());
    cachedTopLists.put(topListIdentifier, newTopList);
    getAndSetTopListContents(newTopList, gameID, gameTypeID, saveType);
    return newTopList;
  }

  private void getAndSetTopListContents(TopList newTopList, String gameID, String gameTypeID, SaveType saveType) {
    getTopNPlayerScores(TopList.TOP_LIST_LENGTH, gameID, gameTypeID, saveType, new Callback<List<PlayerScore>>() {
      @Override
      public void onSuccess(List<PlayerScore> done) {
        newTopList.updatePlayerScores(done);
      }

      @Override
      public void onFailure(@Nullable Throwable throwable, @Nullable List<PlayerScore> value) {
        plugin.getLogger().warning("Error while grabbing top list entries!");
        if (throwable != null) throwable.printStackTrace();
      }
    });
  }

  private UUID getBestScore(Map<UUID, Double> valuesMap, boolean higher) {
    double currentBestScore = higher ? 0. : Double.MAX_VALUE;
    UUID currentBestUuid = null;
    for (Map.Entry<UUID, Double> entry : valuesMap.entrySet()) {
      if (higher) {
        if (entry.getValue() > currentBestScore) {
          currentBestScore = entry.getValue();
          currentBestUuid = entry.getKey();
        }
      } else {
        if (entry.getValue() < currentBestScore) {
          currentBestScore = entry.getValue();
          currentBestUuid = entry.getKey();
        }
      }
    }
    return currentBestUuid;
  }

  private Map<UUID, Double> createValuesMap(String topListIdentifier) {
    Map<UUID, Double> valuesMap = new HashMap<>();
    for (String uuid : data.getKeys(false)) {
      if (!data.isSet(uuid + "." + GAMES_STATISTICS_NODE + "." + topListIdentifier))
        continue;
      try {
        UUID uuid1 = UUID.fromString(uuid);
        valuesMap.put(uuid1, data.getDouble(uuid + "." + GAMES_STATISTICS_NODE + "." + topListIdentifier));
      } catch (IllegalArgumentException exception) {
        Bukkit.getLogger().log(Level.WARNING, "failed to load a player score due to a malformed UUID (" + topListIdentifier + ")");
      }
    }
    return valuesMap;
  }

  @Override
  public void loadPlayer(GBPlayer player, boolean async) {
    boolean playSounds = data.getBoolean(player.getUuid() + "." + DataBase.PLAYER_PLAY_SOUNDS, true);
    boolean allowInvitations = data.getBoolean(player.getUuid() + "." + DataBase.PLAYER_ALLOW_INVITATIONS, true);
    int token = data.getInt(player.getUuid() + "." + DataBase.PLAYER_TOKEN_PATH, 0);
    player.setPlayerData(token, playSounds, allowInvitations);
  }

  @Override
  public void savePlayer(GBPlayer player, boolean async) {
    // async ignored here for file saving... It is in cache anyways
    String uuid = player.getUuid().toString();
    data.set(uuid + "." + DataBase.PLAYER_PLAY_SOUNDS, player.isPlaySounds());
    data.set(uuid + "." + DataBase.PLAYER_ALLOW_INVITATIONS, player.allowsInvites());
    data.set(uuid + "." + DataBase.PLAYER_TOKEN_PATH, player.getTokens());
  }

  @Override
  public void getToken(UUID uuid, Callback<Integer> callback) {
    callback.onSuccess(data.getInt(uuid.toString() + "." + PLAYER_TOKEN_PATH, 0));
  }

  @Override
  public void setToken(UUID uuid, int token) {
    data.set(uuid + "." + DataBase.PLAYER_TOKEN_PATH, token);
  }

  @Override
  public void resetHighScores() {
    for (String uuid : data.getKeys(false)) {
      if (!data.isConfigurationSection(uuid + "." + DataBase.GAMES_STATISTICS_NODE)) continue;
      data.set(uuid + "." + DataBase.GAMES_STATISTICS_NODE, null);
    }
  }

  @Override
  public void resetHighScores(UUID uuid) {
    if (!data.isConfigurationSection(uuid + "." + DataBase.GAMES_STATISTICS_NODE)) return;
    data.set(uuid + "." + DataBase.GAMES_STATISTICS_NODE, null);
  }

  @Override
  public void resetHighScores(UUID uuid, String gameId, String gameTypeId, SaveType saveType) {
    String topListIdentifier = buildTopListIdentifier(gameId, gameTypeId, saveType);
    if (!data.isSet(uuid + "." + DataBase.GAMES_STATISTICS_NODE + "." + topListIdentifier)) {
      return;
    }
    data.set(uuid + "." + DataBase.GAMES_STATISTICS_NODE + "." + topListIdentifier, null);
  }

  @Override
  public void resetHighScores(String gameID, String gameTypeID, SaveType saveType) {
    for (String uuid : data.getKeys(false)) {
      if (!data.isConfigurationSection(uuid + "." + DataBase.GAMES_STATISTICS_NODE)) continue;
      String topListIdentifier = buildTopListIdentifier(gameID, gameTypeID, saveType);
      if (!data.isSet(uuid + "." + DataBase.GAMES_STATISTICS_NODE + "." + topListIdentifier))
        continue;
      data.set(uuid + "." + DataBase.GAMES_STATISTICS_NODE + "." + topListIdentifier, null);
    }
  }

  @Override
  public void getTopNPlayerScores(int n, String gameID, String gameTypeID, SaveType saveType, Callback<List<PlayerScore>> callback) {
    new BukkitRunnable() {
      @Override
      public void run() {
        GameBox.debug("Grabbing top " + n + " entries in " + gameID + "." + gameTypeID);
        long start = System.currentTimeMillis();
        String topListIdentifier = buildTopListIdentifier(gameID, gameTypeID, saveType);
        ArrayList<PlayerScore> toReturn = new ArrayList<>();
        Map<UUID, Double> valuesMap = createValuesMap(topListIdentifier);
        boolean higher = saveType.isHigherScore();
        UUID currentBestUuid;
        int number = 0;
        while (number < TopList.TOP_LIST_LENGTH && !valuesMap.keySet().isEmpty()) {
          currentBestUuid = getBestScore(valuesMap, higher);
          GameBox.debug("Found rank " + (number + 1) + " with time: " + valuesMap.get(currentBestUuid) + "      higher: " + higher);
          toReturn.add(new PlayerScore(currentBestUuid, valuesMap.get(currentBestUuid), saveType));
          number++;
          valuesMap.remove(currentBestUuid);
        }
        GameBox.debug("Found " + toReturn.size() + " in " + Math.round(System.currentTimeMillis() - start) / 1000. + " seconds");
        new BukkitRunnable() {
          @Override
          public void run() {
            callback.onSuccess(toReturn);
          }
        }.runTask(plugin);
      }
    }.runTaskAsynchronously(plugin);
  }

  @Override
  public void updateTopLists() {
    for (TopList topList : cachedTopLists.values()) {
      topList.clearTopList();
      String id = topList.getIdentifier();
      String[] parts = id.split("\\.");
      if (parts.length != 3) {
        plugin.warning("Error while updating top lists");
        plugin.warning("   skipping: " + id);
        continue;
      }
      getAndSetTopListContents(topList, parts[0], parts[1], SaveType.valueOf(parts[2].toUpperCase()));
    }
  }

  public void convertToMySQL() {
    MysqlDB toDb = (MysqlDB) plugin.getDataBase();
    plugin.getLogger().info("Starting file to MySQL conversion...");
    int playerCount = 0;
    UUID uuid;
    boolean playSounds;
    boolean allowInvitations;
    int token;
    for (String uuidString : data.getKeys(false)) {
      playSounds = data.getBoolean(uuidString + "." + DataBase.PLAYER_PLAY_SOUNDS, true);
      allowInvitations = data.getBoolean(uuidString + "." + DataBase.PLAYER_ALLOW_INVITATIONS, true);
      token = data.getInt(uuidString + "." + DataBase.PLAYER_TOKEN_PATH, 0);
      try {
        uuid = UUID.fromString(uuidString);
      } catch (IllegalArgumentException ignore) {
        plugin.getLogger().warning("failed to convert a UUID");
        continue;
      }
      playerCount++;
      toDb.savePlayer(new GBPlayer(plugin, uuid, token, playSounds, allowInvitations), true);
      if (!data.isConfigurationSection(uuidString + "." + GAMES_STATISTICS_NODE)) continue;
      ConfigurationSection statisticsSection = data.getConfigurationSection(uuidString + "." + GAMES_STATISTICS_NODE);
      for (String key : statisticsSection.getKeys(true)) {
        if (!statisticsSection.isDouble(key)) continue;
        String[] parts = key.split("\\.");
        if (parts.length != 3) continue;
        double value = statisticsSection.getDouble(key);
        SaveType saveType;
        try {
          saveType = SaveType.valueOf(parts[2].toUpperCase());
        } catch (IllegalArgumentException exception) {
          plugin.getLogger().warning("failed to recognise the save-type of a high score");
          continue;
        }
        toDb.addStatistics(uuid, parts[0], parts[1], value, saveType);
      }
    }
    plugin.getLogger().info("Player data of " + playerCount + " players has been added to your MySQL database.");
  }
}
