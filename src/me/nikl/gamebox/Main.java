package me.nikl.gamebox;

import me.nikl.gamebox.commands.MainCommand;
import me.nikl.gamebox.nms.*;
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
 * Main class of the plugin GameBox
 */
public class Main extends JavaPlugin{
	
	// enable debug modus (print debug messages)
	public static final boolean debug = true;
	
	// plugin configuration
	private FileConfiguration config;
	
	// prefixes
	public static final String prefix = "[&1GameBox&r]";
	public static final String plainPrefix = "[GameBox]";
	
	// nms util
	private NMSUtil nms;
	
	// economy
	public static Economy econ = null;
	private boolean econEnabled;
	
	// language file
	public Language lang;
	
	/*
	 * Plugin manager that manages all game managers
	 * Listens to events and passes them on
 	 */
	private PluginManager pManager;
	
	
	@Override
	public void onEnable(){
		// get the version and set up nms
		if (!setUpNMS()) {
			getLogger().severe(Main.plainPrefix + " Your server version is not compatible with this plugin!");
			
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		if (!reload()) {
			getLogger().severe(Main.plainPrefix + " Error while loading the plugin! Plugin was disabled!");
			
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
		}
		InputStream defConfigStream = this.getResource("config.yml");
		if (defConfigStream != null){
			@SuppressWarnings("deprecation")
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			this.config.setDefaults(defConfig);
		}
		
		this.lang = new Language(this);
		
		// set up the economy if enabled in the configuration
		this.econEnabled = false;
		if(Main.debug)Bukkit.getConsoleSender().sendMessage("econ enabled: " + getConfig().getBoolean("economy.enabled"));
		if(getConfig().getBoolean("economy.enabled")){
			this.econEnabled = true;
			if (!setupEconomy()){
				Bukkit.getLogger().log(Level.SEVERE, plainPrefix + " &4No economy found!");
				return false;
			}
		}
		
		// if it's not null disable first then get a new manager
		if(pManager != null){
			pManager.shutDown();
			pManager = null;
		}
		pManager = new PluginManager(this);
		
		// set cmd executor
		this.getCommand("gamebox").setExecutor(new MainCommand(this));
		
		return true;
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
}
