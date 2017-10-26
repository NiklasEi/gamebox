package me.nikl.gamebox.games.cookieclicker;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Language;
import me.nikl.gamebox.games.cookieclicker.buildings.Buildings;
import me.nikl.gamebox.games.cookieclicker.upgrades.UpgradeType;
import me.nikl.gamebox.util.LanguageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class CookieClickerLanguage extends Language {

	public List<String> GAME_HELP, GAME_OVEN_LORE, GAME_BUILDING_LORE;

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

	public CookieClickerLanguage(GameBox plugin){
		super(plugin, LanguageUtil.Namespace.COOKIECLICKER);
	}


	@Override
	public void loadMessages() {
		PREFIX = getString("prefix");
		PLAIN_PREFIX = ChatColor.stripColor(PREFIX);

		NAME = getString("name");
		PLAIN_NAME = ChatColor.stripColor(NAME);

		getGameMessages();
		// saving building language in hash maps
		loadBuildingLanguage();
		// saving upgrade language in hash maps
		loadUpgradeLanguage();
	}

	/**
	 * Custom loading of the upgrade language, since
	 * the lists are saved in maps with their upgrade type.
	 */
	private void loadUpgradeLanguage() {
		this.GAME_UPGRADE_LORE = getStringList("upgrades.upgradeLore");
		this.GAME_UPGRADE_NAME = getString("upgrades.upgradeDisplayName");

		FileConfiguration langFile = LanguageUtil.lan

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
}