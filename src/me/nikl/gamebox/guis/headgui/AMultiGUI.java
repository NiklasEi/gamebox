package me.nikl.gamebox.guis.headgui;

import me.nikl.gamebox.guis.AGui;
import me.nikl.gamebox.guis.IButton;
import me.nikl.gamebox.guis.IGui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by niklas on 10/30/16.
 *
 * abstract gui class with pages
 */
public abstract class AMultiGUI implements IMultiGUI{
	
	
	// parent and child AGui
	private AGui parentAGui;
	private AGui childAGui;
	
	protected IButton[] grid;
	
	
	// inventory
	protected Inventory[] inv;
	
	// map of all players with their current page
	private Map<UUID, Integer> players;
	
	public AMultiGUI(AGui parentAGui, AGui childAGui, int numSlotsPerPage, int entriesPerPage, int entries){
		if(entriesPerPage > numSlotsPerPage - 9) return;
		this.parentAGui = parentAGui;
		this.childAGui = childAGui;
		
		int pages = entries/entriesPerPage;
		if(entries%entriesPerPage > 0) pages++;
		
		this.inv = new Inventory[pages];
		this.grid = new IButton[pages*numSlotsPerPage];
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
		players.put(player.getUniqueId(), 0);
		player.openInventory(inv[0]);
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
		return players.keySet().contains(uuid);
	}
	
	@Override
	public void nextPage(UUID uuid){
		int currentPage = players.get(uuid);
		if (players.get(uuid) < (inv.length - 1)){
			players.put(uuid, currentPage+1);
			Player player = Bukkit.getPlayer(uuid);
			if(player == null) return;
			player.openInventory(inv[currentPage+1]);
		}
	}
	
	@Override
	public void lastPage(UUID uuid){
		int currentPage = players.get(uuid);
		if (currentPage > 0){
			players.put(uuid, currentPage-1);
			Player player = Bukkit.getPlayer(uuid);
			if(player == null) return;
			player.openInventory(inv[currentPage-1]);
		}
	}
}
