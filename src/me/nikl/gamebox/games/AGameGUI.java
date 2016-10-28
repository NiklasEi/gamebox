package me.nikl.gamebox.games;

import me.nikl.gamebox.Language;
import me.nikl.gamebox.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by niklas on 10/17/16.
 *
 * AGameGUI for every game
 */
public abstract class AGameGUI implements IGameGUI {
	protected Main plugin;
	protected Language lang;
	protected ItemStack closeButton, backButton, forwardButton, toMainGUI, toGameGUI;
	
	public AGameGUI(Main plugin) {
		this.plugin = plugin;
		this.lang = plugin.lang;
		
		this.backButton = new ItemStack(Material.STAINED_GLASS_PANE, 1);
		backButton.setDurability((short) 14);
		ItemMeta meta = backButton.getItemMeta();
		meta.setDisplayName(chatColor(lang.BUTTON_BACK));
		backButton.setItemMeta(meta);
		
		this.closeButton = new ItemStack(Material.BARRIER, 1);
		meta = closeButton.getItemMeta();
		meta.setDisplayName(chatColor(lang.BUTTON_EXIT));
		closeButton.setItemMeta(meta);
		
		
		this.forwardButton = new ItemStack(Material.STAINED_GLASS_PANE, 1);
		forwardButton.setDurability((short) 13);
		meta = forwardButton.getItemMeta();
		meta.setDisplayName(chatColor(lang.BUTTON_FORWARD));
		forwardButton.setItemMeta(meta);
		
		this.toMainGUI = new ItemStack(Material.SPRUCE_DOOR_ITEM, 1);
		meta = toMainGUI.getItemMeta();
		meta.setDisplayName(chatColor(lang.BUTTON_MAIN_GUI));
		toMainGUI.setItemMeta(meta);
		
		this.toGameGUI = new ItemStack(Material.BIRCH_DOOR_ITEM, 1);
		meta = toGameGUI.getItemMeta();
		meta.setDisplayName(chatColor(lang.BUTTON_GAME_GUI));
		toGameGUI.setItemMeta(meta);
	}
	
	public String chatColor(String message){
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
}