package me.nikl.gamebox;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.List;
import java.util.logging.Level;


/**
 * Created by niklas on 10/17/16.
 *
 * Language class
 * Get all messages on enable
 * save not saved default lang files
 */
public class Language {
	private GameBox plugin;
	private FileConfiguration langFile;

	private YamlConfiguration defaultLang;


	public String PREFIX = "["+ChatColor.BLUE+"GameBox"+ChatColor.RESET+"]";
	public String NAME = ChatColor.BLUE+"GameBox"+ChatColor.RESET;
	public String PLAIN_PREFIX = "[GameBox]";

	// commands
	public String CMD_NO_PERM, CMD_ONLY_PLAYER, CMD_RELOADED, CMD_DISABLED_WORLD, CMD_TOKEN;
	public List<String> CMD_HELP, CMD_WRONG_USAGE, CMD_INFO_HEADER, CMD_INFO_PER_GAME, CMD_INFO_FOOTER;

	// Buttons
	public String BUTTON_EXIT, BUTTON_TO_MAIN_MENU, BUTTON_TO_GAME_MENU, BUTTON_TOKENS
			, BUTTON_FORWARD, BUTTON_BACK, BUTTON_SOUND_ON_NAME, BUTTON_SOUND_OFF_NAME
			, BUTTON_INVITE_BUTTON_NAME, BUTTON_INVITE_SKULL_NAME;
	public List<String> BUTTON_MAIN_MENU_INFO, BUTTON_SOUND_ON_LORE, BUTTON_SOUND_OFF_LORE
			, BUTTON_INVITE_BUTTON_LORE, BUTTON_INVITE_SKULL_LORE;

	// Inv titles
	public String TITLE_MAIN_GUI, TITLE_GAME_GUI, TITLE_NO_PERM, TITLE_NOT_ENOUGH_MONEY, TITLE_OTHER_PLAYER_NOT_ENOUGH_MONEY, TITLE_ALREADY_IN_ANOTHER_GAME,
			TITLE_ERROR = ChatColor.RED + "              Error", TITLE_NOT_ENOUGH_TOKEN;

	// shop
	public String SHOP_TITLE_BOUGHT_SUCCESSFULLY, SHOP_TITLE_INVENTORY_FULL, SHOP_TITLE_MAIN_SHOP, SHOP_TITLE_PAGE_SHOP, SHOP_TITLE_NOT_ENOUGH_TOKEN
			, SHOP_TITLE_NOT_ENOUGH_MONEY, SHOP_FREE, SHOP_MONEY, SHOP_TOKEN, SHOP_IS_CLOSED, SHOP_TITLE_REQUIREMENT_NOT_FULFILLED;

	// player input
	public String INPUT_START_MESSAGE, INPUT_TIME_RAN_OUT, INVITATION_SUCCESSFUL, INVITATION_ALREADY_THERE, INVITATION_NOT_VALID_PLAYER_NAME
			, INVITATION_NOT_ONLINE, INPUT_CLOSED, INVITATION_NOT_YOURSELF, INVITATION_PRE_TEXT, INVITATION_PRE_COLOR, INVITATION_CLICK_TEXT, INVITATION_CLICK_COLOR
			, INVITATION_HOVER_TEXT, INVITATION_HOVER_COLOR, INVITATION_AFTER_TEXT, INVITATION_AFTER_COLOR;

	// JSON prefix parts (click invite message)
	public String JSON_PREFIX_PRE_TEXT, JSON_PREFIX_PRE_COLOR, JSON_PREFIX_TEXT, JSON_PREFIX_COLOR, JSON_PREFIX_AFTER_TEXT, JSON_PREFIX_AFTER_COLOR;

	public List<String> INPUT_HELP_MESSAGE;

	// tokens
	public String WON_TOKEN;

	// invitation
	public List<String> INVITE_MESSAGE;


	
	Language(GameBox plugin){
		this.plugin = plugin;
		getLangFile();

		PREFIX = getString("prefix");
		PLAIN_PREFIX = ChatColor.stripColor(PREFIX);
		NAME = getString("name");

		getCommandMessages();
		getInvTitles();
		getButtons();
		getOthers();
		getShop();

		// JSON prefix

		this.JSON_PREFIX_PRE_TEXT = getString("jsonPrefix.preText");
		this.JSON_PREFIX_PRE_COLOR = getString("jsonPrefix.preColor");
		this.JSON_PREFIX_TEXT = getString("jsonPrefix.text");
		this.JSON_PREFIX_COLOR = getString("jsonPrefix.color");
		this.JSON_PREFIX_AFTER_TEXT = getString("jsonPrefix.afterText");
		this.JSON_PREFIX_AFTER_COLOR = getString("jsonPrefix.afterColor");
	}

	private void getOthers() {
		// clickable invite message
		this.INVITATION_PRE_TEXT = getString("others.invitationClickMessage.preText");
		this.INVITATION_PRE_COLOR = getString("others.invitationClickMessage.preColor");
		this.INVITATION_CLICK_TEXT = getString("others.invitationClickMessage.clickText");
		this.INVITATION_CLICK_COLOR = getString("others.invitationClickMessage.clickColor");
		this.INVITATION_HOVER_TEXT = getString("others.invitationClickMessage.hoverText");
		this.INVITATION_HOVER_COLOR = getString("others.invitationClickMessage.hoverColor");
		this.INVITATION_AFTER_TEXT = getString("others.invitationClickMessage.afterText");
		this.INVITATION_AFTER_COLOR = getString("others.invitationClickMessage.afterColor");




		this.INPUT_START_MESSAGE = getString("others.playerInput.openingMessage");
		this.INPUT_TIME_RAN_OUT = getString("others.playerInput.timeRanOut");
		this.INPUT_HELP_MESSAGE = getStringList("others.playerInput.helpMessage");
		this.INVITATION_SUCCESSFUL = getString("others.playerInput.inputSuccessful");
		this.INVITATION_ALREADY_THERE = getString("others.playerInput.sameInvitation");
		this.INVITATION_NOT_VALID_PLAYER_NAME = getString("others.playerInput.notValidPlayerName");
		this.INVITATION_NOT_ONLINE = getString("others.playerInput.notOnline");
		this.INPUT_CLOSED = getString("others.playerInput.inputClosed");
		this.INVITATION_NOT_YOURSELF = getString("others.playerInput.notInviteYourself");

		this.WON_TOKEN = getString("others.wonToken");

		INVITE_MESSAGE = getStringList("others.invitation");
	}

	private void getButtons() {
		this.BUTTON_EXIT = getString("mainButtons.exitButton");
		this.BUTTON_TO_MAIN_MENU = getString("mainButtons.toMainGUIButton");
		this.BUTTON_TO_GAME_MENU = getString("mainButtons.toGameGUIButton");
		this.BUTTON_TOKENS = getString("mainButtons.tokensButton");

		this.BUTTON_FORWARD = getString("mainButtons.forwardButton");
		this.BUTTON_BACK = getString("mainButtons.backwardButton");

		this.BUTTON_MAIN_MENU_INFO = getStringList("mainButtons.infoMainMenu");

		BUTTON_SOUND_ON_NAME = getString("mainButtons.soundToggle.onDisplayName");
		BUTTON_SOUND_OFF_NAME = getString("mainButtons.soundToggle.offDisplayName");
		BUTTON_SOUND_ON_LORE = getStringList("mainButtons.soundToggle.onLore");
		BUTTON_SOUND_OFF_LORE = getStringList("mainButtons.soundToggle.offLore");

		BUTTON_INVITE_BUTTON_NAME = getString("mainButtons.inviteButton.displayName");
		BUTTON_INVITE_BUTTON_LORE = getStringList("mainButtons.inviteButton.lore");

		BUTTON_INVITE_SKULL_NAME = getString("mainButtons.invitationSkull.displayName");
		BUTTON_INVITE_SKULL_LORE = getStringList("mainButtons.invitationSkull.lore");
	}

	private void getInvTitles() {
		// main GUI
		this.TITLE_MAIN_GUI = getString("inventoryTitles.mainGUI");

		this.TITLE_GAME_GUI = getString("inventoryTitles.gameGUIs");
		this.TITLE_NO_PERM = getString("inventoryTitles.noPermMessage");
		this.TITLE_NOT_ENOUGH_MONEY = getString("inventoryTitles.notEnoughMoney");
		this.TITLE_NOT_ENOUGH_TOKEN = getString("inventoryTitles.notEnoughTokens");
		this.TITLE_ALREADY_IN_ANOTHER_GAME = getString("inventoryTitles.alreadyInAnotherGame");
		this.TITLE_OTHER_PLAYER_NOT_ENOUGH_MONEY = getString("inventoryTitles.otherPlayerNotEnoughMoney");

	}
	
	private void getCommandMessages() {

		this.CMD_NO_PERM = getString("commandMessages.noPermission");
		this.CMD_DISABLED_WORLD = getString("commandMessages.inDisabledWorld");
		this.CMD_ONLY_PLAYER = getString("commandMessages.onlyAsPlayer");
		this.CMD_RELOADED = getString("commandMessages.pluginReloaded");


		this.CMD_TOKEN = getString("commandMessages.tokenInfo");
		
		
		this.CMD_HELP = getStringList("commandMessages.help");
		this.CMD_WRONG_USAGE = getStringList("commandMessages.wrongUsage");

		this.CMD_INFO_HEADER = getStringList("commandMessages.info.header");
		this.CMD_INFO_PER_GAME = getStringList("commandMessages.info.perGame");
		this.CMD_INFO_FOOTER = getStringList("commandMessages.info.footer");
	}

	private void getShop(){
		this.SHOP_TITLE_MAIN_SHOP = getString("shop.mainShop");
		this.SHOP_TITLE_PAGE_SHOP = getString("shop.pageShop");

		this.SHOP_TITLE_INVENTORY_FULL = getString("shop.inventoryIsFull");
		this.SHOP_TITLE_REQUIREMENT_NOT_FULFILLED = getString("shop.requirementNotFulfilled");
		this.SHOP_TITLE_BOUGHT_SUCCESSFULLY = getString("shop.boughtSuccessful");
		this.SHOP_TITLE_NOT_ENOUGH_TOKEN = getString("shop.notEnoughTokens");
		this.SHOP_TITLE_NOT_ENOUGH_MONEY = getString("shop.notEnoughMoney");
		this.SHOP_IS_CLOSED = getString("shop.shopIsClosed");

		this.SHOP_FREE = getString("shop.freeItem");
		this.SHOP_MONEY = getString("shop.moneyItem");
		this.SHOP_TOKEN = getString("shop.tokenItem");
	}

	/**
	 * Load list messages from the language file
	 *
	 * If the requested path is not valid for the chosen
	 * language file the corresponding list from the default
	 * file is returned.
	 * ChatColor is translated here.
	 * @param path path to the message
	 * @return message
	 */
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

	/**
	 * Get a message from the language file
	 *
	 * If the requested path is not valid for the
	 * configured language file the corresponding
	 * message from the default file is returned.
	 * ChatColor is translated when reading the message.
	 * @param path path to the message
	 * @return message
	 */
	private String getString(String path) {
		if(!langFile.isString(path)){
			return ChatColor.translateAlternateColorCodes('&',defaultLang.getString(path));
		}
		return ChatColor.translateAlternateColorCodes('&',langFile.getString(path));
	}
	
	private void getLangFile() {

		/*
		 * The default file will always contain the up to date english messages
		 *
		 * Messages from this file will be used if there are some missing
		 * in the given language file. The missing keys will be listed in the console.
		 */


		try {
			String fileName = "language/lang_en.yml";
			this.defaultLang =  YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(fileName), "UTF-8"));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
		File defaultEn = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "lang_en.yml");
		if(!defaultEn.exists()){
			defaultEn.getParentFile().mkdirs();
			plugin.saveResource("language" + File.separatorChar + "lang_en.yml", false);
		}
		File defaultEs = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "lang_es.yml");
		if(!defaultEs.exists()){
			defaultEs.getParentFile().mkdirs();
			plugin.saveResource("language" + File.separatorChar + "lang_es.yml", false);
		}
		File defaultDe = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "lang_de.yml");
		if(!defaultDe.exists()){
			plugin.saveResource("language" + File.separatorChar + "lang_de.yml", false);
		}
		File defaultZh_cn = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "lang_zh-cn.yml");
		if(!defaultZh_cn.exists()){
			plugin.saveResource("language" + File.separatorChar + "lang_zh-cn.yml", false);
		}
		
		if(!plugin.getConfig().isSet("langFile")){
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Language file is missing in the config!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " Add the following to your config:"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " langFile: 'default.yml'"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Using default language file"));
			this.langFile = defaultLang;
		} else {
			if(!plugin.getConfig().isString("langFile")){
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Language file is invalid (no String)!"));
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Using default language file"));
				this.langFile = defaultLang;
			} else {
				String fileName = plugin.getConfig().getString("langFile");
				if(fileName.equalsIgnoreCase("default") || fileName.equalsIgnoreCase("default.yml")){
					this.langFile = defaultLang;
					return;
				}
				File languageFile = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + plugin.getConfig().getString("langFile"));
				if(!languageFile.exists()){
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Language file not found!"));
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Using default language file"));
					this.langFile = defaultLang;
				} else {
					try {
						this.langFile = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(languageFile), "UTF-8"));
					} catch (UnsupportedEncodingException | FileNotFoundException e) {
						e.printStackTrace();
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Error while loading language file!"));
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Using default language file"));
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
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Please get an up to date language file"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Or add the listed paths to your file"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*"));
		}
		return;
		
	}

}

