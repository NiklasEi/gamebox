package me.nikl.gamebox.games.minesweeper;

import me.nikl.gamebox.Main;
import org.bukkit.scheduler.BukkitRunnable;

public class MinesweeperGameTimer extends BukkitRunnable{
	
	private MinesweeperGame game;
	private int time;
	
	MinesweeperGameTimer(MinesweeperGame game){
		this.game = game;
		this.time = 0;
		
		this.runTaskTimer(Main.getPlugin(Main.class), 20, 20);
	}

	@Override
	public void run() {
		time++;

		String minutes = (time/60) + "";
		if(minutes.length()<2) minutes = "0" + minutes;
		String seconds = (time%60) + "";
		if(seconds.length()<2) seconds = "0" + seconds;
		
		game.setTime(minutes + ":" + seconds);		
	}
}
