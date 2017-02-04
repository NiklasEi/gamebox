package me.nikl.gamebox.games;

import org.bukkit.entity.Player;

/**
 * Created by niklas on 10/17/16.
 *
 * interface for all games
 * store players, manager, inventory
 * implement game behaviour
 */
public interface IGame {
	
	// called on disable
	void onDisable();
	
	// return all players
	Player[] getPlayers();
	
	//return number of players
	int getNumPlayers();
}
