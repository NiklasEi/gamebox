package me.nikl.gamebox.games.gemcrush;

import me.nikl.gamebox.Main;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

/**
 * Created by niklas on 10/6/16.
 *
 * Timer Class used to break matched Gems with a small delay
 */
class BreakTimer extends BukkitRunnable{
	
	private ArrayList<Integer> toBreak;
	private GemCrushGame game;
	
	BreakTimer(GemCrushGame game, ArrayList<Integer> toBreak, int breakTicks){
		this.toBreak = toBreak;
		this.game = game;
		
		this.runTaskLater(Main.getPlugin(Main.class), breakTicks);
	}
	
	
	@Override
	public void run() {
		game.breakGems(toBreak);
	}
}
