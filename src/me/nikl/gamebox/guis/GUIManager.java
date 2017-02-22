package me.nikl.gamebox.guis;

import me.nikl.gamebox.ClickAction;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Language;
import me.nikl.gamebox.Permissions;
import me.nikl.gamebox.guis.button.AButton;
import me.nikl.gamebox.guis.gui.AGui;
import me.nikl.gamebox.guis.gui.MainGui;
import me.nikl.gamebox.guis.gui.game.GameGui;
import me.nikl.gamebox.guis.gui.game.GameGuiPage;
import me.nikl.gamebox.guis.gui.game.StartMultiplayerGamePage;
import me.nikl.gamebox.guis.gui.game.TopListPage;
import me.nikl.gamebox.nms.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created by niklas on 2/4/17.
 *
 * each game gets such a manager
 * listen for events called in the game's guis and pages
 * (not mainGUI and not game)
 */
public class GUIManager implements Listener {
	public static final String TOP_LIST_KEY_ADDON = "topList";
	private GameBox plugin;
	private Map<String, Map<String, GameGui>> gameGuis;
	private NMSUtil nms;
	private Language lang;
	
	private MainGui mainGui;

	private int titleMessageSeconds = 3;

	public static final String MAIN_GAME_GUI = "main", PLAY_GAME_GUI = "play";


	public GUIManager(GameBox plugin){
		this.plugin = plugin;
		this.nms = plugin.getNMS();
		this.lang = plugin.lang;
		this.gameGuis = new HashMap<>();

		this.mainGui = new MainGui(plugin, this);

	}
	
	
	public void onInvClick(InventoryClickEvent event) {
		// get the uuid and check where the click should go
		UUID uuid = event.getWhoClicked().getUniqueId();
		boolean topInv = event.getSlot() == event.getRawSlot();
		if (mainGui.isInGui(uuid)) {
			event.setCancelled(true);
			if(topInv)mainGui.onInvClick(event);
			if(!topInv)mainGui.onBottomInvClick(event);
			return;
		}
		for (String gameID : gameGuis.keySet()) {
			Map<String, GameGui> guis = gameGuis.get(gameID);
			for (GameGui gui : guis.values()) {
				if (gui.isInGui(uuid)) {
					event.setCancelled(true);
					if(topInv)gui.onInvClick(event);
					if(!topInv)gui.onBottomInvClick(event);
					return;
				}
			}
		}
		if(GameBox.debug)Bukkit.getConsoleSender().sendMessage("Not in a GameBox GUI");
	}



	public void onInvClose(InventoryCloseEvent event) {
		// get the uuid and check where the event should go
		if(!(event.getPlayer() instanceof Player)) return;
		UUID uuid = event.getPlayer().getUniqueId();
		if(mainGui.isInGui(uuid)){
			mainGui.onInvClose(event);
			plugin.getPluginManager().restoreInventory((Player)event.getPlayer());
			((Player) event.getPlayer()).updateInventory();
			return;
		}
		for(String gameID : gameGuis.keySet()){
			Map<String, GameGui> guis = gameGuis.get(gameID);
			for(GameGui gui : guis.values()){
				if(gui.isInGui(uuid)){
					gui.onInvClose(event);
					plugin.getPluginManager().restoreInventory((Player)event.getPlayer());
					((Player) event.getPlayer()).updateInventory();
					return;
				}
			}
		}
		if(GameBox.debug)Bukkit.getConsoleSender().sendMessage("Not in a GameBox GUI");
	}
	
	public boolean isInGUI(UUID uuid){
		if(isInMainGUI(uuid)) return true;
		if(isInGameGUI(uuid)) return true;
		return false;
	}

	public boolean isInMainGUI(UUID uuid){
		if(mainGui.isInGui(uuid)){
			return true;
		}
		return false;
	}

	public boolean isInGameGUI(UUID uuid){
		for(String gameID : gameGuis.keySet()){
			if(isInGameGUI(uuid,gameID)) return true;
		}
		return false;
	}

	public boolean isInGameGUI(UUID uuid, String gameID){
		Map<String, GameGui> guis = gameGuis.get(gameID);
		for(GameGui gui : guis.values()){
			if(gui.isInGui(uuid)){
				return true;
			}
		}
		return false;
	}
	
	public boolean openGameGui(Player whoClicked, String... args) {
		if(!plugin.getPluginManager().hasSavedContents(whoClicked.getUniqueId())){
			plugin.getPluginManager().saveInventory(whoClicked);
		}
		if(args.length == 2) {
			String gameID = args[0], key = args[1];
			if (whoClicked.hasPermission(Permissions.ADMIN.getPermission()) || whoClicked.hasPermission(Permissions.OPEN_ALL_GAME_GUI.getPermission())|| whoClicked.hasPermission(Permissions.OPEN_GAME_GUI.getPermission(gameID))) {
				GameBox.openingNewGUI = true;
				boolean opened = gameGuis.get(gameID).get(key).open(whoClicked);
				GameBox.openingNewGUI = false;
				if(opened){
					if(gameGuis.get(gameID).get(key) instanceof GameGuiPage){
						nms.updateInventoryTitle(whoClicked, ((GameGuiPage)gameGuis.get(gameID).get(key)).getTitle().replace("%game%", plugin.getPluginManager().getGame(gameID).getName()).replace("%player%", whoClicked.getName()));
					} else {
						nms.updateInventoryTitle(whoClicked, lang.TITLE_GAME_GUI.replace("%game%", plugin.getPluginManager().getGame(gameID).getName()).replace("%player%", whoClicked.getName()));
					}
				} else {
					plugin.getPluginManager().restoreInventory(whoClicked);
				}
				return opened;
			} else {
				if(mainGui.isInGui(whoClicked.getUniqueId())){

					String currentTitle = plugin.lang.TITLE_MAIN_GUI.replace("%player%", whoClicked.getName());
					plugin.getPluginManager().startTitleTimer(whoClicked, currentTitle, titleMessageSeconds);
					plugin.getNMS().updateInventoryTitle(whoClicked, plugin.lang.TITLE_NO_PERM);
				}
				whoClicked.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);
				plugin.getPluginManager().restoreInventory(whoClicked);
				return false;
			}
		} else {
			Bukkit.getConsoleSender().sendMessage("unknown number of arguments in GUIManager.openGameGui");
			plugin.getPluginManager().restoreInventory(whoClicked);
			return false;
		}
	}
	
	public boolean openMainGui(Player whoClicked, String... args) {
		if(!plugin.getPluginManager().hasSavedContents(whoClicked.getUniqueId())){
			plugin.getPluginManager().saveInventory(whoClicked);
		}
		if(args == null || args.length==0){
			if(whoClicked.hasPermission(Permissions.CMD_MAIN.getPermission())){
				GameBox.openingNewGUI = true;
				mainGui.open(whoClicked);
				GameBox.openingNewGUI = false;
				return true;
			}
			plugin.getPluginManager().restoreInventory(whoClicked);
			whoClicked.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);
			return false;
		}
		Bukkit.getLogger().log(Level.WARNING, "in openMainGui not supported arg found: " + args.toString());
		return false;
	}

	public void registerGameGUI(String gameID, String arg, GameGui gui){
		gameGuis.computeIfAbsent(gameID, k -> new HashMap<>());

		gameGuis.get(gameID).put(arg, gui);
		GameBox.debug("registered gamegui: " + gameID + ", " + arg);
	}

	public void registerGameGUI(String gameID, String arg, GameGui gui, ItemStack button){
		registerGameGUI( gameID,  arg,  gui);
		AButton gameButton = new AButton(button.getData(), 1);
		gameButton.setItemMeta(button.getItemMeta());
		gameButton.setAction(ClickAction.OPEN_GAME_GUI);
		gameButton.setArgs(gameID);
		mainGui.setButton(gameButton);
	}

	public void registerGameGUI(String gameID, String arg, GameGui gui, ItemStack button, String... subCommand){
		registerGameGUI(gameID, arg, gui, button);
		if(subCommand != null)plugin.getMainCommand().registerSubCommands(gameID, subCommand);
	}

	public void registerTopList(String gameID, String buttonID, TopListPage topListPage){
		gameGuis.computeIfAbsent(gameID, k -> new HashMap<>());

		gameGuis.get(gameID).put(buttonID + TOP_LIST_KEY_ADDON, topListPage);
		GameBox.debug("registered toplist: " + gameID + ", " + buttonID);
	}

	private String color(String message){
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public AGui getCurrentGui(UUID uuid){
		if(mainGui.isInGui(uuid)){
			return mainGui;
		}
		for(String gameID : gameGuis.keySet()){
			if(isInGameGUI(uuid,gameID)){
				for(GameGui gui : gameGuis.get(gameID).values()){
					if(gui.isInGui(uuid)){
						return gui;
					}
				}
			}
		}
		return null;
	}

	public void shutDown() {
	}

	public MainGui getMainGui(){
		return this.mainGui;
	}

	public void removePlayer(UUID uuid){

		for(String gameID : gameGuis.keySet()){
			Map<String, GameGui> guis = gameGuis.get(gameID);
			for(GameGui gui : guis.values()){
				gui.removePlayer(uuid);
			}
		}

		this.mainGui.removePlayer(uuid);

	}
}
