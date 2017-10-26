package me.nikl.gamebox.games;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.UUID;

/**
 * Created by niklas on 2/4/17.
 *
 *
 */
public interface IGameManager {

	/**
	 * Handle a click in the game
	 *
	 * @param event ClickEvent
	 * @return successful
	 */
	boolean onInventoryClick(InventoryClickEvent event);


	/**
	 * Handle an InventoryCloseEvent
	 * You should do the same as in IGameManager.removeFromGame
	 *
	 * @param event CloseEvent
	 * @return successful
	 */
	boolean onInventoryClose(InventoryCloseEvent event);
	
	
	/**
	 * Return whether a player is in a game or not
	 * @param uuid player's uuid
	 * @return ingame
	 */
	boolean isInGame(UUID uuid);
	
	/**
	 * Start a game for a player or players
	 * @param players players to start the game with
	 * @param args additional arguments
	 * @return return whether the game was started or not
	 */
	int startGame(Player[] players, boolean playSounds, String... args);


	/**
	 * Remove the specified player from his game
	 * Do not close the inventory
	 *
	 * If the game is not finished yet, consider this as the player closing the inventory
	 * This method is mainly called when a button from the lower inventory is clicked and the player goes back to one of the menus
	 *
	 * @param uuid player to remove
	 */
	void removeFromGame(UUID uuid);

/*
	/**
	 * Quit all games
	 *
	 * There is a reload going on
	 *//*
	void shutDown();
*/

}
