package me.nikl.gamebox.games;

import me.nikl.gamebox.EnumGames;
import me.nikl.gamebox.Language;
import me.nikl.gamebox.Main;
import me.nikl.gamebox.commands.Permissions;
import me.nikl.gamebox.guis.IGui;
import me.nikl.gamebox.guis.gameguis.AGameGUI;
import me.nikl.gamebox.nms.NMSUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

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
	
	protected double price;
	
	public AGameManager(Main plugin, EnumGames game) {
		this.plugin = plugin;
		this.nms = plugin.getNMS();
		this.lang = plugin.lang;
		this.price = 0.;
		
		
		this.games = new ConcurrentHashMap<>();
		this.GUIs = new ConcurrentHashMap<>();
		
		this.game = game;
		
		loadGameConfig(game.toString());
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
			nms.updateInventoryTitle(player, "&1" + game.getName());
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
		return this.price;
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
}
