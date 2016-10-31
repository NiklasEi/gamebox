package me.nikl.gamebox.guis.headgui;

import me.nikl.gamebox.guis.AButton;
import me.nikl.gamebox.guis.IGui;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by niklas on 10/30/16.
 *
 *
 */
public class LastPageButton extends AButton{
	private int page;
	
	public LastPageButton(int page){
		this.page = page;
		
		this.item = new ItemStack(Material.STAINED_GLASS_PANE, 1);
		item.setDurability((short) 13);
		ItemMeta meta = item.getItemMeta();
		// ToDo: somehow enable customisation here. Get the lang file through constructor?
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aGo to page " + (page-1)));
		item.setItemMeta(meta);
	}
	
	@Override
	public void onClick(InventoryClickEvent event, IGui gui) {
		if(!(gui instanceof IMultiGUI)) return;
		((IMultiGUI) gui).lastPage(event.getWhoClicked().getUniqueId());
	}
}
