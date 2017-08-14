package me.nikl.gamebox.nms;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by niklas on 10/17/16.
 *
 * interface for nms utils
 */
public interface NMSUtil {
	
	void updateInventoryTitle(Player player, String newTitle);
	
	void sendTitle(Player player, String title, String subTitle);
	
	void sendActionbar(Player p, String message);
	
	void sendListHeader(Player player, String header);
	
	void sendListFooter(Player player, String footer);
	
	ItemStack removeGlow(ItemStack item);
	
	ItemStack addGlow(ItemStack item);
}
