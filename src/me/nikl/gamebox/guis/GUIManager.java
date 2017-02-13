package me.nikl.gamebox.guis;

import me.nikl.gamebox.ClickAction;
import me.nikl.gamebox.Language;
import me.nikl.gamebox.Main;
import me.nikl.gamebox.Permissions;
import me.nikl.gamebox.guis.button.AButton;
import me.nikl.gamebox.guis.gui.AGui;
import me.nikl.gamebox.guis.gui.MainGui;
import me.nikl.gamebox.nms.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
	private Main plugin;
	private Map<String, Map<String, AGui>> gameGuis;
	private NMSUtil nms;
	private Language lang;
	
	private MainGui mainGui;

	public static final String MAIN_GAME_GUI = "main";
	
	
	public GUIManager(Main plugin){
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
			if(!topInv)Main.debug("lower inventory was clicked   TODO!!!");
			return;
		}
		for (String gameID : gameGuis.keySet()) {
			Map<String, AGui> guis = gameGuis.get(gameID);
			for (AGui gui : guis.values()) {
				if (gui.isInGui(uuid)) {
					event.setCancelled(true);
					if(topInv)gui.onInvClick(event);
					if(!topInv)Main.debug("lower inventory was clicked   TODO!!!");
					return;
				}
			}
		}
		if(!topInv)Main.debug("lower inventory was clicked   TODO!!!");

		if(Main.debug)Bukkit.getConsoleSender().sendMessage("Not in a GameBox GUI");
	}
	
	public void onInvClose(InventoryCloseEvent event) {
		// get the uuid and check where the event should go
		UUID uuid = event.getPlayer().getUniqueId();
		if(mainGui.isInGui(uuid)){
			mainGui.onInvClose(event);
			return;
		}
		for(String gameID : gameGuis.keySet()){
			Map<String, AGui> guis = gameGuis.get(gameID);
			for(AGui gui : guis.values()){
				if(gui.isInGui(uuid)){
					gui.onInvClose(event);
					return;
				}
			}
		}
		if(Main.debug)Bukkit.getConsoleSender().sendMessage("Not in a GameBox GUI");
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
		Map<String, AGui> guis = gameGuis.get(gameID);
		for(AGui gui : guis.values()){
			if(gui.isInGui(uuid)){
				return true;
			}
		}
		return false;
	}
	
	public boolean openGameGui(Player whoClicked, String... args) {
		if(args.length == 2) {
			String gameID = args[0], key = args[1];
			if (whoClicked.hasPermission(Permissions.OPEN_ALL_GAME_GUI.getPermission()) || whoClicked.hasPermission(Permissions.OPEN_GAME_GUI.getPermission(gameID))) {
				return gameGuis.get(gameID).get(key).open(whoClicked);
			} else {
				whoClicked.sendMessage(color(lang.CMD_NO_PERM));
				return false;
			}
		} else {
			Bukkit.getConsoleSender().sendMessage("unknown number of arguments in GUIManager.openGameGui");
			return false;
		}
	}
	
	public boolean openMainGui(Player whoClicked, String... args) {
		if(args == null || args.length==0){
			if(whoClicked.hasPermission(Permissions.CMD_MAIN.getPermission())){
				mainGui.open(whoClicked);
				nms.updateInventoryTitle(whoClicked, lang.TITLE_MAIN_GUI);
				return true;
			}
			//ToDo send message to player?
			return false;
		}
		Bukkit.getLogger().log(Level.WARNING, "in openMainGui not supported arg found: " + args.toString());
		return false;
	}

	public void registerGameGUI(String gameID, String arg, AGui gui){
		gameGuis.computeIfAbsent(gameID, k -> new HashMap<>());

		gameGuis.get(gameID).put(arg, gui);
		Main.debug("registered gamegui: " + gameID + ", " + arg);
	}

	public void registerGameGUI(String gameID, String arg, AGui gui, ItemStack button){
		registerGameGUI( gameID,  arg,  gui);
		AButton gameButton = new AButton(button.getData(), 1);
		gameButton.setAction(ClickAction.OPEN_GAME_GUI);
		gameButton.setArgs(gameID);
		mainGui.setButton(gameButton);
	}

	private String color(String message){
		return ChatColor.translateAlternateColorCodes('&', message);
	}
}
