package me.nikl.gamebox.games.tetris;

import me.nikl.gamebox.EnumGames;
import me.nikl.gamebox.Main;
import me.nikl.gamebox.games.AGameManager;
import me.nikl.gamebox.games.IGame;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by niklas on 10/31/16.
 *
 *
 */
public class TetrisGameManager extends AGameManager{
	
	public TetrisGameManager(Main plugin){
		super(plugin, EnumGames.TETRIS);
	}
	
	@Override
	public ConcurrentHashMap<UUID, IGame> getRunningGames() {
		return null;
	}
	
	@Override
	public void onInvClick(InventoryClickEvent event) {
		
	}
	
	@Override
	public void onInvClose(InventoryCloseEvent event) {
		
	}
	
	@Override
	public void onGUIClick(InventoryClickEvent e) {
		
	}
	
	@Override
	public boolean startGame(Player player) {
		games.put(player.getUniqueId(), new TetrisGame(player, this));
		return true;
	}
	
	@Override
	public void disableGame(IGame game) { //ToDo: change the disable methods completly!!
		
	}
}
