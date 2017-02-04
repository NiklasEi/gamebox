package me.nikl.gamebox;

import org.bukkit.entity.Player;

/**
 * Created by niklas on 2/4/17.
 *
 * Store all players in a game
 * Cash player info?
 */
public class GamePlayers {
	private Player[] players;
	
	public GamePlayers(String gameID, Player[] players){
		this.players = players;
	}
	
	public Player[] getPlayers(){
		return this.players;
	}
	
	public int getNum(){
		return players.length;
	}
	
}
