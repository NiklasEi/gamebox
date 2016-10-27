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
	
	void openGui(UUID player);
	
	void closeGui(UUID player);
	
	void openGui(Player player);
	
	void closeGui(Player player);
	
	void openParentGUI();
	
	void openChildGUI();
	
}
