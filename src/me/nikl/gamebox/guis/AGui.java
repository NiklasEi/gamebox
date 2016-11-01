package me.nikl.gamebox.guis;

import me.nikl.gamebox.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by niklas on 10/27/16.
 *
 * abstract AGui class
 */
public abstract class AGui implements IGui{
	
	// parent and child AGui
	protected IGui parentGui;
	protected IGui childGui;
	
	protected IButton[] grid;
	
	
	// inventory
	protected Inventory inv;
	
	// set of all players in the AGui
	protected Set<UUID> players;
	
	public AGui(IGui parentAGui, IGui childAGui, InventoryType invType){
		this.parentGui = parentAGui;
		this.childGui = childAGui;
		this.players = new HashSet<>();
		
		this.grid = new IButton[invType.getDefaultSize()];
		
		this.inv = Bukkit.createInventory(null, invType, "");
	}
	
	public AGui(int numSlots){
		this.parentGui = null;
		this.childGui = null;
		this.players = new HashSet<>();
		
		this.grid = new IButton[numSlots];
		this.inv = Bukkit.createInventory(null, numSlots, "");
	}
	
	@Override
	public boolean openGui(UUID player, IGui from){
		Player pPlayer = Bukkit.getPlayer(player);
		if(pPlayer != null) return openGui(pPlayer, from);
		return false;
	}
	
	@Override
	public boolean openGui(Player player, IGui from){
		if(from != null) from.removePlayer(player);
		players.add(player.getUniqueId());
		if(Main.debug) Bukkit.getConsoleSender().sendMessage("added to " + this.toString());
		player.openInventory(inv);
		return true;
	}
	
	
	@Override
	public void removePlayer(UUID player){
		players.remove(player);
	}
	
	@Override
	public void removePlayer(Player player){
		players.remove(player.getUniqueId());
	}
	
	@Override
	public boolean inGUI(UUID uuid){
		return players.contains(uuid);
	}
}
