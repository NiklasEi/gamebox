package me.nikl.gamebox;

import me.nikl.gamebox.util.LanguageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.List;


/**
 * Created by niklas on 10/17/16.
 *
 * GameBoxLanguage class
 * Get all messages on enable
 * save not saved default lang files
 */
public class GameBoxLanguage extends Language{

	// commands
	public String CMD_NO_PERM, CMD_ONLY_PLAYER, CMD_RELOADED, CMD_DISABLED_WORLD, CMD_TOKEN, RELOAD_SUCCESS, RELOAD_FAIL;
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


	
	GameBoxLanguage(GameBox plugin){
		super(plugin, LanguageUtil.Namespace.GAMEBOX);

		PREFIX = LanguageUtil.getString("gamebox", "prefix");
		PLAIN_PREFIX = ChatColor.stripColor(PREFIX);
		NAME = LanguageUtil.getString("gamebox", "name");

		getCommandMessages();
		getInvTitles();
		getButtons();
		getOthers();
		getShop();

		// JSON prefix

		this.JSON_PREFIX_PRE_TEXT = LanguageUtil.getString("gamebox", "jsonPrefix.preText");
		this.JSON_PREFIX_PRE_COLOR = LanguageUtil.getString("gamebox", "jsonPrefix.preColor");
		this.JSON_PREFIX_TEXT = LanguageUtil.getString("gamebox", "jsonPrefix.text");
		this.JSON_PREFIX_COLOR = LanguageUtil.getString("gamebox", "jsonPrefix.color");
		this.JSON_PREFIX_AFTER_TEXT = LanguageUtil.getString("gamebox", "jsonPrefix.afterText");
		this.JSON_PREFIX_AFTER_COLOR = LanguageUtil.getString("gamebox", "jsonPrefix.afterColor");
	}

	private void getOthers() {
		// clickable invite message
		this.INVITATION_PRE_TEXT = LanguageUtil.getString("gamebox", "others.invitationClickMessage.preText");
		this.INVITATION_PRE_COLOR = LanguageUtil.getString("gamebox", "others.invitationClickMessage.preColor");
		this.INVITATION_CLICK_TEXT = LanguageUtil.getString("gamebox", "others.invitationClickMessage.clickText");
		this.INVITATION_CLICK_COLOR = LanguageUtil.getString("gamebox", "others.invitationClickMessage.clickColor");
		this.INVITATION_HOVER_TEXT = LanguageUtil.getString("gamebox", "others.invitationClickMessage.hoverText");
		this.INVITATION_HOVER_COLOR = LanguageUtil.getString("gamebox", "others.invitationClickMessage.hoverColor");
		this.INVITATION_AFTER_TEXT = LanguageUtil.getString("gamebox", "others.invitationClickMessage.afterText");
		this.INVITATION_AFTER_COLOR = LanguageUtil.getString("gamebox", "others.invitationClickMessage.afterColor");




		this.INPUT_START_MESSAGE = LanguageUtil.getString("gamebox", "others.playerInput.openingMessage");
		this.INPUT_TIME_RAN_OUT = LanguageUtil.getString("gamebox", "others.playerInput.timeRanOut");
		this.INPUT_HELP_MESSAGE = LanguageUtil.getStringList("gamebox", "others.playerInput.helpMessage");
		this.INVITATION_SUCCESSFUL = LanguageUtil.getString("gamebox", "others.playerInput.inputSuccessful");
		this.INVITATION_ALREADY_THERE = LanguageUtil.getString("gamebox", "others.playerInput.sameInvitation");
		this.INVITATION_NOT_VALID_PLAYER_NAME = LanguageUtil.getString("gamebox", "others.playerInput.notValidPlayerName");
		this.INVITATION_NOT_ONLINE = LanguageUtil.getString("gamebox", "others.playerInput.notOnline");
		this.INPUT_CLOSED = LanguageUtil.getString("gamebox", "others.playerInput.inputClosed");
		this.INVITATION_NOT_YOURSELF = LanguageUtil.getString("gamebox", "others.playerInput.notInviteYourself");

		this.WON_TOKEN = LanguageUtil.getString("gamebox", "others.wonToken");

		INVITE_MESSAGE = LanguageUtil.getStringList("gamebox", "others.invitation");
	}

	private void getButtons() {
		this.BUTTON_EXIT = LanguageUtil.getString("gamebox", "mainButtons.exitButton");
		this.BUTTON_TO_MAIN_MENU = LanguageUtil.getString("gamebox", "mainButtons.toMainGUIButton");
		this.BUTTON_TO_GAME_MENU = LanguageUtil.getString("gamebox", "mainButtons.toGameGUIButton");
		this.BUTTON_TOKENS = LanguageUtil.getString("gamebox", "mainButtons.tokensButton");

		this.BUTTON_FORWARD = LanguageUtil.getString("gamebox", "mainButtons.forwardButton");
		this.BUTTON_BACK = LanguageUtil.getString("gamebox", "mainButtons.backwardButton");

		this.BUTTON_MAIN_MENU_INFO = LanguageUtil.getStringList("gamebox", "mainButtons.infoMainMenu");

		BUTTON_SOUND_ON_NAME = LanguageUtil.getString("gamebox", "mainButtons.soundToggle.onDisplayName");
		BUTTON_SOUND_OFF_NAME = LanguageUtil.getString("gamebox", "mainButtons.soundToggle.offDisplayName");
		BUTTON_SOUND_ON_LORE = LanguageUtil.getStringList("gamebox", "mainButtons.soundToggle.onLore");
		BUTTON_SOUND_OFF_LORE = LanguageUtil.getStringList("gamebox", "mainButtons.soundToggle.offLore");

		BUTTON_INVITE_BUTTON_NAME = LanguageUtil.getString("gamebox", "mainButtons.inviteButton.displayName");
		BUTTON_INVITE_BUTTON_LORE = LanguageUtil.getStringList("gamebox", "mainButtons.inviteButton.lore");

		BUTTON_INVITE_SKULL_NAME = LanguageUtil.getString("gamebox", "mainButtons.invitationSkull.displayName");
		BUTTON_INVITE_SKULL_LORE = LanguageUtil.getStringList("gamebox", "mainButtons.invitationSkull.lore");
	}

	private void getInvTitles() {
		// main GUI
		this.TITLE_MAIN_GUI = LanguageUtil.getString("gamebox", "inventoryTitles.mainGUI");

		this.TITLE_GAME_GUI = LanguageUtil.getString("gamebox", "inventoryTitles.gameGUIs");
		this.TITLE_NO_PERM = LanguageUtil.getString("gamebox", "inventoryTitles.noPermMessage");
		this.TITLE_NOT_ENOUGH_MONEY = LanguageUtil.getString("gamebox", "inventoryTitles.notEnoughMoney");
		this.TITLE_NOT_ENOUGH_TOKEN = LanguageUtil.getString("gamebox", "inventoryTitles.notEnoughTokens");
		this.TITLE_ALREADY_IN_ANOTHER_GAME = LanguageUtil.getString("gamebox", "inventoryTitles.alreadyInAnotherGame");
		this.TITLE_OTHER_PLAYER_NOT_ENOUGH_MONEY = LanguageUtil.getString("gamebox", "inventoryTitles.otherPlayerNotEnoughMoney");

	}
	
	private void getCommandMessages() {

		this.CMD_NO_PERM = LanguageUtil.getString("gamebox", "commandMessages.noPermission");
		this.CMD_DISABLED_WORLD = LanguageUtil.getString("gamebox", "commandMessages.inDisabledWorld");
		this.CMD_ONLY_PLAYER = LanguageUtil.getString("gamebox", "commandMessages.onlyAsPlayer");
		this.CMD_RELOADED = LanguageUtil.getString("gamebox", "commandMessages.pluginReloaded");

		this.RELOAD_FAIL = LanguageUtil.getString("gamebox", "commandMessages.reload.fail");
		this.RELOAD_SUCCESS = LanguageUtil.getString("gamebox", "commandMessages.reload.success");


		this.CMD_TOKEN = LanguageUtil.getString("gamebox", "commandMessages.tokenInfo");
		
		
		this.CMD_HELP = LanguageUtil.getStringList("gamebox", "commandMessages.help");
		this.CMD_WRONG_USAGE = LanguageUtil.getStringList("gamebox", "commandMessages.wrongUsage");

		this.CMD_INFO_HEADER = LanguageUtil.getStringList("gamebox", "commandMessages.info.header");
		this.CMD_INFO_PER_GAME = LanguageUtil.getStringList("gamebox", "commandMessages.info.perGame");
		this.CMD_INFO_FOOTER = LanguageUtil.getStringList("gamebox", "commandMessages.info.footer");
	}

	private void getShop(){
		this.SHOP_TITLE_MAIN_SHOP = LanguageUtil.getString("gamebox", "shop.mainShop");
		this.SHOP_TITLE_PAGE_SHOP = LanguageUtil.getString("gamebox", "shop.pageShop");

		this.SHOP_TITLE_INVENTORY_FULL = LanguageUtil.getString("gamebox", "shop.inventoryIsFull");
		this.SHOP_TITLE_REQUIREMENT_NOT_FULFILLED = LanguageUtil.getString("gamebox", "shop.requirementNotFulfilled");
		this.SHOP_TITLE_BOUGHT_SUCCESSFULLY = LanguageUtil.getString("gamebox", "shop.boughtSuccessful");
		this.SHOP_TITLE_NOT_ENOUGH_TOKEN = LanguageUtil.getString("gamebox", "shop.notEnoughTokens");
		this.SHOP_TITLE_NOT_ENOUGH_MONEY = LanguageUtil.getString("gamebox", "shop.notEnoughMoney");
		this.SHOP_IS_CLOSED = LanguageUtil.getString("gamebox", "shop.shopIsClosed");

		this.SHOP_FREE = LanguageUtil.getString("gamebox", "shop.freeItem");
		this.SHOP_MONEY = LanguageUtil.getString("gamebox", "shop.moneyItem");
		this.SHOP_TOKEN = LanguageUtil.getString("gamebox", "shop.tokenItem");
	}
}

