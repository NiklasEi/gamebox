package me.nikl.gamebox.games.minesweeper;

import me.nikl.gamebox.EnumGames;
import me.nikl.gamebox.Language;
import me.nikl.gamebox.Main;
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
 * Created by niklas on 10/30/16.
 */
public class MinesweeperGameManager extends AGameManager{
	
	private final boolean econEnabled;
	private final double reward;
	
	private boolean wonCommandsEnabled;
	private List wonCommands;
	
	public MinesweeperGameManager(Main plugin){
		super(plugin, EnumGames.MINESWEEPER);

		this.wonCommandsEnabled = gameConfig.getBoolean("wonCommands.enabled", false);
		this.wonCommands = gameConfig.getStringList("wonCommands.commands");
		
		
		this.econEnabled = gameConfig.getBoolean("economy.enabled", false);
		this.price = gameConfig.getDouble("economy.cost", 0.);
		this.reward = gameConfig.getDouble("economy.reward", 0.);
		this.gameGUI = new MinesweeperGameGUI(plugin, this);
	}
	
	@Override
	public void onInvClick(InventoryClickEvent e) {
		if(games.get(e.getWhoClicked().getUniqueId()) == null || e.getClickedInventory() == null){
			return;
		}
		e.setCancelled(true);
		/*if(!e.getClickedInventory().equals((MinesweeperGame)games.get(e.getWhoClicked().getUniqueId())).getInv()){
			return;
		}*/
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
					game.setState(lang.MINESWEEPER_TITLE_END.replaceAll("%timer%", game.getDisplayTime()+""));
					if(plugin.getEconEnabled() && !e.getWhoClicked().hasPermission("gamebox.minesweeper.bypass")){
						Player player = (Player) e.getWhoClicked();
						Main.econ.depositPlayer(player, plugin.getConfig().getDouble("economy.reward"));
						player.sendMessage(plugin.chatColor(Main.prefix + lang.MINESWEEPER_GAME_WON_MONEY.replaceAll("%reward%", reward+"")));
					}
					if(wonCommandsEnabled && !e.getWhoClicked().hasPermission("gamebox.minesweeper.bypass")){
						Player player = (Player) e.getWhoClicked();
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
	public void onInvClose(InventoryCloseEvent e) {
		if(games.get(e.getPlayer().getUniqueId()) == null )
			return;
		MinesweeperGame game = (MinesweeperGame) games.get(e.getPlayer().getUniqueId());
		if(game.isChangingInv()) return;
		game.cancelTimer();
		games.remove(e.getPlayer().getUniqueId());
	}
	
	@Override
	public boolean startGame(Player player) {
		if(!player.hasPermission("gamebox.minesweeper.play")){
			player.sendMessage(plugin.chatColor(Main.prefix + lang.CMD_NO_PERM));
			return false;
		}
		if(Main.debug)Bukkit.getConsoleSender().sendMessage("pluginEcon: " + plugin.getEconEnabled() + "   bypassPerm: " + player.hasPermission("gamebox.minesweeper.bypass") + "    price: " + price);
		if(plugin.getEconEnabled() && !player.hasPermission("gamebox.minesweeper.bypass") && price > 0){
			if(Main.econ.getBalance(player) >= price){
				Main.econ.withdrawPlayer(player, price);
				player.sendMessage(plugin.chatColor(Main.prefix + lang.MINESWEEPER_GAME_PAYED.replaceAll("%cost%", price+"")));
				games.put(player.getUniqueId(), new MinesweeperGame(plugin, player.getUniqueId(), this));
				return true;
			} else {
				player.sendMessage(plugin.chatColor(Main.prefix + lang.MINESWEEPER_GAME_NOT_ENOUGH_MONEY));
				return false;
			}
		} else {
			games.put(player.getUniqueId(), new MinesweeperGame(plugin, player.getUniqueId(), this));
			return true;
		}
	}
	
	@Override
	public double getPrice(){
		return this.price;
	}
}
