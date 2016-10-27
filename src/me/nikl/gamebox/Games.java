package me.nikl.gamebox;

/**
 * Created by niklas on 10/17/16.
 *
 *
 */
public enum Games {
	MINESWEEPER("Minesweeper"), BATTLESHIP("Battleship", 2), GEMCRUSH("Gem Crush");
	
	// number of players per game
	int playerNumber;
	
	// name of the game
	String name;
	
	Games(String name, int playerNumber){
		this.name = name;
		this.playerNumber = playerNumber;
	}
	
	Games(String name){
		this(name, 1);
	}
	
	@Override
	public String toString(){
		return this.name().toLowerCase();
	}
}
