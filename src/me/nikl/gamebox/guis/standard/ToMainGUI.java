package me.nikl.gamebox.guis.standard;

import me.nikl.gamebox.Language;
import me.nikl.gamebox.PluginManager;
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
 *
 *
 */
public class ToMainGUI extends AButton{
	private PluginManager pluginManager;
	
	public ToMainGUI(Language lang, PluginManager pManager){
		this.item = new ItemStack(Material.SPRUCE_DOOR_ITEM, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', lang.BUTTON_MAIN_GUI));
		item.setItemMeta(meta);
		
		this.pluginManager = pManager;
	}
	
	
	@Override
	public void onClick(InventoryClickEvent event, IGui gui) {
		pluginManager.openGUI((Player)event.getWhoClicked());
	}
}
