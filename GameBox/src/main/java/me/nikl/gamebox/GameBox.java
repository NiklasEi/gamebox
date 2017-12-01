package me.nikl.gamebox;

import me.nikl.gamebox.commands.AdminCommand;
import me.nikl.gamebox.commands.MainCommand;
import me.nikl.gamebox.data.DataBase;
import me.nikl.gamebox.data.FileDB;
import me.nikl.gamebox.data.MysqlDB;
import me.nikl.gamebox.games.Game;
import me.nikl.gamebox.games.GameLanguage;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.invitation.HandleInvitations;
import me.nikl.gamebox.invitation.HandleInviteInput;
import me.nikl.gamebox.listeners.EnterGameBoxListener;
import me.nikl.gamebox.listeners.LeftGameBoxListener;
import me.nikl.gamebox.nms.*;
import me.nikl.gamebox.util.FileUtil;
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

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;

/**
 * Created by niklas on 10/27/16.
 *
 * Main class of the plugin GameBox
 */
public class GameBox extends JavaPlugin{
	public static final String MODULE_GAMEBOX = "gamebox";

	public static final String MODULE_CONNECTFOUR = "connectfour";
	public static final String MODULE_COOKIECLICKER = "cookieclicker";

	// enable debug mode (print debug messages)
	public static boolean debug = true;

	// toggle to stop inventory contents to be restored when a new gui is opened and automatically closes the old one
	public static boolean openingNewGUI = false;

	// these are returned from GameManagers when a new game is started/failed to start
	public static final int GAME_STARTED = 1, GAME_NOT_STARTED_ERROR = 0, GAME_NOT_ENOUGH_MONEY = 2, GAME_NOT_ENOUGH_MONEY_1 = 3, GAME_NOT_ENOUGH_MONEY_2 = 4;
	
	// plugin configuration
	private FileConfiguration config;
	
	// nms util
	private NMSUtil nms;

	// API
	private GameBoxAPI api;
	
	// economy
	public static Economy econ = null;

	// language file
	public GameBoxLanguage lang;

	/*
	 * Plugin manager that manages all game managers
	 * Listens to events and passes them on
 	 */
	private PluginManager pManager;

	private MainCommand mainCommand;
	private AdminCommand adminCommand;

	private DataBase dataBase;

	private Metrics metrics;

	private LeftGameBoxListener leftGameBoxListener;
	private EnterGameBoxListener enterGameBoxListener;

	@Deprecated
	public static boolean playSounds = GameBoxSettings.playSounds;

	private GameRegistry gameRegistry;


	@Override
	public void onEnable(){
		// get the version and set up nms
		if (!setUpNMS()) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - + - +");
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " Your server version is not compatible with this plugin!");
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "   Get the newest version on Spigot:");
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "   https://www.spigotmc.org/resources/37273/");
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - + - +");

			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		this.gameRegistry = new GameRegistry(this);
		// GameBox module
		new Module(this, MODULE_GAMEBOX);

		if (!reload()) {
			getLogger().severe(" Problem while loading the plugin! Plugin was disabled!");

			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		registerGames();

		if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")){
			new PlaceholderAPIHook(this, MODULE_GAMEBOX);
			Bukkit.getConsoleSender().sendMessage(lang.PREFIX + " Hooked into PlaceholderAPI");
		}

		// send data with bStats if not opt out
		if(GameBoxSettings.bStats) {
			metrics = new Metrics(this);

			// Bar chart of all installed games
			metrics.addCustomChart(new Metrics.SimpleBarChart("gamebox_games", new Callable<Map<String, Integer>>(){
				@Override
				public HashMap<String, Integer> call() throws Exception {
					HashMap<String, Integer> valueMap = new HashMap<>();
					for(String gameID : getPluginManager().getGames().keySet()){
						valueMap.put(getOriginalGameName(gameID), 1);
					}
					return valueMap;
				}
			}));

			// Drill down pie with number of games and further breakdown of proportions of the games
			metrics.addCustomChart(new Metrics.DrilldownPie("games_drill_down", () -> {
				Map<String, Map<String, Integer>> map = new HashMap<>();

				Map<String, Integer> entry = new HashMap<>();

				for(String gameID : getPluginManager().getGames().keySet()){
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
			if(GameBoxSettings.tokensEnabled)
				metrics.addCustomChart(new Metrics.SimplePie("gamebox_shop_enabled"
					, () -> getPluginManager().getGuiManager().getShopManager().isClosed() ?
						"Closed" : "Open"));

		} else {
			Bukkit.getConsoleSender().sendMessage(lang.PREFIX + " You have opt out bStats... That's sad!");
		}


		// check for registered games
		// running as a bukkit runnable ensures that the task is run as soon as all plugins are loaded.
		new BukkitRunnable(){
			@Override
			public void run() {
				debug(" running late check in GameBox");
				runLateChecks();
			}
		}.runTask(this);
	}

	private void runLateChecks() {
		if(GameBoxSettings.checkInventoryLength) {
			info(ChatColor.RED + " Your server version can't handle more then 32 characters in inventory titles!");
			info(ChatColor.RED + " GameBox will shorten too long titles (marked by '...') to prevent errors.");
			info(ChatColor.RED + " To fix this ('...'), create your own language file with shorter titles.");
		}

		adminCommand.printIncompleteLangFilesInfo();

		if(PluginManager.gamesRegistered == 0){
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
				, "me.nikl.gamebox.games.connectfour.ConnectFour");
		new Module(this, MODULE_COOKIECLICKER
				, "me.nikl.gamebox.games.cookieclicker.CookieClicker");
	}

	/**
	 * Reload method called onEnable and on the reload command
	 *
	 * get the configuration
	 * set up economy if enabled
	 */
	public boolean reload(){

		if(!reloadConfiguration()){
			getLogger().severe(" Failed to load config file!");
			return false;
		}

		// copy default language file from jar to language folders
		FileUtil.copyDefaultLanguageFiles();

		// get gamebox language file
		this.lang = new GameBoxLanguage(this);

		this.api = new GameBoxAPI(this);

		// load all settings from config
		GameBoxSettings.loadSettings(this);

		// if it's not null disable first then get a new manager
		if(pManager != null){
			pManager.shutDown();
			HandlerList.unregisterAll(pManager);
			pManager = null;
		}

		// here try connecting to database when option set in config (for later)
		if(GameBoxSettings.useMysql){
			// on reload the old dataBase have to be saved before loading the new one
			if(dataBase != null) dataBase.save(false);

			// get and load a new statistic
			this.dataBase = new MysqlDB(this);
			if (!dataBase.load(false)) {
				getLogger().log(Level.SEVERE, " Falling back to file storage...");
				GameBoxSettings.useMysql = false;
				dataBase = null;
			}
		}

		// if connecting to the database failed useMysql will be set to false
		// and the plugin should fall back to file storage
		if(!GameBoxSettings.useMysql) {

			// on reload the old dataBase have to be saved before loading the new one
			if(dataBase != null) dataBase.save(false);

			// get and load a new file dataBase
			this.dataBase = new FileDB(this);
			if (!dataBase.load(false)) {
				getLogger().log(Level.SEVERE, " Something went wrong with the data file");
				return false;
			}
		}

		if(GameBoxSettings.econEnabled){
			if (!setupEconomy()){
				getLogger().log(Level.SEVERE, "No economy found!");
				getLogger().log(Level.SEVERE, "Even though it is enabled in the configuration file...");
				GameBoxSettings.econEnabled = false;
				return false;
			}
		}

		// renew the GameBox listeners
		reloadListeners();

		// get a new plugin manager and set the other managers and handlers
		pManager = new PluginManager(this);
		pManager.setGuiManager(new GUIManager(this));

		pManager.setHandleInviteInput(new HandleInviteInput(this));
		pManager.setHandleInvitations(new HandleInvitations(this));

		// set cmd executors
		mainCommand = new MainCommand(this);
		adminCommand = new AdminCommand(this);
		this.getCommand("gamebox").setExecutor(mainCommand);
		this.getCommand("gameboxadmin").setExecutor(adminCommand);

		// load players that are already online (otherwise done on join)
		pManager.loadPlayers();

		return true;
	}

	private void reloadListeners() {
		if(leftGameBoxListener != null){
			HandlerList.unregisterAll(leftGameBoxListener);
			leftGameBoxListener = null;
		}
		leftGameBoxListener = new LeftGameBoxListener(this);

		if(enterGameBoxListener != null){
			HandlerList.unregisterAll(enterGameBoxListener);
			enterGameBoxListener = null;
		}
		enterGameBoxListener = new EnterGameBoxListener(this);
	}


	public DataBase getDataBase(){
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


	private boolean setUpNMS() {
		String version;
		
		try {
			version = Bukkit.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
		
		debug("Your server is running version " + version);
		
		switch (version) {
			case "v1_8_R1":
				nms = new NMSUtil_1_8_R1();
				GameBoxSettings.delayedInventoryUpdate = true;
				break;

			case "v1_8_R2":
				nms = new NMSUtil_1_8_R2();
				GameBoxSettings.delayedInventoryUpdate = true;
				break;

			case "v1_8_R3":
				nms = new NMSUtil_1_8_R3();
				GameBoxSettings.delayedInventoryUpdate = true;
				break;

			case "v1_9_R1":
				nms = new NMSUtil_1_9_R1();
				break;

			case "v1_9_R2":
				nms = new NMSUtil_1_9_R2();
				break;

			case "v1_10_R1":
				nms = new NMSUtil_1_10_R1();
				break;

			case "v1_11_R1":
				nms = new NMSUtil_1_11_R1();
				break;

			case "v1_12_R1":
				nms = new NMSUtil_1_12_R1();
				break;
		}
		return nms != null;
	}


	@Override
	public void onDisable(){
		if(pManager != null) pManager.shutDown();
		if(dataBase != null) dataBase.onShutDown();
	}


	@Override
	public FileConfiguration getConfig() {
		return config;
	}


	public PluginManager getPluginManager() {
		return pManager;
	}


	public NMSUtil getNMS() {
		return nms;
	}


	public static void debug(String message){
		if(debug) Bukkit.getConsoleSender().sendMessage(message);
	}


	public MainCommand getMainCommand(){
		return this.mainCommand;
	}


	public boolean reloadConfiguration(){

		// save the default configuration file if the file does not exist
		File con = new File(this.getDataFolder().toString() + File.separatorChar + "config.yml");
		if(!con.exists()){
			this.saveResource("config.yml", false);
		}

		// reload config
		try {
			this.config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(con), "UTF-8"));
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


	public boolean wonTokens(UUID player, int tokens, String gameID){
		if(!GameBoxSettings.tokensEnabled) return false;
		return this.pManager.wonTokens(player, tokens, gameID);
	}

	public GameBoxAPI getApi(){
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
	 * @param gameID Id of the game
	 * @return the original name, or 'Other (custom game)'
	 */
	private String getOriginalGameName(String gameID){
		GameLanguage gameLang = getPluginManager().getGame(gameID).getGameLang();
		if(gameLang != null) return gameLang.DEFAULT_PLAIN_NAME;

		// is also set as default name, if not set in language file
		return "Other (custom game)";
	}

	public FileConfiguration getConfig(Module module) {
		return getConfig(module.getModuleID());
	}

	public FileConfiguration getConfig(String moduleId) {
		if(moduleId.equals(MODULE_GAMEBOX))
			return getConfig();

		Game game = getPluginManager().getGame(moduleId);

		if(game == null) return null;

		return game.getConfig();
	}

	public Language getLanguage(Module module) {
		return getLanguage(module.getModuleID());
	}

	public Language getLanguage(String moduleID) {
		if(moduleID.equals(MODULE_GAMEBOX))
			return lang;

		Game game = getPluginManager().getGame(moduleID);

		if(game == null) return null;

		return game.getGameLang();
	}

	public void info(String message) {
		Bukkit.getConsoleSender().sendMessage(lang.PREFIX + message);
	}

	public GameRegistry getGameRegistry() {
		return gameRegistry;
	}
}