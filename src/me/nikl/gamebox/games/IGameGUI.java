package me.nikl.gamebox.games;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Created by niklas on 10/18/16.
 */
public interface IGameGUI {
	
	void open(Player player);
	void onGUIClick(InventoryClickEvent e);
	void setGameManager(IGameManager IGameManager);
}
