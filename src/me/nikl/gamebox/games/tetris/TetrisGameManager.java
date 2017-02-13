package me.nikl.gamebox.games.tetris;

import me.nikl.gamebox.EnumGames;
import me.nikl.gamebox.Main;
import me.nikl.gamebox.games.AGameManager;
import me.nikl.gamebox.games.IGame;
import me.nikl.gamebox.games.minesweeper.MinesweeperGameGUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by niklas on 10/31/16.
 *
 *
 */
public class TetrisGameManager extends AGameManager{
	private Map<UUID, ItemStack[]> savedInv;

	
	public TetrisGameManager(Main plugin){
		super(plugin, EnumGames.TETRIS);
		
		this.savedInv = new HashMap<>();
		this.gameGUI = new TetrisGameGUI(plugin, this);
	}
	
	@Override
	public void onInvClick(InventoryClickEvent event) {
		if(event.getSlot() == event.getRawSlot()) return;
		if(event.getCurrentItem() == null) return;
		TetrisGame game = (TetrisGame) games.get(event.getWhoClicked().getUniqueId());
		if(game == null) return;
		if(event.getSlot() == 21){
			event.getWhoClicked().sendMessage("clicked left");
			game.moveToLeft();
		} else if(event.getSlot() == 22){
			if(event.getAction() == InventoryAction.PICKUP_ALL){
				event.getWhoClicked().sendMessage("turn clockwise");
				game.turn(1);
			} else if(event.getAction() == InventoryAction.PICKUP_HALF){
				event.getWhoClicked().sendMessage("turn anticlockwise");
				game.turn(3);
			}
		} else if(event.getSlot() == 23){
			if(Main.debug)event.getWhoClicked().sendMessage("clicked right");
			game.moveToRight();
		} else if(event.getSlot() == 31){
			if(Main.debug)event.getWhoClicked().sendMessage("clicked down");
			game.moveDown();
		} else if(event.getSlot() == 35){//toGameGUI
			if(Main.debug)event.getWhoClicked().sendMessage("go to game gui");
			game.onDisable();
			games.remove(event.getWhoClicked().getUniqueId());
			Player player = Bukkit.getPlayer(event.getWhoClicked().getUniqueId());
			if(player != null)openGameGUI(player);
		} else if(event.getSlot() == 27){//toMainGUI
			if(Main.debug)event.getWhoClicked().sendMessage("go to main gui");
			game.onDisable();
			games.remove(event.getWhoClicked().getUniqueId());
			Player player = Bukkit.getPlayer(event.getWhoClicked().getUniqueId());
			if(player != null)plugin.getPluginManager().openGUI(player, null);
		}
	}
	
	@Override
	public boolean startGame(Player player) {
		if(!super.startGame(player)) return false;
		savedInv.put(player.getUniqueId(), player.getInventory().getContents().clone());
		games.put(player.getUniqueId(), new TetrisGame(player, this));
		return true;
	}
	
	public void resetInv(Player player) {
		if(Main.debug) Bukkit.getConsoleSender().sendMessage("resetting Inventory");
		player.getInventory().setContents(savedInv.get(player.getUniqueId()));
	}
}
