package me.nikl.gamebox.guis.standard;

import me.nikl.gamebox.Language;
import me.nikl.gamebox.games.IGameManager;
import me.nikl.gamebox.guis.AButton;
import me.nikl.gamebox.guis.IGui;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by niklas on 10/28/16.
 *
 * button to the game gui
 */
public class ToPluginGUI extends AButton{
	
	private IGameManager gManager;
	
	public ToPluginGUI(IGameManager gManager, Language lang){
		this.gManager = gManager;
		
		this.item = new ItemStack(Material.BIRCH_DOOR_ITEM, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', lang.BUTTON_GAME_GUI));
		item.setItemMeta(meta);
	
	}
		
	@Override
	public void onClick(InventoryClickEvent event, IGui gui) {
		//toDo
	}
}
