package me.nikl.gamebox;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.List;


/**
 * Created by niklas on 10/17/16.
 *
 * Language class
 * Get all messages on enable
 * save not saved default lang files
 */
public class Language {
	private Main plugin;
	private FileConfiguration langFile;
	
	// main plugin
	public String CMD_NO_PERM, CMD_ONLY_PLAYER, CMD_RELOADED;
	public List<String> CMD_HELP, CMD_WRONG_USAGE;
	private YamlConfiguration defaultLang;
	public String TITLE_MAIN_GUI;
	
	// buttons
	public String BUTTON_EXIT, BUTTON_MAIN_GUI, BUTTON_GAME_GUI, BUTTON_BACK, BUTTON_FORWARD;
	
	// GemCrush
	public String GEMCRUSH_FINISHED_WITH_PAY, GEMCRUSH_FINISHED_NO_PAY, GEMCRUSH_PAYED, GEMCRUSH_NOT_ENOUGH_MONEY;
	public String GEMCRUSH_GAME_TITLE;
	
	// Minesweeper
	public String MINESWEEPER_GAME_PAYED, MINESWEEPER_GAME_NOT_ENOUGH_MONEY, MINESWEEPER_GAME_WON_MONEY, MINESWEEPER_TITLE_BEGINNING,
		MINESWEEPER_TITLE_INGAME, MINESWEEPER_TITLE_END, MINESWEEPER_TITLE_LOST;
	
	Language(Main plugin){
		this.plugin = plugin;
		getLangFile();
		
		getCommandMessages();
		getInvTitles();
		getGemCrushMessages();
		getButtons();
		getMinesweeperMessages();
	}
	
	
	private void getMinesweeperMessages() {
		MINESWEEPER_GAME_PAYED = getString("minesweeper.econ.payed");
		MINESWEEPER_GAME_NOT_ENOUGH_MONEY = getString("minesweeper.econ.notEnoughMoney");
		MINESWEEPER_GAME_WON_MONEY = getString("minesweeper.econ.wonMoney");
		
		MINESWEEPER_TITLE_BEGINNING = getString("minesweeper.inventoryTitles.beginning");
		MINESWEEPER_TITLE_INGAME = getString("minesweeper.inventoryTitles.ingame");
		MINESWEEPER_TITLE_END = getString("minesweeper.inventoryTitles.won");
		MINESWEEPER_TITLE_LOST = getString("minesweeper.inventoryTitles.lost");
	}
	
	private void getButtons() {
		BUTTON_EXIT  = getString("mainButtons.exitButton");
		BUTTON_MAIN_GUI  = getString("mainButtons.toMainGUIButton");
		BUTTON_GAME_GUI  = getString("mainButtons.toGameGUIButton");
		BUTTON_BACK  = getString("mainButtons.backwardButton");
		BUTTON_FORWARD  = getString("mainButtons.forwardButton");
	}
	
	private void getGemCrushMessages() {
		GEMCRUSH_FINISHED_WITH_PAY  = getString("gemcrush.finishedWithPayout");
		GEMCRUSH_FINISHED_NO_PAY = getString("gemcrush.finishedWithoutPayout");
		GEMCRUSH_PAYED = getString("gemcrush.econ.payed");
		GEMCRUSH_NOT_ENOUGH_MONEY = getString("gemcrush.econ.notEnoughMoney");
		
		GEMCRUSH_GAME_TITLE = getString("gemcrush.game.inventoryTitle");
	}
	
	private void getInvTitles() {
		// main GUI
		this.TITLE_MAIN_GUI = getString("inventoryTitles.mainGUI");
		
		
	}
	
	private void getCommandMessages() {
		
		this.CMD_NO_PERM = getString("commandMessages.noPermission");
		this.CMD_ONLY_PLAYER = getString("commandMessages.onlyAsPlayer");
		this.CMD_RELOADED = getString("commandMessages.pluginReloaded");
		
		
		this.CMD_HELP = getStringList("commandMessages.help");
		this.CMD_WRONG_USAGE = getStringList("commandMessages.wrongUsage");
	}
	
	private List<String> getStringList(String path) {
		if(!langFile.isList(path)){
			return defaultLang.getStringList(path);
		}
		return langFile.getStringList(path);
	}
	
	private String getString(String path) {
		if(!langFile.isString(path)){
			return defaultLang.getString(path);
		}
		return langFile.getString(path);
	}
	
	private void getLangFile() {
		InputStream inputStream = null;
		OutputStream outputStream = null;
		
		/*
		 * The default file will always contain the up to date english messages
		 *
		 * Messages from this file will be used if there are some missing
		 * in the given language file. The missing keys will be listed in the console.
		 */
		File defaultFile = null;
		try {
			
			// read this file into InputStream
			String fileName = "language/lang_en.yml";
			inputStream = plugin.getResource(fileName);
			
			// write the inputStream to a FileOutputStream
			defaultFile = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "default.yml");
			defaultFile.getParentFile().mkdirs();
			outputStream = new FileOutputStream(defaultFile);
			
			int read;
			byte[] bytes = new byte[1024];
			
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
		try {
			this.defaultLang =  YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(defaultFile), "UTF-8"));
		} catch (UnsupportedEncodingException | FileNotFoundException e2) {
			e2.printStackTrace();
		}
		File defaultEn = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "lang_en.yml");
		if(!defaultEn.exists()){
			defaultEn.getParentFile().mkdirs();
			plugin.saveResource("language" + File.separatorChar + "lang_en.yml", false);
		}
		File defaultDe = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "lang_de.yml");
		if(!defaultDe.exists()){
			plugin.saveResource("language" + File.separatorChar + "lang_de.yml", false);
		}
		
		if(!plugin.getConfig().isSet("langFile")){
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Language file is missing in the config!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " Add the following to your config:"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " langFile: 'default.yml'"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Using default language file"));
			this.langFile = defaultLang;
		} else {
			if(!plugin.getConfig().isString("langFile")){
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Language file is invalid (no String)!"));
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Using default language file"));
				this.langFile = defaultLang;
			} else {
				File languageFile = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + plugin.getConfig().getString("langFile"));
				if(!languageFile.exists()){
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Language file not found!"));
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Using default language file"));
					this.langFile = defaultLang;
				} else {
					try {
						this.langFile = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(languageFile), "UTF-8"));
					} catch (UnsupportedEncodingException | FileNotFoundException e) {
						e.printStackTrace();
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Error while loading language file!"));
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Using default language file"));
						this.langFile = defaultLang;
					}
				}
			}
		}
		/*
		 * get missing keys and print them
		 */
		int count = 0;
		for(String key : defaultLang.getKeys(true)){
			if(defaultLang.isString(key)){
				if(!this.langFile.isString(key)){// there is a message missing
					if(count == 0){
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*"));
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Missing message(s) in your language file!"));
					}
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " " + key));
					count++;
				}
			}
		}
		if(count > 0){
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + ""));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Game will use default messages for these paths"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + ""));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Please use an up to date language file"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Or add the listed paths to your custom file"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*"));
		}
		return;
		
	}
	
	
}

