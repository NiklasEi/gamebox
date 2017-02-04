package me.nikl.gamebox.games;

import me.nikl.gamebox.EnumGames;
import me.nikl.gamebox.Language;
import me.nikl.gamebox.Main;
import me.nikl.gamebox.commands.Permissions;
import me.nikl.gamebox.games.minesweeper.MinesweeperGame;
import me.nikl.gamebox.guis.IGui;
import me.nikl.gamebox.guis.gameguis.AGameGUI;
import me.nikl.gamebox.nms.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.io.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by niklas on 10/28/16.
 *
 *
 */
public abstract class AGameManager implements IGameManager {
	protected EnumGames game;
	protected NMSUtil nms;
	
	protected Main plugin;
	protected ConcurrentHashMap<UUID, IGame> games;
	protected ConcurrentHashMap<UUID, IGui> GUIs;
	protected AGameGUI gameGUI;
	protected FileConfiguration gameConfig;
	protected Language lang;
	
	
	protected boolean econEnabled;
	protected double cost;
	
	public AGameManager(Main plugin, EnumGames game) {
		this.plugin = plugin;
		this.nms = plugin.getNMS();
		this.lang = plugin.lang;
		this.cost = 0.;
		
		
		
		this.games = new ConcurrentHashMap<>();
		this.GUIs = new ConcurrentHashMap<>();
		
		this.game = game;
		
		loadGameConfig(game.toString());
		this.econEnabled = gameConfig.getBoolean("economy.enabled", false);
		this.cost = gameConfig.getDouble("economy.cost", 0.);
	}
	
	private void loadGameConfig(String gameName) {
		File con = new File(plugin.getDataFolder().toString() + File.separatorChar + "games" + File.separatorChar + gameName + File.separatorChar + "config.yml");
		if(!con.exists()){
			con.getParentFile().mkdirs();
			plugin.saveResource("games" + File.separatorChar + gameName + File.separatorChar + "config.yml", false);
		}
		
		// reload config
		try {
			this.gameConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(con), "UTF-8"));
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		}
		InputStream defConfigStream = plugin.getResource("games" + File.separatorChar + gameName + File.separatorChar + "config.yml");
		if (defConfigStream != null){
			@SuppressWarnings("deprecation")
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			this.gameConfig.setDefaults(defConfig);
		}
	}
	
	
	
	@Override
	public boolean startGame(Player player) {
		if(Main.debug)
			Bukkit.getConsoleSender().sendMessage("pluginEcon: " + plugin.getEconEnabled() + "   bypassPerm: " + player.hasPermission(Permissions.getGamePerm(game, "bypass").perm) + "    price: " + cost);
		if(plugin.getEconEnabled() && !player.hasPermission(Permissions.getGamePerm(game, "bypass").perm) && cost > 0){
			if(Main.econ.getBalance(player) >= cost){
				Main.econ.withdrawPlayer(player, cost);
				player.sendMessage(plugin.chatColor(Main.prefix + lang.G_PAYED.replaceAll("%cost%", cost+"").replaceAll("%game%", game.getName())));
				return true;
			} else {
				player.sendMessage(plugin.chatColor(Main.prefix + lang.G_NOT_ENOUGH_MONEY.replaceAll("%game%", game.getName())));
				return false;
			}
		} else {
			return true;
		}
	}
	
	
	@Override
	public void onInvClose(InventoryCloseEvent e) {
		if(!isIngame(e.getPlayer().getUniqueId()) && !isInGUI(e.getPlayer().getUniqueId())){
			return;
		}
		UUID uuid = e.getPlayer().getUniqueId();
		if(Main.debug)e.getPlayer().sendMessage("Inventory was closed");//XXX
		if(games.containsKey(uuid)) {
			games.get(uuid).onDisable();
			games.remove(uuid);
		}
		GUIs.remove(uuid);
	}
	
	@Override
	public boolean isIngame(UUID uuid) {
		return games.keySet().contains(uuid);
	}
	
	@Override
	public void onDisable() {
		for(IGame game : games.values()){
			game.onDisable();
		}
	}
	
	@Override
	public boolean openGameGUI(Player player) {
		//noinspection ConstantConditions
		if(player.hasPermission(Permissions.getGamePerm(game, "play").perm)){
			gameGUI.openGui(player, plugin.getPluginManager().getMainGUI());
			GUIs.put(player.getUniqueId(), gameGUI);
			nms.updateInventoryTitle(player, "&1" + game.getName() + "    &aHave fun!");
			return true;
		}
		return false;
	}
	
	@Override
	public void removeFromGUI(Player player) {
		GUIs.remove(player.getUniqueId());
	}
	
	@Override
	public boolean isInGUI(UUID uuid) {
		return GUIs.keySet().contains(uuid);
	}
	
	@Override
	public void removeGame(UUID uuid) {
		if(games.get(uuid) != null){
			games.get(uuid).onDisable();
		}
		games.remove(uuid);
	}
	
	@Override
	public Main getPlugin() {
		return this.plugin;
	}
	
	@Override
	public FileConfiguration getGameConfig() {
		return this.gameConfig;
	}
	
	@Override
	public AGameGUI getGameMenu(){
		return this.gameGUI;
	}
	
	
	@Override
	public double getPrice(){
		return this.cost;
	}
	
	
	@Override
	public void disableGame(IGame game) {
		game.onDisable();
	}
	
	@Override
	public void onGUIClick(InventoryClickEvent e) {
		GUIs.get(e.getWhoClicked().getUniqueId()).onClick(e);
	}
	
	
	@Override
	public ConcurrentHashMap<UUID, IGame> getRunningGames() {
		return games;
	}
	
	@Override
	public void won(Player player){
		player.sendMessage(plugin.chatColor(Main.prefix + lang.G_WON.replaceAll("%game%", game.getName())));
	}
	
	@Override
	public void won(Player player, int score){
		player.sendMessage(plugin.chatColor(Main.prefix + lang.G_WON_WITH_SCORE.replaceAll("%game%", game.getName()).replaceAll("%score%", score +"")));
	}
	
	@Override
	public void won(Player player, String time){
		player.sendMessage(plugin.chatColor(Main.prefix + lang.G_WON_IN_TIME.replaceAll("%game%", game.getName()).replaceAll("%time%", time)));
	}
	
	@Override
	public void won(Player player, double reward){
		if(player.hasPermission(Permissions.getGamePerm(game, "bypass").perm) || !plugin.getEconEnabled() || !(reward > 0)){
			won(player);
			return;
		}
		Main.econ.depositPlayer(player, reward);
		player.sendMessage(plugin.chatColor(Main.prefix + lang.G_WON_MONEY.replaceAll("%reward%", reward+"")));
	}
	
	@Override
	public void won(Player player, double reward, int score){
		if(player.hasPermission(Permissions.getGamePerm(game, "bypass").perm) || !plugin.getEconEnabled() || !(reward > 0)){
			won(player, score);
			return;
		}
		Main.econ.depositPlayer(player, reward);
		player.sendMessage(plugin.chatColor(Main.prefix + lang.G_WON_MONEY_WITH_SCORE.replaceAll("%reward%", reward+"").replaceAll("%score%", score +"")));
	}
	
	@Override
	public void won(Player player, double reward, String time){
		if(player.hasPermission(Permissions.getGamePerm(game, "bypass").perm) || !plugin.getEconEnabled() || !(reward > 0)){
			won(player, time);
			return;
		}
		Main.econ.depositPlayer(player, reward);
		player.sendMessage(plugin.chatColor(Main.prefix + lang.G_WON_MONEY_IN_TIME.replaceAll("%reward%", reward+"").replaceAll("%time%", time)));
	}
}
