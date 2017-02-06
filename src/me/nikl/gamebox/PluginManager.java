package me.nikl.gamebox;

import me.nikl.gamebox.game.IGameManager;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.nms.NMSUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by niklas on 10/17/16.
 *
 * Manage the main GUI and create the GameManagers / register all games
 * Clicks are managed here
 * Check what GUI is open for the player and then pass the click event on
 */
public class PluginManager implements Listener{
	
	// Main instance
	private Main plugin;
	
	// Language
	private Language lang;
	
	// plugin configuration
	private FileConfiguration config;
	
	private NMSUtil nms;
	
	private Map<String, GUIManager> gameGUIs;
	private Map<String, GUIManager> pluginGUIs;
	
	private GUIManager guiManager;
	
	public PluginManager(Main plugin){
		this.plugin = plugin;
		this.gameGUIs = new HashMap<>();
		this.pluginGUIs = new HashMap<>();
		this.lang = plugin.lang;
		this.nms = plugin.getNMS();
		this.config = plugin.getConfig();
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	
	
	@EventHandler
	public void onInvClick(InventoryClickEvent event) {
		if(event.getClickedInventory() == null || event.getWhoClicked() == null){
			return;
		}
		if(!(event.getWhoClicked() instanceof Player)){
			return;
		}
		UUID uuid = event.getWhoClicked().getUniqueId();
		//ToDo
	}
	
	@EventHandler
	public void onInvClose(InventoryCloseEvent event) {
		// ToDo
	}
	
	public void shutDown() {
		// ToDo
	}
	
	public Main getPlugin() {
		return this.plugin;
	}
	
	private String chatColor(String message){
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	
	public boolean openGameGui(Player whoClicked, String... args) {
		//ToDo
		return false;
	}
	
	public void registerGame(IGameManager gameManager, String gameID){
		
	}
}
