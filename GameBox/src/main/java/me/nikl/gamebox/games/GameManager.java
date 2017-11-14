package me.nikl.gamebox.games;

import me.nikl.gamebox.data.Statistics;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by niklas on 2/4/17.
 *
 *
 */
public abstract class GameManager {
	protected HashMap<String, GameRule> gameRules;
	protected Statistics statistics;

	/**
	 * Handle a click in the game
	 *
	 * @param event ClickEvent
	 * @return successful
	 */
	public abstract boolean onInventoryClick(InventoryClickEvent event);


	/**
	 * Handle an InventoryCloseEvent
	 * You should do the same as in IGameManager.removeFromGame
	 *
	 * @param event CloseEvent
	 * @return successful
	 */
	public abstract boolean onInventoryClose(InventoryCloseEvent event);
	
	
	/**
	 * Return whether a player is in a game or not
	 * @param uuid player's uuid
	 * @return ingame
	 */
	public abstract boolean isInGame(UUID uuid);
	
	/**
	 * Start a game for a player or players
	 * @param players players to start the game with
	 * @param args additional arguments
	 * @return return whether the game was started or not
	 */
	public abstract int startGame(Player[] players, boolean playSounds, String... args);


	/**
	 * Remove the specified player from his game
	 * Do not close the inventory
	 *
	 * If the game is not finished yet, consider this as the player closing the inventory
	 * This method is mainly called when a button from the lower inventory is clicked
	 * and the player goes back to one of the menus
	 *
	 * @param uuid player to remove
	 */
	public abstract void removeFromGame(UUID uuid);

	/**
	 * Load game rules from the given ConfigurationSection
	 *
	 * @param buttonSec Configuration section with the rules
	 * @param buttonID ID of the rules
	 */
    public abstract void loadGameRules(ConfigurationSection buttonSec, String buttonID);

    public HashMap<String, GameRule> getGameRules(){
    	return this.gameRules;
	}
}
