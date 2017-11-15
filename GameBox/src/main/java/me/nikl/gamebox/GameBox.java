package me.nikl.gamebox;

import me.nikl.gamebox.commands.AdminCommand;
import me.nikl.gamebox.commands.MainCommand;
import me.nikl.gamebox.data.PlaceholderAPIHook;
import me.nikl.gamebox.data.Statistics;
import me.nikl.gamebox.data.StatisticsFile;
import me.nikl.gamebox.data.StatisticsMysql;
import me.nikl.gamebox.games.GameLanguage;
import me.nikl.gamebox.games.GameSettings;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.listeners.EnterGameBoxListener;
import me.nikl.gamebox.listeners.LeftGameBoxListener;
import me.nikl.gamebox.nms.*;
import me.nikl.gamebox.players.HandleInvitations;
import me.nikl.gamebox.players.HandleInviteInput;
import me.nikl.gamebox.util.FileUtil;
import me.nikl.gamebox.util.Module;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;

/**
 * Created by niklas on 10/27/16.
 *
 * Main class of the plugin GameBox
 */
public class GameBox extends JavaPlugin{

	// enable debug mode (print debug messages)
	public static final boolean debug = false;

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

	private Statistics statistics;

	private Metrics metrics;

	private LeftGameBoxListener leftGameBoxListener;
	private EnterGameBoxListener enterGameBoxListener;

	@Deprecated
	public static boolean playSounds = GameBoxSettings.playSounds;



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

		if (!reload()) {
			getLogger().severe(" Problem while loading the plugin! Plugin was disabled!");

			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")){
			new PlaceholderAPIHook(this, "gamebox");
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
					for(Module module : getPluginManager().getGames().keySet()){
						valueMap.put(getOriginalGameName(module), 1);
					}
					return valueMap;
				}
			}));

			// Drill down pie with number of games and further breakdown of proportions of the games
			metrics.addCustomChart(new Metrics.DrilldownPie("games_drill_down", () -> {
				Map<String, Map<String, Integer>> map = new HashMap<>();

				Map<String, Integer> entry = new HashMap<>();

				for(Module module : getPluginManager().getGames().keySet()){
					entry.put(getOriginalGameName(module), 1);
				}

				map.put(String.valueOf(getPluginManager().getGames().size()), entry);
				return map;
			}));

			// Pie chart with number of games
			metrics.addCustomChart(new Metrics.SimplePie("number_of_gamebox_games"
					, () -> String.valueOf(PluginManager.gamesRegistered)));
		} else {
			Bukkit.getConsoleSender().sendMessage(lang.PREFIX + " You have opt out bStats... That's sad!");
		}


		// check for registered games
		new BukkitRunnable(){
			@Override
			public void run() {
				if(PluginManager.gamesRegistered == 0){
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - + - +");
					Bukkit.getConsoleSender().sendMessage(lang.PREFIX + ChatColor.RED + " There are no registered games!");
					Bukkit.getConsoleSender().sendMessage(lang.PREFIX + ChatColor.RED + " All games are add-ons");
					Bukkit.getConsoleSender().sendMessage(lang.PREFIX + ChatColor.RED + " You should visit Spigot and get a few ;)");
					Bukkit.getConsoleSender().sendMessage(lang.PREFIX + ChatColor.RED + "   https://www.spigotmc.org/resources/37273/");
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - + - +");
				} else {
					Bukkit.getConsoleSender().sendMessage(lang.PREFIX + ChatColor.GREEN + " " + PluginManager.gamesRegistered + " games were registered. Have fun :)");
				}
			}
		}.runTaskLaterAsynchronously(this, 100);
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
			getLogger().log(Level.INFO, "- - - - - - - - - - - - - - - - - - - -");
			getLogger().log(Level.INFO, "This plugin version does not support MYSQL!");
			getLogger().log(Level.INFO, "Falling back to file storage!");
			getLogger().log(Level.INFO, "- - - - - - - - - - - - - - - - - - - -");
			GameBoxSettings.useMysql = false;

			// on reload the old statistics have to be saved before loading the new one
			if(statistics != null) statistics.save();

			// get and load a new statistic
			this.statistics = new StatisticsMysql(this);
			if (!statistics.load()) {
				getLogger().log(Level.SEVERE, " Falling back to file storage...");
				GameBoxSettings.useMysql = false;
				statistics = null;
			}
		}

		// if connecting to the database failed useMysql will be set to false
		// and the plugin should fall back to file storage
		if(!GameBoxSettings.useMysql) {

			// on reload the old statistics have to be saved before loading the new one
			if(statistics != null) statistics.save();

			// get and load a new file statistics
			this.statistics = new StatisticsFile(this);
			if (!statistics.load()) {
				getLogger().log(Level.SEVERE, " Something went wrong with the data file");
				return false;
			}
		}

		if(GameBoxSettings.econEnabled){
			if (!setupEconomy()){
				getLogger().log(Level.SEVERE, "No economy found!");
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

		// load players that are already online (otherwise done on join)
		pManager.loadPlayers();

		// set cmd executors
		mainCommand = new MainCommand(this);
		this.getCommand("gamebox").setExecutor(mainCommand);
		this.getCommand("gameboxadmin").setExecutor(new AdminCommand(this));

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


	public Statistics getStatistics(){
		return this.statistics;
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
		
		if(debug) getLogger().info("Your server is running version " + version);
		
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
		if(statistics != null) statistics.save();
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
	 * Get the original game name from the module enum
	 *
	 * This is to make the statistics on bStats nicer.
	 * Since the game names can be changed, the statistics would be messed up,
	 * when using the customized game names.
	 * Try to get the default name from the default
	 * language files, then fall back on hardcoded names.
	 * @param module Id of the game
	 * @return the original name if given, otherwise the ID itself
	 */
	private String getOriginalGameName(Module module){
		if(module == null) return "null";

		GameLanguage gameLang = getPluginManager().getGame(module).getGameLang();
		if(gameLang != null) return gameLang.PLAIN_NAME;

		String gameID = module.moduleID();
		switch (gameID){
			case "minesweeper": return "Minesweeper";
			case "battleship": return "Battleship";
			case "whacamole": return "WhacAMole";
			case "2048": return "2048";
			case "gemcrush": return "GemCrush";
			case "sudoku": return "Sudoku";
			case "connect4": return "ConnectFour";

			// prevent chaos on bstats...

			case "cookieclicker": return "Cookie Clicker";
			case "tictactoe": return "Tic-tac-toe";
			case "rockpaperscissors": return "Rock–paper–scissors";
			case "fruitninja": return "Fruit Ninja";
			case "solitaire": return "Solitaire";
			case "headsortails": return "Heads or Tails";
			case "mastermind": return "Mastermind";
			case "tetris": return "Tetris";
			case "chess": return "Chess";
		}
		return gameID;
	}
}
