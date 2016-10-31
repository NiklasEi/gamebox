package me.nikl.gamebox.games.gemcrush;

import me.nikl.gamebox.EnumGames;
import me.nikl.gamebox.Main;
import me.nikl.gamebox.games.AGameManager;
import me.nikl.gamebox.games.minesweeper.MinesweeperGameGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.*;

/**
 * Created by niklas on 10/17/16.
 *
 * Manager für GemCrush
 */
public class GemCrushManager extends AGameManager{
	private boolean econEnabled;
	
	private Map<UUID, Integer> clicks;
	
	private boolean pay, sendMessages, sendBroadcasts, dispatchCommands;
	private Map<Integer, Double> prices;
	private Map<Integer, List<String>> commands;
	private Map<Integer, List<String>> broadcasts;
	private Map<Integer, List<String>> messages;
	
	public GemCrushManager(Main plugin){
		super(plugin, EnumGames.GEMCRUSH);
		
		if(this.gameConfig.isDouble("economy.cost") || this.gameConfig.isInt("economy.cost")){
			this.price = this.gameConfig.getDouble("economy.cost");
		} else {
			this.price = 0;
		}
		
		if(this.gameConfig.isBoolean("economy.enabled")){
			this.econEnabled = this.gameConfig.getBoolean("economy.enabled");
		} else {
			this.econEnabled = false;
		}
		
		this.clicks = new HashMap<>();
		getOnGameEnd();
		
		this.gameGUI = new GemCrushGameGUI(plugin, this);
	}
	
	@Override
	public void onInvClick(InventoryClickEvent e) {
		if(!isIngame(e.getWhoClicked().getUniqueId()) || e.getClickedInventory() == null || e.getCurrentItem() == null || !(e.getWhoClicked() instanceof Player)){
			return;
		}
		
		// player is inGame, clicked inside an inventory and the clicked item is not null
		
		// cancel event and return if it's not a right/left click
		e.setCancelled(true);
		if(!e.getAction().equals(InventoryAction.PICKUP_ALL) && !e.getAction().equals(InventoryAction.PICKUP_HALF)) {
			return;
		}
		
		// check whether the clicked inventory is the top inventory
		if(e.getRawSlot() != e.getSlot()){
			return;
		}
		
		// get Player and Game objects
		Player player = (Player) e.getWhoClicked();
		GemCrushGame game = (GemCrushGame) games.get(player.getUniqueId());
		if(game == null) return;
		
		int slot = e.getSlot();
		
		// switch with getState
		switch(game.getState()){
			
			
			case PLAY:
				if(this.clicks.containsKey(player.getUniqueId())){
					int oldSlot = clicks.get(player.getUniqueId());
					if(slot == oldSlot + 1 || slot == oldSlot - 1 || slot == oldSlot + 9 || slot == oldSlot - 9){
						if(Main.debug)player.sendMessage("Switching Gems " + slot + " and " + oldSlot);
						if(game.switchGems(slot < oldSlot ? slot : oldSlot, slot > oldSlot ? slot : oldSlot)){
							clicks.remove(player.getUniqueId());
						}
					} else if(slot == oldSlot){
						break;
					} else {
						clicks.put(player.getUniqueId(), slot);
						game.shine(slot, true);
						game.shine(oldSlot, false);
						if(Main.debug)player.sendMessage("overwritten click in " + oldSlot + " with click in " + slot);
					}
				} else {
					game.shine(slot, true);
					if(Main.debug)player.sendMessage("saved first click in slot " + slot);
					this.clicks.put(player.getUniqueId(), slot);
				}
				if(Main.debug)player.sendMessage("saved click: " + clicks.get(player.getUniqueId()));
				if(Main.debug)player.sendMessage("Columns:");
				if(Main.debug)player.sendMessage(game.scanColumns().toString());
				if(Main.debug)player.sendMessage("Rows:");
				if(Main.debug)player.sendMessage(game.scanRows().toString());
				break;
			
			case FILLING:
				break;
			
			
			default:
				break;
			
		}
	}
	
	@Override
	public void onInvClose(InventoryCloseEvent e) {
		if(!isIngame(e.getPlayer().getUniqueId())){
			return;
		}
		if(Main.debug)e.getPlayer().sendMessage("Inventory was closed");//XXX
		games.remove(e.getPlayer().getUniqueId());
	}
	
	@Override
	public boolean startGame(Player player) {
		if(!player.hasPermission("gamebox.gemcrush.play")){
			player.sendMessage(chatColor(Main.prefix + lang.CMD_NO_PERM));
			return false;
		}
		if(plugin.getEconEnabled() && this.econEnabled && !player.hasPermission("gamebox.gemcrush.bypass")){
			if(Main.econ.getBalance(player) >= this.price){
				Main.econ.withdrawPlayer(player, this.price);
				player.sendMessage(chatColor(Main.prefix + lang.GEMCRUSH_PAYED.replaceAll("%cost%", this.price+"")));
				games.put(player.getUniqueId(), new GemCrushGame(this, player.getUniqueId()));
				return true;
			} else {
				player.sendMessage(chatColor(Main.prefix + lang.GEMCRUSH_NOT_ENOUGH_MONEY));
				return false;
			}
		}
		games.put(player.getUniqueId(), new GemCrushGame(this, player.getUniqueId()));
		return true;
	}
	
	@Override
	public double getPrice() {
		return this.price;
	}
	
	
	private void getOnGameEnd() {
		if(this.gameConfig.isConfigurationSection("onGameEnd.scoreIntervals")) {
			ConfigurationSection onGameEnd = this.gameConfig.getConfigurationSection("onGameEnd");
			prices = new HashMap<>();
			commands = new HashMap<>();
			broadcasts = new HashMap<>();
			messages = new HashMap<>();
			pay = onGameEnd.getBoolean("pay");
			sendMessages = onGameEnd.getBoolean("sendMessages");
			sendBroadcasts = onGameEnd.getBoolean("sendBroadcasts");
			dispatchCommands = onGameEnd.getBoolean("dispatchCommands");
			onGameEnd = gameConfig.getConfigurationSection("onGameEnd.scoreIntervals");
			for (String key : onGameEnd.getKeys(false)) {
				int keyInt;
				try {
					keyInt = Integer.parseInt(key);
				} catch (NumberFormatException e) {
					Bukkit.getLogger().warning("[GemCrush] NumberFormatException while getting the rewards from config!");
					continue;
				}
				if (onGameEnd.isSet(key + ".money") && (onGameEnd.isDouble(key + ".money") || onGameEnd.isInt(key + ".money"))) {
					prices.put(keyInt, onGameEnd.getDouble(key + ".money"));
				} else {
					prices.put(keyInt, 0.);
				}
				
				if (onGameEnd.isSet(key + ".broadcast") && onGameEnd.isList(key + ".broadcast")) {
					broadcasts.put(keyInt, onGameEnd.getStringList(key + ".broadcast"));
				} else {
					broadcasts.put(keyInt, null);
				}
				
				if (onGameEnd.isSet(key + ".messages") && onGameEnd.isList(key + ".messages")) {
					messages.put(keyInt, onGameEnd.getStringList(key + ".messages"));
				} else {
					messages.put(keyInt, null);
				}
				
				if (onGameEnd.isSet(key + ".commands") && onGameEnd.isList(key + ".commands")) {
					commands.put(keyInt, onGameEnd.getStringList(key + ".commands"));
				} else {
					commands.put(keyInt, null);
				}
			}
			
			if(Main.debug){
				Bukkit.getConsoleSender().sendMessage("Testing onGameEnd: ");
				for (int i : prices.keySet()) {
					
					Bukkit.getConsoleSender().sendMessage("Over: " + i + "    reward: " + prices.get(i));
					Bukkit.getConsoleSender().sendMessage("    broadcasts: " + broadcasts.get(i));
					Bukkit.getConsoleSender().sendMessage("    messages: " + messages.get(i));
					Bukkit.getConsoleSender().sendMessage("    commands: " + commands.get(i));
				}
				
				Bukkit.getConsoleSender().sendMessage(" ");
				Bukkit.getConsoleSender().sendMessage("Run some tests: ");
				Random rand = new Random();
				for (int i = 0; i < 10; i++) {
					int score = rand.nextInt(600);
					Bukkit.getConsoleSender().sendMessage("Random score: " + score);
					Bukkit.getConsoleSender().sendMessage("Key: " + getKey(score));
				}
				Bukkit.getConsoleSender().sendMessage("Random score: " + 100);
				Bukkit.getConsoleSender().sendMessage("Key: " + getKey(100));
			}
			
		}
	}
	
	private int getKey(int score){
		int distance = -1;
		for(int key : prices.keySet()) {
			if((score - key) >= 0 && (distance < 0 || distance > (score - key))){
				distance = score - key;
			}
		}
		if(distance > -1)
			return score - distance;
		return -1;
	}
	
	
	void onGameEnd(int score, Player player){
		onGameEnd(score, player, true, true, true, true);
	}
	
	private void onGameEnd(int score, Player player, boolean payOut, boolean sendMessages, boolean dispatchCommands, boolean sendBroadcasts){
		//plugin.setStatistics(player.getUniqueId(), score);
		int key = getKey(score);
		if(plugin.getEconEnabled() && this.econEnabled && this.pay && payOut){
			double reward = prices.get(key);
			if(plugin.getEconEnabled() && reward > 0){
				Main.econ.depositPlayer(player, reward);
				player.sendMessage(chatColor(Main.prefix + plugin.lang.GEMCRUSH_FINISHED_WITH_PAY.replaceAll("%score%", score +"").replaceAll("%reward%", reward + "")));
			} else {
				player.sendMessage(chatColor(Main.prefix + plugin.lang.GEMCRUSH_FINISHED_NO_PAY.replaceAll("%score%", score +"")));
			}
		} else {
			player.sendMessage(chatColor(Main.prefix + plugin.lang.GEMCRUSH_FINISHED_NO_PAY.replaceAll("%score%", score +"")));
		}
		
		if(sendMessages && this.sendMessages && messages != null && messages.get(key) != null && messages.get(key).size() > 0){
			for(String message : messages.get(key)){
				player.sendMessage(chatColor(Main.prefix + " " + message.replaceAll("%player%", player.getName()).replaceAll("%score%", score + "")));
			}
		}
		
		if(dispatchCommands && this.dispatchCommands && commands != null && commands.get(key) != null && commands.get(key).size() > 0){
			for(String cmd : commands.get(key)){
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("%player%", player.getName()).replaceAll("%score%", score + ""));
			}
		}
		
		if(sendBroadcasts && this.sendBroadcasts && broadcasts != null && broadcasts.get(key) != null && broadcasts.get(key).size() > 0){
			for(String broadcast: broadcasts.get(key)){
				Bukkit.broadcastMessage(chatColor(Main.prefix + " " + broadcast.replaceAll("%player%", player.getName()).replaceAll("%score%", score + "")));
			}
		}
		
	}
	
	private String chatColor(String message){
		return ChatColor.translateAlternateColorCodes('&', message);
	}
}
