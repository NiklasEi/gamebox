package me.nikl.gamebox.games;

import me.nikl.gamebox.Main;
import me.nikl.gamebox.guis.IGui;
import me.nikl.gamebox.guis.gameguis.AGameGUI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by niklas on 10/17/16.
 *
 * Handle the events
 * Store all players that are in the game atm with their game object
 */
public interface IGameManager {
	
	ConcurrentHashMap<UUID, IGame> getRunningGames();
	
	void onInvClick(InventoryClickEvent event);
	
	void onInvClose(InventoryCloseEvent event);
	
	boolean isIngame(UUID uuid);
	
	void onDisable();
	
	boolean openGameGUI(Player player);
	
	void removeFromGUI(Player player);
	
	boolean isInGUI(UUID uuid);
	
	void onGUIClick(InventoryClickEvent e);
	
	boolean startGame(Player player);
	
	void removeGame(UUID uuid);
	
	Main getPlugin();
	
	AGameGUI getGameMenu();
	
	FileConfiguration getGameConfig();
	
	// if a game is configured wrong or another error happens, this will be called by the game
	// block clicks in the game gui and note in the lore ToDo!!!
	void disableGame(IGame game);
	
	double getPrice();
	
	void won(Player player);
	
	void won(Player player, int score);
	
	void won(Player player, String time);
	
	void won(Player player, double reward);
	
	void won(Player player, double reward, int score);
	
	void won(Player player, double reward, String time);
}
