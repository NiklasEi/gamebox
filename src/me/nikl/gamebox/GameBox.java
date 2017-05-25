package me.nikl.gamebox;

import me.clip.placeholderapi.PlaceholderAPI;
import me.nikl.gamebox.commands.AdminCommand;
import me.nikl.gamebox.commands.MainCommand;
import me.nikl.gamebox.data.PlaceholderAPIHook;
import me.nikl.gamebox.data.Statistics;
import me.nikl.gamebox.data.StatisticsFile;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.nms.*;
import me.nikl.gamebox.players.HandleInvitations;
import me.nikl.gamebox.players.HandleInviteInput;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.UUID;
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
	public Language lang;

	/*
	 * Plugin manager that manages all game managers
	 * Listens to events and passes them on
 	 */
	private PluginManager pManager;

	private MainCommand mainCommand;

	private Statistics statistics;

	@Deprecated
	public static boolean playSounds = true;

	public boolean delayedInventoryUpdate = false;


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

		if(!reloadConfiguration()){
			getLogger().severe(" Failed to load config file!");

			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		if (!reload()) {
			getLogger().severe(" Error while loading the plugin! Plugin was disabled!");

			Bukkit.getPluginManager().disablePlugin(this);
			return;
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

		if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")){
			new PlaceholderAPIHook(this, "gamebox");
		}
	}
	
	/***
	 * Reload method called onEnable and on the reload command
	 *
	 * get the configuration
	 * set up economy if enabled
	 */
	public boolean reload(){
		this.lang = new Language(this);

		this.api = new GameBoxAPI(this);

		// load all settings from config
		GameBoxSettings.loadSettings(this);

		// ToDo: remove when removing all deprecated stuff
		playSounds = GameBoxSettings.playSounds;

		// disable mysql
		GameBoxSettings.useMysql = false;

		// here try connecting to database when option set in config (for later)

		// if connecting to the database failed useMysql will be set to false and the plugin should fall back to file storage
		if(!GameBoxSettings.useMysql) {
			this.statistics = new StatisticsFile(this);
			if (!statistics.load()) {
				Bukkit.getLogger().log(Level.SEVERE, " Something went wrong with the data file");
				return false;
			}
		}

		if(GameBoxSettings.econEnabled){
			if (!setupEconomy()){
				Bukkit.getLogger().log(Level.SEVERE, "No economy found!");
				return false;
			}
		}

		// if it's not null disable first then get a new manager
		if(pManager != null){
			pManager.shutDown();
			pManager = null;
		}

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
			case "v1_10_R1":
				nms = new NMSUtil_1_10_R1();
				
				break;
			case "v1_9_R2":
				nms = new NMSUtil_1_9_R2();
				
				break;
			case "v1_9_R1":
				nms = new NMSUtil_1_9_R1();
				
				break;
			case "v1_8_R3":
				nms = new NMSUtil_1_8_R3();
				delayedInventoryUpdate = true;

				break;
			case "v1_8_R2":
				nms = new NMSUtil_1_8_R2();
				delayedInventoryUpdate = true;

				break;
			case "v1_8_R1":
				nms = new NMSUtil_1_8_R1();
				delayedInventoryUpdate = true;

				break;
			case "v1_11_R1":
				nms = new NMSUtil_1_11_R1();
				
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


	/**
	 * Will be removed
	 *
	 * as of @version 1.3.0 this should be retrieved from GameBoxSettings directly
	 * @return
	 */
	@Deprecated
	public boolean getEconEnabled() {
		return GameBoxSettings.econEnabled;
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


	/**
	 * Will be removed
	 *
	 * as of @version 1.3.0 this should be retrieved from GameBoxSettings directly
	 * @return
	 */
	@Deprecated
	public boolean isTokensEnabled() {
		return GameBoxSettings.tokensEnabled;
	}


	/**
	 * Will be removed
	 *
	 * as of @version 1.3.0 this should be done with GameBoxSettings directly
	 * @return
	 */
	@Deprecated
	public void setTokensEnabled(boolean tokensEnabled){
		GameBoxSettings.tokensEnabled = tokensEnabled;
	}


	public GameBoxAPI getApi(){
		return this.api;
	}


	public static String chatColor(String message){
		return ChatColor.translateAlternateColorCodes('&', message);
	}
}
