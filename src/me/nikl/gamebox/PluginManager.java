package me.nikl.gamebox;

import me.nikl.gamebox.games.*;
import me.nikl.gamebox.games.gemcrush.GemCrushManager;
import me.nikl.gamebox.games.minesweeper.MinesweeperGameManager;
import me.nikl.gamebox.guis.IGui;
import me.nikl.gamebox.guis.maingui.MainGui;
import me.nikl.gamebox.nms.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Created by niklas on 10/17/16.
 *
 * Manage the main GUI and create the GameManagers / register all games
 * Clicks are managed here
 * Check what GUI is open for the player and then pass the click event on to the manager of the GUI.
 */
public class PluginManager implements Listener{
	
	// Main instance
	private Main plugin;
	
	// Language
	private Language lang;
	
	// plugin configuration
	private FileConfiguration config;
	
	// main GUI with all registered games and a close button
	private MainGui mainGUI;
	
	// registered games
	private ConcurrentHashMap<EnumGames, IGameManager> registeredGames;
	
	private NMSUtil nms;
	
	//close Button
	private ItemStack closeButton;
	
	public PluginManager(Main plugin){
		this.plugin = plugin;
		this.lang = plugin.lang;
		this.nms = plugin.getNMS();
		this.config = plugin.getConfig();
				
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	public void registerGames() {
		if(registeredGames != null){
			this.shutDown();
		}
		this.registeredGames = new ConcurrentHashMap<>();
		if(Main.debug) Bukkit.getConsoleSender().sendMessage("registering games");
		if(!config.isConfigurationSection("games")) return;
		ConfigurationSection gamesSection = config.getConfigurationSection("games");
		for(EnumGames game : EnumGames.values()){
			if(!gamesSection.getBoolean(game.toString() + ".enabled")){
				Bukkit.getConsoleSender().sendMessage(Main.plainPrefix + " the game " + game.toString() + " is disabled");
				continue;
			}
			switch (game.toString()) {
				case "minesweeper":
					registeredGames.put(EnumGames.MINESWEEPER, new MinesweeperGameManager(plugin));
					break;
				case "gemcrush":
					registeredGames.put(EnumGames.GEMCRUSH, new GemCrushManager(plugin));
					break;
				/*case "battleship":
					registeredGames.put(EnumGames.BATTLESHIP, new BattleshipManager(plugin));
					ArrayList<String> testLore = new ArrayList<>();
					testLore.add(chatColor("&1Testing the lore"));
					testLore.add(chatColor("&aTest row 2"));
					mainGUI.setItem(slot, loadButton("games." + game, "&1Battleship", testLore));
					slots.put(slot, EnumGames.BATTLESHIP);
					slot++;
					break;*/
				default:
					Bukkit.getLogger().log(Level.WARNING, chatColor("&4Game " + game.toString() + " was found but could not be registered!"));
					break;
			}
		}
		this.mainGUI = new MainGui(this, config);
	}
	
	/***
	 * open the main AGui for a player
	 * add him to the set of players in the AGui and update the title from language
	 *
	 * @param player this players inventory will be opened
	 */
	public boolean openGUI(Player player, IGui from){
		boolean b = mainGUI.openGui(player, from);
		if(b) {
			mainGUI.addToGui(player.getUniqueId());
			if (Main.debug)
				Bukkit.getConsoleSender().sendMessage("in gui? " + mainGUI.inGUI(player.getUniqueId()));
			nms.updateInventoryTitle(player, lang.TITLE_MAIN_GUI.replaceAll("%player%", player.getName()));
			if (Main.debug)
				nms.sendTitle(player, "still testing", "bla bla");
		}
		return b;
	}
	
	/**
	 * Load a game button from config and add a lore to it
	 *
	 * @param path: path to ConfigurationSection with settings for the button
	 * @param name: Display name of the button
	 * @param lore: lore of the button
	 * @return loaded ItemStack with lore
	 */
	private ItemStack loadButton(String path, String name, ArrayList<String> lore){
		ItemStack button = loadButton(path, name);
		ItemMeta meta = button.getItemMeta();
		meta.setLore(lore);
		button.setItemMeta(meta);
		return button;
	}
	
	
	private ItemStack loadButton(String path, String name) {
		Material mat = null;
		int data = 0;
			
		String value = config.getString(path + ".symbol");
		if(value == null) return null;
		String[] obj = value.split(":");
		
			
		if (obj.length == 2) {
			try {
				mat = Material.matchMaterial(obj[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				data = Integer.valueOf(obj[1]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		} else {
			try {
				mat = Material.matchMaterial(value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(mat == null){
			return null;
		}
		ItemStack button = new ItemStack(mat);
		if(obj.length == 2) button.setDurability((short)data);
		ItemMeta meta = button.getItemMeta();
		meta.setDisplayName(chatColor(name));
		button.setItemMeta(meta);
		
		return button;
	}
	
	
	@EventHandler
	public void onInvClick(InventoryClickEvent e) {
		if(e.getClickedInventory() == null || e.getWhoClicked() == null){
			return;
		}
		if(!(e.getWhoClicked() instanceof Player)){
			return;
		}
		Player player = (Player) e.getWhoClicked();
		UUID uuid = player.getUniqueId();
		
		if(Main.debug){
			Bukkit.getConsoleSender().sendMessage("Player in Main GUI? " + mainGUI.inGUI(uuid));
			for(IGameManager manager : registeredGames.values()) {
				Bukkit.getConsoleSender().sendMessage("Player in " + manager.toString() + " GUI? " + manager.isInGUI(uuid));
				Bukkit.getConsoleSender().sendMessage("Player in " + manager.toString() + " game? " + manager.isIngame(uuid));
			}
		}
		
		
		if(mainGUI.inGUI(uuid)){
			e.setCancelled(true);
			mainGUI.onClick(e);
			return;
		}
		for(IGameManager manager : registeredGames.values()){
			if(manager.isIngame(uuid)){
				e.setCancelled(true);
				manager.onInvClick(e);
				return;
			} else if(manager.isInGUI(uuid)){
				e.setCancelled(true);
				manager.onGUIClick(e);
				return;
			}
		}
		
	}
	
	@EventHandler
	public void onInvClose(InventoryCloseEvent e) {
		for(IGameManager manager : registeredGames.values()){
			if(manager.isIngame(e.getPlayer().getUniqueId())){
				manager.onInvClose(e);
				return;
			}
		}
		if(mainGUI.inGUI(e.getPlayer().getUniqueId())){
			mainGUI.removePlayer(e.getPlayer().getUniqueId());
		}
	}
	
	public void shutDown() {
		for(IGameManager manager : registeredGames.values()){
			manager.onDisable();
		}
		registeredGames.clear();
	}
	
	public void removeFromGUI(UUID uuid){
		mainGUI.removePlayer(uuid);
	}
	
	public ConcurrentHashMap<EnumGames, IGameManager> getRegisteredGames(){
		return this.registeredGames;
	}
	
	public Main getPlugin() {
		return this.plugin;
	}
	
	private String chatColor(String message){
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public IGui getMainGUI() {
		return mainGUI;
	}
	
	
}
