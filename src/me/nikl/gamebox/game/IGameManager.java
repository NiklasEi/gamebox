package me.nikl.gamebox.game;

import org.bukkit.event.inventory.InventoryClickEvent;

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
	public boolean onInventoryClick(InventoryClickEvent event, String action);
}
