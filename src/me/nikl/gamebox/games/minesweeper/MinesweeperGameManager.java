package me.nikl.gamebox.games.minesweeper;

import me.nikl.gamebox.EnumGames;
import me.nikl.gamebox.Language;
import me.nikl.gamebox.Main;
import me.nikl.gamebox.commands.Permissions;
import me.nikl.gamebox.games.gemcrush.GemCrushGame;
import me.nikl.gamebox.guis.gameguis.AGameGUI;
import me.nikl.gamebox.games.AGameManager;
import me.nikl.gamebox.games.IGame;
import me.nikl.gamebox.guis.IGui;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.io.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Game manager for the game minesweeper
 *
 */
public class MinesweeperGameManager extends AGameManager{
	private final double reward;
	
	private boolean wonCommandsEnabled;
	private List wonCommands;
	
	boolean automaticReveal;
	
	public MinesweeperGameManager(Main plugin){
		super(plugin, EnumGames.MINESWEEPER);

		this.wonCommandsEnabled = gameConfig.getBoolean("wonCommands.enabled", false);
		this.wonCommands = gameConfig.getStringList("wonCommands.commands");
		
		
		this.reward = gameConfig.getDouble("economy.reward", 0.);
		this.automaticReveal = !gameConfig.getBoolean("rules.turnOffAutomaticRevealing");
		
		this.gameGUI = new MinesweeperGameGUI(plugin, this);
	}
	
	@Override
	public void onInvClick(InventoryClickEvent e) {
		if(games.get(e.getWhoClicked().getUniqueId()) == null || e.getClickedInventory() == null){
			return;
		}
		e.setCancelled(true);
		
		if(e.getRawSlot() != e.getSlot()){
			return;
		}
		MinesweeperGame game = (MinesweeperGame) games.get(e.getWhoClicked().getUniqueId());
		int slot = e.getSlot();
		if(!game.isStarted()){
			game.start();
		}
		if (game.isEmpty(slot)){
			e.setCancelled(true);
			return;
		}
		if(Main.debug)
			Bukkit.getConsoleSender().sendMessage("Clicked   " + "Covered: " + game.isCovered(slot) +"   Flaged: " + (game.isFlaged(game.getInv().getItem(slot)) && e.getAction().equals(InventoryAction.PICKUP_HALF)) + "   toGUI: " + game.isToGameGUI(slot));
		if(game.isCovered(slot)){
			if(e.getAction().equals(InventoryAction.PICKUP_HALF)){
				game.setFlagged(slot);
			} else if (e.getAction().equals(InventoryAction.PICKUP_ALL)){
				game.uncover(slot);
				if(game.isWon()){
					game.cancelTimer();
					game.reveal();
					game.setState(lang.MINESWEEPER_TITLE_END.replaceAll("%timer%", game.getDisplayTime()));
					Player player = (Player) e.getWhoClicked();
					super.won(player, reward, game.getDisplayTime());
					if(wonCommandsEnabled && !e.getWhoClicked().hasPermission(Permissions.GAME_MINESWEEPER_BYPASS.perm)){
						for(Object cmd : wonCommands){
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ((String)cmd).replace("%player%", player.getName()));
						}
					}
				}
			}
		} else if(game.isFlaged(game.getInv().getItem(slot)) && e.getAction().equals(InventoryAction.PICKUP_HALF)){
			game.deFlag(slot);
		} else if(game.isToGameGUI(slot)){
			removeGame(e.getWhoClicked().getUniqueId());
			Player player = Bukkit.getPlayer(e.getWhoClicked().getUniqueId());
			openGameGUI(player);
		}
		e.setCancelled(true);
	}
	
	
	@Override
	public boolean startGame(Player player) {
		if(!super.startGame(player)) return false;
		games.put(player.getUniqueId(), new MinesweeperGame(plugin, player.getUniqueId(), this));
		return true;
	}
}
