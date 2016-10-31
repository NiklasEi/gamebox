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
 * forward button
 */
public class ForwardButton extends AButton{
	
	public ForwardButton(Language lang){
		this.item = new ItemStack(Material.STAINED_GLASS_PANE, 1);
		item.setDurability((short) 13);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', lang.BUTTON_FORWARD));
		item.setItemMeta(meta);
	}
	
	
	
	@Override
	public void onClick(InventoryClickEvent event, IGui gui) {
		gui.openChildGUI((Player)event.getWhoClicked(), gui);
	}
}
