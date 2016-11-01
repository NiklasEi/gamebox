package me.nikl.gamebox.guis.maingui;

import me.nikl.gamebox.EnumGames;
import me.nikl.gamebox.Main;
import me.nikl.gamebox.PluginManager;
import me.nikl.gamebox.games.IGameManager;
import me.nikl.gamebox.guis.AGui;
import me.nikl.gamebox.guis.IGui;
import me.nikl.gamebox.guis.standard.CloseButton;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by niklas on 10/29/16.
 *
 *
 */
public class MainGui extends AGui {
	private ConcurrentHashMap<EnumGames, IGameManager> registeredGames;
	private FileConfiguration config;
	private PluginManager pManager;
	
	public MainGui(PluginManager pManager, FileConfiguration config){
		super(54);
		this.pManager = pManager;
		this.registeredGames = pManager.getRegisteredGames();
		this.config = config;
		loadButtons();
	}
	
	private void loadButtons() {
		for(EnumGames game : registeredGames.keySet()){
			loadButton(game);
		}
		grid[grid.length-5] = new CloseButton(pManager.getPlugin().lang);
		buildInventory();
	}
	
	private void loadButton(EnumGames game) {
		for(int i = 0; i < grid.length; i++){
			if(grid[i] != null) continue;
			grid[i] = new GameButton(pManager, game, registeredGames.get(game), config);
			return;
		}
	}
	
	@Override
	public void onClick(InventoryClickEvent event) {
		if(event.getSlot() != event.getRawSlot()) return;
		int slot = event.getSlot();
		if(grid[slot] == null) return;
		grid[slot].onClick(event, this);
	}
	
	@Override
	public boolean openParentGUI(Player player, IGui from) {
		return false;
	}
	
	@Override
	public boolean openChildGUI(Player player, IGui from) {
		return false;
	}
	
	public void buildInventory(){
		for(int i = 0; i < grid.length; i ++){
			if(grid[i] != null){
				this.inv.setItem(i, grid[i].getItem());
			}
		}
	}
	
	@Override
	public boolean openGui(Player player, IGui from){
		if(from != null) from.removePlayer(player);
		players.add(player.getUniqueId());
		if(Main.debug) Bukkit.getConsoleSender().sendMessage("added to " + this.toString());
		player.openInventory(inv);
		return true;
	}
	
	public void addToGui(UUID uuid){
		players.add(uuid);
	}
}
