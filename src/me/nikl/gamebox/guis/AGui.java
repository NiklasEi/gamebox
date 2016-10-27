package me.nikl.gamebox.guis;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.Set;
import java.util.UUID;

/**
 * Created by niklas on 10/27/16.
 *
 * abstract AGui class
 */
public abstract class AGui implements IGui{
	
	// parent and child AGui
	private AGui parentAGui;
	private AGui childAGui;
	
	private IButton[] grid;
	
	
	// inventory
	private Inventory inv;
	
	// set of all players in the AGui
	private Set<UUID> players;
	
	public AGui(AGui parentAGui, AGui childAGui, InventoryType invType){
		this.parentAGui = parentAGui;
		this.childAGui = childAGui;
		
		this.inv = Bukkit.createInventory(null, invType, "");
	}
	
	@Override
	public void openGui(UUID player){
		Player pPlayer = Bukkit.getPlayer(player);
		if(pPlayer != null) openGui(pPlayer);
	}
	
	@Override
	public void openGui(Player player){
		players.add(player.getUniqueId());
		player.openInventory(inv);
	}
}
