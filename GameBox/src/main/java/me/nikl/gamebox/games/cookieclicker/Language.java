package me.nikl.gamebox.games.cookieclicker;

import me.nikl.cookieclicker.buildings.Buildings;
import me.nikl.cookieclicker.upgrades.UpgradeType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Language {
	private Main plugin;
	private FileConfiguration langFile;
	
	public String PREFIX = "[CookieClicker]", NAME = "&1CookieClicker&r";
	public List<String> GAME_HELP, GAME_OVEN_LORE, GAME_BUILDING_LORE;
	private YamlConfiguration defaultLang;

	public String GAME_TITLE, GAME_CLOSED
			, GAME_COOKIE_NAME, GAME_OVEN_NAME, GAME_BUILDING_NAME;
	public String GAME_PAYED, GAME_NOT_ENOUGH_MONEY;

	public HashMap<Buildings, String> buildingName = new HashMap<>();
	public HashMap<Buildings, List<String>> buildingLore = new HashMap<>();

	public String GAME_UPGRADE_NAME;
	public List<String> GAME_UPGRADE_LORE;
	public HashMap<Integer, String> upgradeName = new HashMap<>();
	public HashMap<Integer, List<String>> upgradeDescriptionLore = new HashMap<>();
	public HashMap<UpgradeType, List<String>> upgradeLore = new HashMap<>();

	public Language(Main plugin){
		this.plugin = plugin;
		getLangFile();
		PREFIX = getString("prefix");
		NAME = getString("name");


		getGameMessages();
		// saving building language in hash maps
		loadBuildingLanguage();
		// saving upgrade language in hash maps
		loadUpgradeLanguage();
	}

	private void loadUpgradeLanguage() {
		this.GAME_UPGRADE_LORE = getStringList("upgrades.upgradeLore");
		this.GAME_UPGRADE_NAME = getString("upgrades.upgradeDisplayName");


		UpgradeType upgradeType;
		List<String> lore = new ArrayList<>();

		// load middle lore
		if(langFile.isConfigurationSection("upgrades.types")) {
			for (String key : langFile.getConfigurationSection("upgrades.types").getKeys(false)) {
				try {
					upgradeType = UpgradeType.valueOf(key.toUpperCase());
				} catch (IllegalArgumentException exception) {
					// ignore
					continue;
				}
				lore.clear();
				lore.addAll(GAME_UPGRADE_LORE);
				lore.addAll(getStringList("upgrades.types." + key));
				upgradeLore.put(upgradeType, new ArrayList<>(lore));
			}
		}
		// check for missing middle lore
		for (String key : defaultLang.getConfigurationSection("upgrades.types").getKeys(false)) {
			try {
				upgradeType = UpgradeType.valueOf(key.toUpperCase());
			} catch (IllegalArgumentException exception) {
				// ignore
				continue;
			}
			if(upgradeLore.containsKey(upgradeType)) continue;
			lore.clear();
			lore.addAll(GAME_UPGRADE_LORE);
			lore.addAll(getStringList("upgrades.types." + key));
			upgradeLore.put(upgradeType, new ArrayList<>(lore));
		}


		int id;
		// load description and names
		if(langFile.isConfigurationSection("upgrades.upgrades")) {
			for (String key : langFile.getConfigurationSection("upgrades.upgrades").getKeys(false)) {
				try {
					id = Integer.valueOf(key);
				} catch (NumberFormatException exception) {
					// ignore
					continue;
				}
				upgradeDescriptionLore.put(id, getStringList("upgrades.upgrades." + key + ".description"));
				upgradeName.put(id, getString("upgrades.upgrades." + key + ".name"));
			}
		}
		// check for missing description and names
		if(langFile.isConfigurationSection("upgrades.upgrades")) {
			for (String key : defaultLang.getConfigurationSection("upgrades.upgrades").getKeys(false)) {
				try {
					id = Integer.valueOf(key);
				} catch (NumberFormatException exception) {
					// ignore
					continue;
				}
				if(upgradeDescriptionLore.containsKey(id) && upgradeName.containsKey(id)) continue;
				upgradeDescriptionLore.put(id, getStringList("upgrades.upgrades." + key + ".description"));
				upgradeName.put(id, getString("upgrades.upgrades." + key + ".name"));
			}
		}
	}

	private void loadBuildingLanguage() {
		this.GAME_BUILDING_LORE = getStringList("buildings.buildingLore");
		this.GAME_BUILDING_NAME = getString("buildings.buildingDisplayName");
		Buildings building;
		List<String> lore = new ArrayList<>();

		if(langFile.isConfigurationSection("buildings")) {
			for (String key : langFile.getConfigurationSection("buildings").getKeys(false)) {
				try {
					building = Buildings.valueOf(key.toUpperCase());
				} catch (IllegalArgumentException exception) {
					// ignore
					continue;
				}
				lore.clear();
				lore.addAll(GAME_BUILDING_LORE);
				buildingName.put(building, getString("buildings." + key + ".name"));
				lore.addAll(getStringList("buildings." + key + ".description"));
				buildingLore.put(building, new ArrayList<>(lore));
			}
		}

		// check for missing language in default file
		for(String key : defaultLang.getConfigurationSection("buildings").getKeys(false)){
			try{
				building = Buildings.valueOf(key.toUpperCase());
			} catch (IllegalArgumentException exception){
				// ignore
				continue;
			}
			if(buildingLore.containsKey(building) && buildingName.containsKey(building)) continue;
			lore.clear();
			lore.addAll(GAME_BUILDING_LORE);
			buildingName.put(building, getString("buildings." + key + ".name"));
			lore.addAll(getStringList("buildings." + key + ".description"));
			buildingLore.put(building, new ArrayList<>(lore));
		}
	}

	private void getGameMessages() {
		this.GAME_TITLE = getString("game.inventoryTitles.gameTitle");

		this.GAME_COOKIE_NAME = getString("game.cookieName");

		this.GAME_OVEN_NAME = getString("game.ovenName");
		this.GAME_OVEN_LORE = getStringList("game.ovenLore");

		this.GAME_PAYED = getString("game.econ.payed");
		this.GAME_NOT_ENOUGH_MONEY = getString("game.econ.notEnoughMoney");

		this.GAME_CLOSED = getString("game.closedGame");

		this.GAME_HELP = getStringList("gameHelp");
	}


	private List<String> getStringList(String path) {
		List<String> toReturn;
		if(!langFile.isList(path)){
			toReturn = defaultLang.getStringList(path);
			for(int i = 0; i<toReturn.size(); i++){
				toReturn.set(i, ChatColor.translateAlternateColorCodes('&',toReturn.get(i)));
			}
			return toReturn;
		}
		toReturn = langFile.getStringList(path);
		for(int i = 0; i<toReturn.size(); i++){
			toReturn.set(i, ChatColor.translateAlternateColorCodes('&',toReturn.get(i)));
		}
		return toReturn;
	}

	private String getString(String path) {
		if(!langFile.isString(path)){
			// get string from default lang file
			return ChatColor.translateAlternateColorCodes('&',defaultLang.getString(path));
		}
		return ChatColor.translateAlternateColorCodes('&',langFile.getString(path));
	}

	private void getLangFile() {
		// load the default language as YML config from the english lang file in the jar
		try {
			String fileName = "language/lang_en.yml";
			this.defaultLang =  YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(fileName), "UTF-8"));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		// check for all default language files. If not found in the plugin folder: copy from the jar
		File defaultEn = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "lang_en.yml");
		if(!defaultEn.exists()){
			defaultEn.getParentFile().mkdirs();
			plugin.saveResource("language" + File.separatorChar + "lang_en.yml", false);
		}
		File defaultDe = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "lang_de.yml");
		if(!defaultDe.exists()){
			plugin.saveResource("language" + File.separatorChar + "lang_de.yml", false);
		}
		File defaultZh = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "lang_zh-cn.yml");
		if(!defaultZh.exists()){
			plugin.saveResource("language" + File.separatorChar + "lang_zh-cn.yml", false);
		}
		if(!plugin.getConfig().isString("langFile")){
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Language file is missing in the config!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Add the following to your config:"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " langFile: 'lang_en.yml'"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Using default language file"));
			this.langFile = defaultLang;
			return;
		}
		String fileName = plugin.getConfig().getString("langFile");
		if(fileName.equalsIgnoreCase("default") || fileName.equalsIgnoreCase("default.yml")){
			this.langFile = defaultLang;
			return;
		}
		File languageFile = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + plugin.getConfig().getString("langFile"));
		if(!languageFile.exists()){
			languageFile.mkdir();
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Language file not found!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Using default language file"));
			this.langFile = defaultLang;
			return;
		}
		try { 
			this.langFile = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(languageFile), "UTF-8"));
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace(); 
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Error in language file!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Using default language file"));
			this.langFile = defaultLang;
			return;
		}
		int count = 0;
		for(String key : defaultLang.getKeys(true)){
			if(defaultLang.isString(key)){
				if(!this.langFile.isString(key)){// there is a message missing
					if(count == 0){
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*"));
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Missing message(s) in your language file!"));
					}
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " " + key));
					count++;
				}
			} else if (defaultLang.isList(key)){
				if(!this.langFile.isList(key)){// there is a message missing
					if(count == 0){
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*"));
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Missing message(s) in your language file!"));
					}
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " " + key + "     (StringList!)"));
					count++;
				}
			}
		}
		if(count > 0){
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + ""));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Game will use default messages for these paths"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + ""));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Please get an updated language file"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Or add the listed paths by hand"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*"));
		}
		return;
		
	}
	
}

