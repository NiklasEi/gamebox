package me.nikl.gamebox;

import org.bukkit.ChatColor;

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
	public String CMD_NO_PERM, CMD_ONLY_PLAYER, CMD_RELOADED, CMD_DISABLED_WORLD, CMD_TOKEN_INFO
			, CMD_TOOK_TOKEN, CMD_NOT_ENOUGH_TOKEN, CMD_GAVE_TOKEN, CMD_SET_TOKEN, RELOAD_SUCCESS, RELOAD_FAIL;
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
		super(plugin, GameBox.MODULE_GAMEBOX);
	}

	@Override
	protected void loadMessages() {
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

		this.RELOAD_FAIL = getString("commandMessages.reload.fail");
		this.RELOAD_SUCCESS = getString("commandMessages.reload.success");


		this.CMD_TOKEN_INFO = getString("commandMessages.tokenInfo");
		this.CMD_TOOK_TOKEN = getString("commandMessages.tookToken");
		this.CMD_GAVE_TOKEN = getString("commandMessages.gaveToken");
		this.CMD_SET_TOKEN = getString("commandMessages.setToken");
		this.CMD_NOT_ENOUGH_TOKEN = getString("commandMessages.notEnoughToken");
		
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
}

