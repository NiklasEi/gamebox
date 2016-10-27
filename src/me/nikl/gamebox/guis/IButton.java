package me.nikl.gamebox.guis;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by niklas on 10/27/16.
 *
 * button interface
 */
public interface IButton {
	
	void onClick(InventoryClickEvent event, IGui gui);
	
	ItemStack getItem();
}
