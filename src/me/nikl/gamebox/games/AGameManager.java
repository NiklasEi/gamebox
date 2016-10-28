package me.nikl.gamebox.games;

import me.nikl.gamebox.Language;
import me.nikl.gamebox.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by niklas on 10/28/16.
 *
 *
 */
public abstract class AGameManager implements IGameManager {
	
	
	private Main plugin;
	private ConcurrentHashMap<UUID, IGame> games;
	private ConcurrentHashMap<UUID, IGameGUI> GUIs;
	private IGameGUI gcGameGUI;
	private FileConfiguration gameConfig;
	private Language lang;
	
	public AGameManager(Main plugin, String gameName) {
		this.plugin = plugin;
		this.lang = plugin.lang;
		
		this.games = new ConcurrentHashMap<>();
		this.GUIs = new ConcurrentHashMap<>();
		
		loadGameConfig(gameName);
		
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
		
	}
	
	@Override
	public void openGameGUI(Player player) {
		
	}
	
	@Override
	public void removeFromGUI(Player player) {
		
	}
	
	@Override
	public boolean isInGUI(UUID uuid) {
		return GUIs.keySet().contains(uuid);
	}
	
	@Override
	public void removeGame(UUID uuid) {
		
	}
	
	@Override
	public Main getPlugin() {
		return null;
	}
	
	@Override
	public FileConfiguration getGameConfig() {
		return null;
	}
}
