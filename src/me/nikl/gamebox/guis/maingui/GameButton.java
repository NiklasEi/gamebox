package me.nikl.gamebox.guis.maingui;

import me.nikl.gamebox.EnumGames;
import me.nikl.gamebox.Main;
import me.nikl.gamebox.PluginManager;
import me.nikl.gamebox.games.IGameManager;
import me.nikl.gamebox.guis.AButton;
import me.nikl.gamebox.guis.IGui;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Created by niklas on 10/29/16.
 *
 * button class
 */
public class GameButton extends AButton{
	private IGameManager gameManager;
	private EnumGames eGame;
	private PluginManager pManager;
	
	// clickable?
	private boolean enabled;
	
	public GameButton(PluginManager pManager, EnumGames eGame, IGameManager gameManager, FileConfiguration config){
		this.gameManager = gameManager;
		this.pManager = pManager;
		this.eGame = eGame;
		this.enabled = true;
		
		if(!loadItem(config)){
			this.enabled = false;
			this.item = new ItemStack(Material.BARRIER, 1);
			ArrayList<String> lore = new ArrayList<>();
			lore.add(ChatColor.translateAlternateColorCodes('&', "&4Not configured for this game jet"));
			ItemMeta meta = item.getItemMeta();
			meta.setLore(lore);
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', eGame.getName()));
			item.setItemMeta(meta);
		}
	}
	
	private boolean loadItem(FileConfiguration config) {
		Material mat = null;
		int data = 0;
		
		String value = config.getString("games." + eGame.toString() + ".symbol");
		if(value == null) return false;
		String[] obj = value.split(":");
		
		
		if (obj.length == 2) {
			try {
				mat = Material.matchMaterial(obj[0]);
			} catch (Exception e) {
				
			}
			
			try {
				data = Integer.valueOf(obj[1]);
			} catch (NumberFormatException e) {
				
			}
		} else {
			try {
				mat = Material.matchMaterial(value);
			} catch (Exception e) {
				
			}
		}
		if(mat == null){
			return false;
		}
		ArrayList<String> lore = new ArrayList<>();
		lore.add(ChatColor.translateAlternateColorCodes('&', ""));
		lore.add(ChatColor.translateAlternateColorCodes('&', "&aOpen Game GUI"));
		this.item = new ItemStack(mat);
		if(obj.length == 2) item.setDurability((short)data);
		ItemMeta meta = item.getItemMeta();
		meta.setLore(lore);
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', eGame.getName()));
		item.setItemMeta(meta);
		return true;
	}
	
	@Override
	public void onClick(InventoryClickEvent event, IGui gui) {
		if(!enabled) return;
		if(Main.debug) event.getWhoClicked().sendMessage("clicked: " + eGame.getName());
		if(gameManager.openGameGUI((Player)event.getWhoClicked())){
			pManager.removeFromGUI(event.getWhoClicked().getUniqueId());
		}
	}
	
	
}
