package me.nikl.gamebox.guis;

import me.nikl.gamebox.Language;
import me.nikl.gamebox.Main;
import me.nikl.gamebox.Permissions;
import me.nikl.gamebox.guis.button.AButton;
import me.nikl.gamebox.guis.gui.AGui;
import me.nikl.gamebox.guis.gui.MainGui;
import me.nikl.gamebox.nms.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
	
	
	public GUIManager(Main plugin){
		this.plugin = plugin;
		this.nms = plugin.getNMS();
		this.lang = plugin.lang;
		this.gameGuis = new HashMap<>();
	}
	
	
	public void onInvClick(InventoryClickEvent event) {
		UUID uuid = event.getWhoClicked().getUniqueId();
		//ToDo
	}
	
	public void onInvClose(InventoryCloseEvent event) {
		// ToDo
	}
	
	public boolean isInGUI(UUID uuid){
		//ToDo
		return false;
	}
	
	public boolean openGameGui(Player whoClicked, String... args) {
		if(whoClicked.hasPermission(Permissions.OPEN_GAME_GUI.getPermission(args[0]))){
			return gameGuis.get(args[0]).get(args[1]).open(whoClicked);
		} else {
			//ToDo no perm message
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
	
	public void addGameGui(String gameID, String ID, AGui aGui){
		gameGuis.putIfAbsent(gameID, new HashMap<>());
		gameGuis.get(gameID).put(ID, aGui);
	}
}
