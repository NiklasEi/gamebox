package me.nikl.gamebox.guis.gui;

import me.nikl.gamebox.ClickAction;
import me.nikl.gamebox.Main;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created by niklas on 2/5/17.
 *
 *
 */
public abstract class AGui {
	Inventory inventory;
	private GUIManager guiManager;
	private Set<UUID> inGui;
	private Main plugin;
	
	public AGui(Main plugin, GUIManager guiManager){
		this.plugin = plugin;
		this.guiManager = guiManager;
		inGui = new HashSet<>();
	}
	
	public boolean open(Player player){
		player.openInventory(inventory);
		inGui.add(player.getUniqueId());
		return true;
	}
	
	public boolean action(InventoryClickEvent event, ClickAction action, String[] args){
		
		if(Main.debug) Bukkit.getConsoleSender().sendMessage("action called: " + action.toString() + "with the args: " + (args == null?"": args.toString()));
		switch (action){
			case OPEN_GAME_GUI:
				return guiManager.openGameGui((Player)event.getWhoClicked(), args[0], "main");
			
			case START_GAME:
				return false;
			
			case OPEN_MAIN_GUI:
				return guiManager.openMainGui((Player)event.getWhoClicked());
				
			case NOTHING:
				return true;
				
			default:
				Bukkit.getLogger().log(Level.WARNING, "not valid action called in gui: " + action.toString());
				return false;
		}
	}
}
