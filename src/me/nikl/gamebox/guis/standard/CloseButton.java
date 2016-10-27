package me.nikl.gamebox.guis.standard;

import me.nikl.gamebox.Language;
import me.nikl.gamebox.guis.AButton;
import me.nikl.gamebox.guis.IGui;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by niklas on 10/27/16.
 *
 * CloseButton
 */
public class CloseButton extends AButton {
	
	public CloseButton(Language lang){
		item = new ItemStack(Material.BARRIER, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', lang.BUTTON_EXIT));
		item.setItemMeta(meta);
	}
	
	@Override
	public void onClick(InventoryClickEvent event, IGui gui) {
		// toDo be sure that the player is removed from gui!
		((Player) event.getWhoClicked()).closeInventory();
	}
}
