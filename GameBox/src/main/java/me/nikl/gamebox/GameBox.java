package me.nikl.gamebox;

import me.nikl.gamebox.commands.AdminCommand;
import me.nikl.gamebox.commands.MainCommand;
import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.data.database.DataBase;
import me.nikl.gamebox.data.database.FileDB;
import me.nikl.gamebox.data.database.MysqlDB;
import me.nikl.gamebox.game.Game;
import me.nikl.gamebox.game.GameLanguage;
import me.nikl.gamebox.input.HandleInvitations;
import me.nikl.gamebox.input.HandleInviteInput;
import me.nikl.gamebox.inventory.GUIManager;
import me.nikl.gamebox.inventory.InventoryTitleMessenger;
import me.nikl.gamebox.listeners.EnterGameBoxListener;
import me.nikl.gamebox.listeners.LeftGameBoxListener;
import me.nikl.gamebox.nms.NmsFactory;
import me.nikl.gamebox.utility.FileUtility;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created by niklas on 10/27/16.
 *
 * Main class of the plugin GameBox
 */
public class GameBox extends JavaPlugin {
    public static final String MODULE_GAMEBOX = "gamebox";
    public static final String MODULE_CONNECTFOUR = "connectfour";
    public static final String MODULE_COOKIECLICKER = "cookieclicker";
    public static final String MODULE_MATCHIT = "matchit";
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

    private MainCommand mainCommand;
    private AdminCommand adminCommand;
    private LeftGameBoxListener leftGameBoxListener;
    private EnterGameBoxListener enterGameBoxListener;

    @Override
    public void onEnable() {
        if ((NmsFactory.getNmsUtility()) == null) {
            sendVersionError();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.gameRegistry = new GameRegistry(this);
        new Module(this, MODULE_GAMEBOX, null, null);

        if (!reload()) {
            getLogger().severe(" Problem while loading the plugin! Plugin was disabled!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        // At this point all managers are set up and games can be registered
        registerGames();
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
        if (GameBoxSettings.checkInventoryLength) {
            info(ChatColor.RED + " Your server version can't handle more then 32 characters in inventory titles!");
            info(ChatColor.RED + " GameBox will shorten too long titles (marked by '...') to prevent errors.");
            info(ChatColor.RED + " To fix this ('...'), create your own language file with shorter titles.");
        }

        if (GameBoxSettings.runLanguageChecksAutomatically) adminCommand.printIncompleteLangFilesInfo();

        if (PluginManager.gamesRegistered == 0) {
            info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - + - +");
            info(ChatColor.RED + " There are no registered games!");
            info(ChatColor.RED + " You should visit Spigot and get a few ;)");
            info(ChatColor.RED + "   https://www.spigotmc.org/resources/37273/");
            info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - + - +");
        } else {
            info(ChatColor.GREEN + " " + PluginManager.gamesRegistered + " games were registered. Have fun :)");
        }
    }

    private void registerGames() {
        // Default games:
        new Module(this, MODULE_CONNECTFOUR
                , "me.nikl.gamebox.games.connectfour.ConnectFour", null
                , GameBox.MODULE_CONNECTFOUR, "connect4", "c4");
        new Module(this, MODULE_COOKIECLICKER
                , "me.nikl.gamebox.games.cookieclicker.CookieClicker", null
                , GameBox.MODULE_COOKIECLICKER, "cookies", "cc");
        new Module(this, MODULE_MATCHIT
                , "me.nikl.gamebox.games.matchit.MatchIt", null
                , GameBox.MODULE_MATCHIT, "mi");
    }

    /**
     * Reload method called onEnable and on the reload command
     */
    public boolean reload() {
        if (!prepareForReload()) return false;
        if (pManager != null) {
            pManager.shutDown();
            HandlerList.unregisterAll(pManager);
            pManager = null;
        }
        if (GameBoxSettings.useMysql) {
            setUpMySQL();
        }
        // if connecting to the database failed, the setting will be set to false
        // and the plugin falls back to file storage
        if (!GameBoxSettings.useMysql) {
            if (!setUpFileDB()) return false;
        }

        if (GameBoxSettings.econEnabled) {
            if (!setupEconomy()) {
                getLogger().log(Level.SEVERE, "No economy found!");
                getLogger().log(Level.SEVERE, "Even though it is enabled in the configuration file...");
                GameBoxSettings.econEnabled = false;
                return false;
            }
        }
        reloadListeners();
        GBPlayer.clearTokenListeners();
        this.inventoryTitleMessenger = new InventoryTitleMessenger(this);
        // get a new plugin manager and set the other managers and handlers
        pManager = new PluginManager(this);
        pManager.setGuiManager(new GUIManager(this));
        pManager.setHandleInviteInput(new HandleInviteInput(this));
        pManager.setHandleInvitations(new HandleInvitations(this));

        mainCommand = new MainCommand(this);
        adminCommand = new AdminCommand(this);
        this.getCommand("gamebox").setExecutor(mainCommand);
        this.getCommand("gameboxadmin").setExecutor(adminCommand);
        // load players that are already online (otherwise done on join)
        pManager.loadPlayers();
        gameRegistry.loadGames();
        new BukkitRunnable() {
            @Override
            public void run() {
                debug(" running late checks in GameBox");
                runLateChecks();
            }
        }.runTask(this);
        return true;
    }

    private boolean setUpFileDB() {
        if (dataBase != null) {
            dataBase.onShutDown();
            dataBase = null;
        }

        this.dataBase = new FileDB(this);
        if (!dataBase.load(false)) {
            getLogger().log(Level.SEVERE, " Something went wrong with the data file");
            return false;
        }
        return true;
    }

    private void setUpMySQL() {
        if (dataBase != null) {
            dataBase.onShutDown();
            dataBase = null;
        }

        this.dataBase = new MysqlDB(this);
        if (!dataBase.load(false)) {
            getLogger().log(Level.SEVERE, " Falling back to file storage...");
            GameBoxSettings.useMysql = false;
            dataBase = null;
        }
    }

    private boolean prepareForReload() {
        if (!reloadConfiguration()) {
            getLogger().severe(" Failed to load config file!");
            return false;
        }
        FileUtility.copyDefaultLanguageFiles();
        this.lang = new GameBoxLanguage(this);
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
        return econ != null;
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

    @Deprecated // to config manager
    public boolean reloadConfiguration() {
        File con = new File(this.getDataFolder().toString() + File.separatorChar + "config.yml");
        if (!con.exists()) {
            this.saveResource("config.yml", false);
        }
        try {
            this.config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(con), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Deprecated // use api implementation
    public boolean wonTokens(UUID player, int tokens, String gameID) {
        if (!GameBoxSettings.tokensEnabled) return false;
        return this.pManager.wonTokens(player, tokens, gameID);
    }

    private void establishHooksAndMetric() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPIHook(this);
            Bukkit.getConsoleSender().sendMessage(lang.PREFIX + " Hooked into PlaceholderAPI");
        }
        // send data with bStats if not opt out
        if (GameBoxSettings.bStatsMetrics) {
            setupMetrics();
        } else {
            Bukkit.getConsoleSender().sendMessage(lang.PREFIX + " You have opt out bStats... That's sad!");
        }
    }

    private void sendVersionError() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - + - +");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " Your server version is not compatible with this plugin!");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "   Make sure you have the newest version:");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "   https://www.spigotmc.org/resources/37273/");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - + - +");
    }

    public GameBoxAPI getApi() {
        return this.api;
    }

    /**
     * Get the original game name from the module
     *
     * This is to make the statistics on bStats cleaner
     * and to have unified names of the games (since they can be customised).
     *
     * Get the default name from the default
     * language file of the game.
     *
     * @param gameID Id of the game
     * @return the original name, or 'Other (custom game)'
     */
    private String getOriginalGameName(String gameID) {
        GameLanguage gameLang = getPluginManager().getGame(gameID).getGameLang();
        if (gameLang != null) return gameLang.DEFAULT_PLAIN_NAME;

        // is also set as default name, if not set in language file
        return "Other (custom game)";
    }

    @Deprecated // to config manager
    public FileConfiguration getConfig(Module module) {
        return getConfig(module.getModuleID());
    }

    @Deprecated // to config manager
    public FileConfiguration getConfig(String moduleId) {
        if (moduleId.equals(MODULE_GAMEBOX))
            return getConfig();
        Game game = getPluginManager().getGame(moduleId);
        if (game == null) return null;
        return game.getConfig();
    }

    @Deprecated // to language manager
    public Language getLanguage(Module module) {
        return getLanguage(module.getModuleID());
    }

    @Deprecated // to language manager
    public Language getLanguage(String moduleID) {
        if (moduleID.equals(MODULE_GAMEBOX))
            return lang;
        Game game = getPluginManager().getGame(moduleID);
        if (game == null) return null;
        return game.getGameLang();
    }

    public static void debug(String message) {
        if (debug) Bukkit.getConsoleSender().sendMessage(message);
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
}