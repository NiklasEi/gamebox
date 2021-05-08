package me.nikl.gamebox;

import me.nikl.gamebox.commands.GameBoxCommands;
import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.data.bungee.BukkitBridge;
import me.nikl.gamebox.data.database.DataBase;
import me.nikl.gamebox.data.database.FileDB;
import me.nikl.gamebox.data.database.MysqlDB;
import me.nikl.gamebox.external.CalendarEventsHook;
import me.nikl.gamebox.external.PlaceholderAPIHook;
import me.nikl.gamebox.input.InvitationHandler;
import me.nikl.gamebox.input.InviteInputHandler;
import me.nikl.gamebox.inventory.GuiManager;
import me.nikl.gamebox.inventory.InventoryTitleMessenger;
import me.nikl.gamebox.listeners.EnterGameBoxListener;
import me.nikl.gamebox.listeners.LeftGameBoxListener;
import me.nikl.gamebox.module.ModulesManager;
import me.nikl.gamebox.module.cloud.CloudService;
import me.nikl.gamebox.module.data.CloudModuleData;
import me.nikl.gamebox.module.local.VersionedModule;
import me.nikl.gamebox.utility.ConfigManager;
import me.nikl.gamebox.utility.FileUtility;
import me.nikl.nmsutilities.NmsFactory;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Niklas Eicker
 *
 * Main class of the plugin GameBox
 */
public class GameBox extends JavaPlugin {
  public static final String MODULE_GAMEBOX = "gamebox";
  public static boolean debug = false;
  // toggle to stop inventory contents from being restored when a new gui is opened
  public static boolean openingNewGUI = false;
  public static Economy econ = null;
  public GameBoxLanguage lang;
  private FileConfiguration config;
  private GameBoxAPI api;
  private PluginManager pManager;
  private DataBase dataBase;
  private Metrics metrics;
  private GameRegistry gameRegistry;
  private InventoryTitleMessenger inventoryTitleMessenger;
  private LeftGameBoxListener leftGameBoxListener;
  private EnterGameBoxListener enterGameBoxListener;
  private GameBoxCommands commands;
  private CalendarEventsHook calendarEventsHook;
  private BukkitBridge bukkitBridge;
  private ModulesManager modulesManager;

  public static void debug(String message) {
    if (debug) Bukkit.getConsoleSender().sendMessage(message);
  }

  @Override
  public void onEnable() {
    GameBoxSettings.defineGameBoxData(this);
    if ((NmsFactory.getNmsUtility()) == null) {
      sendVersionError();
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }

    this.gameRegistry = new GameRegistry(this);

    reload(null);
    establishHooksAndMetric();
  }

  private void setupMetrics() {
    metrics = new Metrics(this);

    // Bar chart of all installed games
    metrics.addCustomChart(new Metrics.SimpleBarChart("gamebox_games", () -> {
      HashMap<String, Integer> valueMap = new HashMap<>();
      for (String gameID : getPluginManager().getGames().keySet()) {
        valueMap.put(getOriginalGameName(gameID), 1);
      }
      return valueMap;
    }));

    // Drill down pie with modules and further breakdown of their installed version
    metrics.addCustomChart(new Metrics.DrilldownPie("installed_modules_pie", () -> {
      Map<String, Map<String, Integer>> map = new HashMap<>();
      for (VersionedModule module : getModulesManager().getLoadedVersionedModules()) {
        Map<String, Integer> entry = new HashMap<>();
        entry.put(module.getVersionData().getVersion().toString(), 1);
        map.put(module.getId(), entry);
      }
      return map;
    }));

    // Drill down pie with number of games and further breakdown of proportions of the games
    metrics.addCustomChart(new Metrics.DrilldownPie("games_drill_down", () -> {
      Map<String, Map<String, Integer>> map = new HashMap<>();

      Map<String, Integer> entry = new HashMap<>();

      for (String gameID : getPluginManager().getGames().keySet()) {
        entry.put(getOriginalGameName(gameID), 1);
      }

      map.put(String.valueOf(getPluginManager().getGames().size()), entry);
      return map;
    }));

    // Pie chart with number of games
    metrics.addCustomChart(new Metrics.SimplePie("number_of_gamebox_games"
            , () -> String.valueOf(PluginManager.gamesRegistered)));

    // Pie chart info about token (disabled/enabled)
    metrics.addCustomChart(new Metrics.SimplePie("token_enabled"
            , () -> GameBoxSettings.tokensEnabled ? "Enabled" : "Disabled"));

    // Pie chart with closed/open info of the token shop
    if (GameBoxSettings.tokensEnabled)
      metrics.addCustomChart(new Metrics.SimplePie("gamebox_shop_enabled"
              , () -> getPluginManager().getGuiManager().getShopManager().isClosed() ?
              "Closed" : "Open"));

    // Pie chart with data storage types
    metrics.addCustomChart(new Metrics.SimplePie("data_storage_type", () -> {
      if (GameBoxSettings.useMysql) {
        return "MySQL";
      }
      return "File (yml)";
    }));

    // Pie chart for hub mode
    metrics.addCustomChart(new Metrics.SimplePie("hub_mode_enabled"
            , () -> GameBoxSettings.hubModeEnabled ? "Enabled" : "Disabled"));
  }

  private void runLateChecks() {
    ConfigManager.printIssuesWhileLoadingLanguageFiles(this);
    if (GameBoxSettings.runLanguageChecksAutomatically)
      ConfigManager.printIncompleteLangFilesInfo(this);
    if (PluginManager.gamesRegistered == 0) {
      info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - + - +");
      info(ChatColor.RED + " There are no registered games!");
      info(ChatColor.RED + " Run '/gba module list' to see available modules ;)");
      info(ChatColor.RED + " Checkout 'https://modules.gamebox.nikl.me' for more info");
      info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - + - +");
    } else {
      info(ChatColor.GREEN + " " + PluginManager.gamesRegistered + " games were registered. Have fun :)");
    }
  }

  /**
   * Reload method called onEnable and on the reload command
   */
  public void reload(CommandSender sender) {
    if (!prepareForReload()) return;
    if (pManager != null) {
      pManager.shutDown();
      HandlerList.unregisterAll(pManager);
      pManager = null;
    }

    GameBox instance = this;
    BukkitRunnable rampUpGameBox = new BukkitRunnable() {
      @Override
      public void run() {
        if (GameBoxSettings.bungeeMode) {
          dataBase.registerBukkitBridge(bukkitBridge == null ? (bukkitBridge = new BukkitBridge(instance)) : bukkitBridge);
        }
        if (GameBoxSettings.econEnabled) {
          if (!setupEconomy()) {
            getLogger().log(Level.SEVERE, "No economy found!");
            getLogger().log(Level.SEVERE, "Even though it is enabled in the configuration file...");
            GameBoxSettings.econEnabled = false;
            Bukkit.getPluginManager().disablePlugin(instance);
            if (sender != null && lang != null) {
              sender.sendMessage(lang.PREFIX + lang.RELOAD_FAIL);
            }
            return;
          }
        }
        reloadListeners();
        GBPlayer.clearTokenListeners();
        inventoryTitleMessenger = new InventoryTitleMessenger(instance);
        // get a new plugin manager and set the other managers and handlers
        pManager = new PluginManager(instance);
        pManager.setGuiManager(new GuiManager(instance));
        pManager.setInviteInputHandler(new InviteInputHandler(instance));
        pManager.setInvitationHandler(new InvitationHandler(instance));

        commands = new GameBoxCommands(instance);
        // load players that are already online (otherwise done on join)
        pManager.loadPlayers();
        gameRegistry.reload();
        if (modulesManager != null) {
          modulesManager.shutDown();
          HandlerList.unregisterAll(modulesManager);
        }
        modulesManager = new ModulesManager(instance);
        if (sender != null && lang != null) {
          sender.sendMessage(lang.PREFIX + lang.RELOAD_SUCCESS);
        }
        debug(" running late checks in GameBox");
        runLateChecks();
      }
    };

    new BukkitRunnable() {
      @Override
      public void run() {
        if (loadDatabase()) {
          rampUpGameBox.runTask(instance);
        } else {
          new BukkitRunnable() {
            @Override
            public void run() {
              instance.getLogger().severe("Failed to load the database");
              if (sender != null && lang != null) {
                sender.sendMessage(lang.PREFIX + lang.RELOAD_FAIL);
              }
              Bukkit.getPluginManager().disablePlugin(instance);
            }
          }.runTask(instance);
        }
      }
    }.runTaskAsynchronously(this);
  }

  private boolean loadDatabase() {
    if (GameBoxSettings.useMysql) {
      return setUpMySQL();
    } else {
      return setUpFileDB();
    }
  }

  private boolean setUpFileDB() {
    if (dataBase != null) {
      dataBase.onShutDown();
      dataBase = null;
    }
    this.dataBase = new FileDB(this);
    info(" Loading database file...");
    info(" If this takes too long, you should switch to MySQL!");
    boolean success = dataBase.load();
    if (!success) return false;
    info(" ...done loading the database.");
    return true;
  }

  private boolean setUpMySQL() {
    if (dataBase != null) {
      dataBase.onShutDown();
      dataBase = null;
    }
    this.dataBase = new MysqlDB(this);
    return dataBase.load();
  }

  private boolean prepareForReload() {
    ConfigManager.clear();
    if (!reloadConfiguration()) {
      getLogger().severe(" Failed to load config file!");
      return false;
    }
    FileUtility.copyDefaultLanguageFiles();
    this.lang = new GameBoxLanguage(this);
    ConfigManager.registerGameLanguage(MODULE_GAMEBOX, lang);
    this.api = new GameBoxAPI(this);
    GameBoxSettings.loadSettings(this);
    return true;
  }

  private void reloadListeners() {
    if (leftGameBoxListener != null) {
      HandlerList.unregisterAll(leftGameBoxListener);
      leftGameBoxListener = null;
    }
    leftGameBoxListener = new LeftGameBoxListener(this);

    if (enterGameBoxListener != null) {
      HandlerList.unregisterAll(enterGameBoxListener);
      enterGameBoxListener = null;
    }
    enterGameBoxListener = new EnterGameBoxListener(this);
  }

  public DataBase getDataBase() {
    return this.dataBase;
  }

  private boolean setupEconomy() {
    if (getServer().getPluginManager().getPlugin("Vault") == null) {
      return false;
    }
    RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
    if (rsp == null) {
      return false;
    }
    econ = rsp.getProvider();
    return true;
  }

  @Override
  public void onDisable() {
    if (pManager != null) pManager.shutDown();
    if (dataBase != null) dataBase.onShutDown();
  }

  @Override
  public FileConfiguration getConfig() {
    return config;
  }

  public PluginManager getPluginManager() {
    return pManager;
  }

  public boolean reloadConfiguration() {
    File con = new File(this.getDataFolder().toString() + File.separatorChar + "config.yml");
    if (!con.exists()) {
      this.saveResource("config.yml", false);
    }
    try {
      this.config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(con), StandardCharsets.UTF_8));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return false;
    }
    ConfigManager.registerGameConfiguration(MODULE_GAMEBOX, config);
    return true;
  }

  private void establishHooksAndMetric() {
    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
      new PlaceholderAPIHook(this);
      Bukkit.getConsoleSender().sendMessage(lang.PREFIX + " Hooked into PlaceholderAPI");
    }
    hookCalendarEvents();
    // send data with bStats if not opt out
    if (GameBoxSettings.bStatsMetrics) {
      setupMetrics();
    } else {
      Bukkit.getConsoleSender().sendMessage(lang.PREFIX + " You have opt out bStats... That's sad!");
    }
  }

  private void hookCalendarEvents() {
    if (Bukkit.getPluginManager().isPluginEnabled("CalendarEvents")) {
      try {
        String[] version = Bukkit.getPluginManager().getPlugin("CalendarEvents").getDescription().getVersion().split("\\.");
        int minorVersion = Integer.parseInt(version[1]);
        int majorVersion = Integer.parseInt(version[0]);
        if (minorVersion < 4 && majorVersion == 1) {
          getLogger().warning(" CalendarEvents has to be version 1.4.0 or above!");
          return;
        }
      } catch (NumberFormatException e) {
        getLogger().warning(" CalendarEvents has to be version 1.4.0 or above!");
        return;
      }
      calendarEventsHook = new CalendarEventsHook(this);
      Bukkit.getConsoleSender().sendMessage(lang.PREFIX + " Hooked into CalendarEvents");
    }
  }

  private void sendVersionError() {
    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - + - +");
    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " Your server version is not compatible with this plugin!");
    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "   Please make sure that you have the newest version:");
    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "   https://www.spigotmc.org/resources/37273/");
    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - + - +");
  }

  public GameBoxAPI getApi() {
    return this.api;
  }

  /**
   * Get the original game name from the module
   * <p>
   * This is to make the statistics on bStats cleaner
   * and to have unified names of the games (since they can be customised).
   * <p>
   * Get the default name from the default
   * language file of the game.
   *
   * @param gameID Id of the game
   * @return the original name'
   */
  private String getOriginalGameName(String gameID) {
    return ConfigManager.getGameLanguage(gameID).DEFAULT_PLAIN_NAME;
  }

  public void info(String message) {
    Bukkit.getConsoleSender().sendMessage(lang.PREFIX + message);
  }

  public GameRegistry getGameRegistry() {
    return gameRegistry;
  }

  public void warning(String message) {
    getLogger().warning(message);
  }

  public InventoryTitleMessenger getInventoryTitleMessenger() {
    return inventoryTitleMessenger;
  }

  public CalendarEventsHook getCalendarEventsHook() {
    return calendarEventsHook;
  }

  public GameBoxCommands getCommands() {
    return commands;
  }

  public ModulesManager getModulesManager() {
    return modulesManager;
  }

  public void hookAfterConnectingToCloud() {
    getPluginManager().getGuiManager().getModulesGuiManager().loadGui();
    getModulesManager().collectUpdatesForInstalledModules();
    getModulesManager().updateModulesAndPrintInfo();

    CloudService cloudService = getModulesManager().getCloudService();
    java.util.List<CloudModuleData> cloudModuleData = cloudService.getCachedCloudContent();
    HashMap<String, String> context = new HashMap<>();
    context.put("amount", String.valueOf(cloudModuleData.size()));
    Bukkit.getConsoleSender().sendMessage(lang.PREFIX + lang.replaceContext(lang.CMD_MODULES_LIST_HEADER, context));
  }
}
