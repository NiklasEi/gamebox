package me.nikl.gamebox.game;

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
	boolean startGame(Player[] players, String... args);
}