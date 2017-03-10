package me.nikl.gamebox;

import me.nikl.gamebox.commands.MainCommand;
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

import java.io.*;
import java.util.logging.Level;

/**
 * Created by niklas on 10/27/16.
 *
 * GameBox class of the plugin GameBox
 */
public class GameBox extends JavaPlugin{
	
	// enable debug mode (print debug messages)
	public static final boolean debug = false;

	// toggle to stop inventory contents to be restored when a new gui is opened and automatically closes the old one
	public static boolean openingNewGUI = false;

	// toggle for playing sounds
	public static boolean playSounds = false;

	public static final int GAME_STARTED = 1, GAME_NOT_STARTED_ERROR = 0, GAME_NOT_ENOUGH_MONEY = 2, GAME_NOT_ENOUGH_MONEY_1 = 3, GAME_NOT_ENOUGH_MONEY_2 = 4;
	
	// plugin configuration
	private FileConfiguration config;
	
	// nms util
	private NMSUtil nms;
	
	// economy
	public static Economy econ = null;
	private boolean econEnabled;
	
	// language file
	public Language lang;

	public static boolean useMysql = false;
	
	/*
	 * Plugin manager that manages all game managers
	 * Listens to events and passes them on
 	 */
	private PluginManager pManager;

	private MainCommand mainCommand;

	private Statistics statistics;


	// time in seconds for inputs and invitations
	public static int timeForPlayerInput;
	public static int timeForInvitations;

	
	
	@Override
	public void onEnable(){
		// get the version and set up nms
		if (!setUpNMS()) {
			getLogger().severe(" Your server version is not compatible with this plugin!");

			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		if(!reloadConfiguration()){
			getLogger().severe(" Failed to load config file!");

			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}


		useMysql = config.getBoolean("mysql.enabled", false);
		if(useMysql) {
			useMysql = false;
		}

		// check again
		// if connecting to the database failed useMysql will be set to false and the plugin should fall back to file storage
		if(!useMysql) {
			this.statistics = new StatisticsFile(this);
			if (!statistics.load()) {
				Bukkit.getLogger().log(Level.SEVERE, " Something went wrong with the data file");
				Bukkit.getPluginManager().disablePlugin(this);
				return;
			}
		}

		if (!reload()) {
			getLogger().severe(" Error while loading the plugin! Plugin was disabled!");

			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
	}
	
	/***
	 * Reload method called onEnable and on the reload command
	 *
	 * get the configuration
	 * set up economy if enabled
	 */
	public boolean reload(){
		timeForInvitations = config.getInt("timeForInvitations");
		timeForPlayerInput = config.getInt("timeForPlayerInput");


		this.lang = new Language(this);

		playSounds = config.getBoolean("guiSettings.playSounds");
		
		// set up the economy if enabled in the configuration
		this.econEnabled = false;
		if(GameBox.debug)Bukkit.getConsoleSender().sendMessage("econ enabled: " + getConfig().getBoolean("economy.enabled"));
		if(getConfig().getBoolean("economy.enabled")){
			this.econEnabled = true;
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
		pManager.loadPlayers();
		// set cmd executor
		mainCommand = new MainCommand(this);
		this.getCommand("gamebox").setExecutor(mainCommand);
		
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
				
				break;
			case "v1_8_R2":
				nms = new NMSUtil_1_8_R2();
				
				break;
			case "v1_8_R1":
				nms = new NMSUtil_1_8_R1();
				
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
		if(statistics!=null)statistics.save();
	}
	
	@Override
	public FileConfiguration getConfig() {
		return config;
	}
	
	public PluginManager getPluginManager() {
		return pManager;
	}
	
	public boolean getEconEnabled() {
		return econEnabled;
	}
	
	public String chatColor(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
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
}
