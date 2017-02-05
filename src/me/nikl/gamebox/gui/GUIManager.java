package me.nikl.gamebox.gui;

import me.nikl.gamebox.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.UUID;

/**
 * Created by niklas on 2/4/17.
 *
 * each game gets such a manager
 * listen for events called in the game's gui and pages
 * (not mainGUI and not game)
 */
public class GUIManager  implements Listener {
	private Main plugin;
	
	public GUIManager(Main plugin){
		this.plugin = plugin;
		
		
	}
	
	
	public void onInvClick(InventoryClickEvent event) {
		if(event.getClickedInventory() == null || event.getWhoClicked() == null){
			return;
		}
		if(!(event.getWhoClicked() instanceof Player)){
			return;
		}
		Player player = (Player) event.getWhoClicked();
		UUID uuid = player.getUniqueId();
		//ToDo
	}
	
	public void onInvClose(InventoryCloseEvent event) {
		// ToDo
	}
}
