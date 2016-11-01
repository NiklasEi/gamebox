package me.nikl.gamebox.guis;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

/**
 * Created by niklas on 10/27/16.
 *
 * Interface for GUIs
 */
public interface IGui {
	
	void onClick(InventoryClickEvent event);
	
	boolean openGui(UUID player, IGui from);
	
	void removePlayer(UUID player);
	
	boolean openGui(Player player, IGui from);
	
	void removePlayer(Player player);
	
	boolean openParentGUI(Player player, IGui from);
	
	boolean openChildGUI(Player player, IGui from);
	
	boolean inGUI(UUID uuid);
	
}
