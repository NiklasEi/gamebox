package me.nikl.gamebox.guis.gameguis;

import me.nikl.gamebox.Language;
import me.nikl.gamebox.Main;
import me.nikl.gamebox.games.IGameManager;
import me.nikl.gamebox.guis.AGui;
import me.nikl.gamebox.guis.IButton;
import me.nikl.gamebox.guis.IGui;
import me.nikl.gamebox.guis.standard.CloseButton;
import me.nikl.gamebox.guis.standard.ToMainGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

/**
 * Created by niklas on 10/17/16.
 *
 * AGameGUI for every game
 */
public abstract class AGameGUI extends AGui {
	protected Main plugin;
	protected Language lang;
	protected IGameManager gameManager;
	
	
	
	public AGameGUI(Main plugin, IGameManager gManager) {
		super(54);
		this.plugin = plugin;
		this.lang = plugin.lang;
		
		this.gameManager = gManager;
		
		this.grid[grid.length-5] = new CloseButton(lang);
		this.grid[grid.length-6] = new ToMainGUI(lang, plugin.getPluginManager());
		
		buildInventory();
	}
	
	public String chatColor(String message){
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	
	public void addToGrid(IButton button){
		for(int i = 0; i < grid.length; i ++){
			if(grid[i] == null){
				grid[i] = button;
				break;
			}
		}
		buildInventory();
	}
	
	public void buildInventory(){
		for(int i = 0; i < grid.length; i ++){
			if(grid[i] != null){
				inv.setItem(i, grid[i].getItem());
 			}
		}
	}
	
	@Override
	public void onClick(InventoryClickEvent event) {
		if(event.getSlot() != event.getRawSlot()) return;
		if(grid[event.getSlot()] != null){
			grid[event.getSlot()].onClick(event, this);
		}
	}
	
	@Override
	public boolean openParentGUI(Player player, IGui from) {
		return plugin.getPluginManager().openGUI(player, from);
	}
	
	@Override
	public boolean openChildGUI(Player player, IGui from) {
		return false;
	}
	
	@Override
	public void removePlayer(UUID player){
		players.remove(player);
		gameManager.removeFromGUI(Bukkit.getPlayer(player));
	}
}