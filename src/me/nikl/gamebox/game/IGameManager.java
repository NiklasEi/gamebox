package me.nikl.gamebox.game;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

/**
 * Created by niklas on 2/4/17.
 *
 *
 */
public interface IGameManager {
	/**
	 * Handle a click in the games GUI
	 * Action is the predefined action, set by the GameManager on hook
	 * @param event ClickEvent
	 * @param action defined action on click
	 * @return action successful
	 */
	boolean onInventoryClick(InventoryClickEvent event, String action);
	
	
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
