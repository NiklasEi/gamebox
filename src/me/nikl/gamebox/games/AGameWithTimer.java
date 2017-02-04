package me.nikl.gamebox.games;

import me.nikl.gamebox.Main;
import me.nikl.gamebox.nms.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Created by niklas on 10/31/16.
 *
 *
 */
public abstract class AGameWithTimer extends BukkitRunnable implements IGame{
	protected Player[] players;
	protected int numPlayers;
	protected IGameManager gameManager;
	
	protected NMSUtil nms;
	
	
	public AGameWithTimer(Player[] players, IGameManager gameManager){
		this.gameManager = gameManager;
		this.players = players;
		this.numPlayers = players.length;
		
		this.nms = gameManager.getPlugin().getNMS();
	}
	
	public AGameWithTimer(UUID[] players, IGameManager gameManager){
		this.gameManager = gameManager;
		this.players = new Player[players.length];
		for(int i = 0; i<players.length;i++){
			this.players[i] = Bukkit.getPlayer(players[i]);
		}
		this.numPlayers = players.length;
		
		this.nms = gameManager.getPlugin().getNMS();
	}
	
	
	public AGameWithTimer(UUID player, IGameManager gameManager){
		this.gameManager = gameManager;
		this.numPlayers = 1;
		this.players = new Player[1];
		this.players[0] =  Bukkit.getPlayer(player);
		
		this.nms = gameManager.getPlugin().getNMS();
	}
	
	public AGameWithTimer(Player player, IGameManager gameManager){
		this(player.getUniqueId(), gameManager);
	}
	
	@Override
	public Player[] getPlayers(){
		return players;
	}
}
