package me.nikl.gamebox.games.tetris;

import me.nikl.gamebox.Main;
import me.nikl.gamebox.games.AGame;
import me.nikl.gamebox.games.AGameWithTimer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * Created by niklas on 10/31/16.
 */
public class TetrisGame extends AGameWithTimer{
	private Player player;
	private Inventory inv;
	private int moveTicks;
	
	
	public TetrisGame(Player player, TetrisGameManager gameManager) {
		super(player, gameManager);
		this.inv = Bukkit.createInventory(null, 81, "Tetris");
		
		
		this.moveTicks = 5; //ToDo!!!!!!!!!!!
		this.runTaskTimer(Main.getPlugin(Main.class), 0, this.moveTicks);
	}
	
	@Override
	public void onDisable() {
		
	}
	
	@Override
	public void won(Player... players) {
		
	}
	
	@Override
	public int getNumPlayers() {
		return numPlayers;
	}
	
	@Override
	public void run() {
		
	}
}
