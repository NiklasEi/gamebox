package me.nikl.gamebox.games.gemcrush.gems;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Created by niklas on 10/23/16.
 *
 */
public class Bomb extends Gem {
	private boolean isExploding;
	
	public Bomb(){
		super(Material.TNT, "Bomb");
		ArrayList<String> lore = new ArrayList<>();
		lore.add(ChatColor.translateAlternateColorCodes('&',"&2Matchable with any gems"));
		lore.add(ChatColor.translateAlternateColorCodes('&',"&4Caution: explosive!"));
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&4Bomb"));
		meta.setLore(lore);
		item.setItemMeta(meta);
		isExploding = false;
	}
	
	@Override
	public void onBreak() {
		
	}
	
	public String getName(){
		return "Bomb";
	}
	
	public boolean isExploding(){
		return isExploding;
	}
	
	public void setExploding(boolean isExploding){
		this.isExploding = isExploding;
	}
}
