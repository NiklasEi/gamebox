package me.nikl.gamebox;

import me.nikl.gamebox.games.GameManager;
import me.nikl.gamebox.games.battleship.BattleshipManager;
import me.nikl.gamebox.games.gemcrush.GemCrushManager;
import me.nikl.gamebox.games.minesweeper.MinesweeperManager;
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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
	private Inventory mainGUI;
	
	// registered games
	private ConcurrentHashMap<Games, GameManager> registeredGames;
	
	private Map<Integer, Games> slots;
	
	private Set<UUID> inGUI;
	
	private NMSUtil nms;
	
	//close Button
	private ItemStack closeButton;
	
	public PluginManager(Main plugin){
		this.plugin = plugin;
		this.lang = plugin.lang;
		this.nms = plugin.getNms();
		this.config = plugin.getConfig();
		
		this.inGUI = new HashSet<>();
		this.slots = new HashMap<>();
		
		this.registeredGames = new ConcurrentHashMap<>();
		
		this.mainGUI = Bukkit.createInventory(null, InventoryType.CHEST, "Main GUI");
		
		this.closeButton = new ItemStack(Material.BARRIER,1);
		ItemMeta meta = closeButton.getItemMeta();
		meta.setDisplayName(chatColor(lang.BUTTON_EXIT));
		closeButton.setItemMeta(meta);
		
		mainGUI.setItem(22, closeButton);
		
		
		registerGames();
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	private void registerGames() {
		if(Main.debug) Bukkit.getConsoleSender().sendMessage("registering games");
		int slot = 0;
		if(!config.isConfigurationSection("games")) return;
		ConfigurationSection gamesSection = config.getConfigurationSection("games");
		for(Games game : Games.values()){
			if(!gamesSection.getBoolean(game.toString() + ".enabled")){
				Bukkit.getConsoleSender().sendMessage(Main.plainPrefix + "the game " + game.toString() + " is disabled");
				continue;
			}
			switch (game.toString()) {
				case "minesweeper":
					registeredGames.put(Games.MINESWEEPER, new MinesweeperManager(plugin));
					mainGUI.setItem(slot, loadButton("games." + game, "&1Minesweeper"));
					slots.put(slot, Games.MINESWEEPER);
					slot++;
					break;
				case "gemcrush":
					registeredGames.put(Games.GEMCRUSH, new GemCrushManager(plugin));
					mainGUI.setItem(slot, loadButton("games." + game, "&1GemCrush"));
					slots.put(slot, Games.GEMCRUSH);
					slot++;
					break;
				case "battleship":
					registeredGames.put(Games.BATTLESHIP, new BattleshipManager(plugin));
					ArrayList<String> testLore = new ArrayList<>();
					testLore.add(chatColor("&1Testing the lore"));
					testLore.add(chatColor("&aTest row 2"));
					mainGUI.setItem(slot, loadButton("games." + game, "&1Battleship", testLore));
					slots.put(slot, Games.BATTLESHIP);
					slot++;
					break;
				default:
					break;
			}
		}
	}
	
	/***
	 * open the main AGui for a player
	 * add him to the set of players in the AGui and update the title from language
	 *
	 * @param player this players inventory will be opened
	 */
	public void openGUI(Player player){
		player.openInventory(mainGUI);
		nms.updateInventoryTitle(player, chatColor(lang.TITLE_MAIN_GUI.replaceAll("%player%", player.getName())));
		inGUI.add(player.getUniqueId());
		if(Main.debug)nms.sendTitle(player, "still testing", "bla bla");
	}
	
	/**
	 * Load a game button from config and add a lore to it
	 *
	 * @param path path to ConfigurationSection with settings for the button
	 * @param name Display name of the button
	 * @param lore lore of the button
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
				
			}
			
			try {
				data = Integer.valueOf(obj[1]);
			} catch (NumberFormatException e) {
				
			}
		} else {
			try {
				mat = Material.matchMaterial(value);
			} catch (Exception e) {
				
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
		
		if(inGUI.contains(uuid)){
			e.setCancelled(true);
			if(slots.containsKey(e.getSlot())){
				if(Main.debug) player.sendMessage(" Game clicked: " + slots.get(e.getSlot()));
				inGUI.remove(uuid);
				registeredGames.get(slots.get(e.getSlot())).openGameGUI(player);
			} else if(e.getCurrentItem() != null && e.getCurrentItem().getType() == closeButton.getType()){
				inGUI.remove(uuid);
				player.closeInventory();
			}
			return;
		}
		for(GameManager manager : registeredGames.values()){
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
	
	private boolean isPlayer(UUID uuid) {
		for(GameManager gManager : registeredGames.values()){
			if(gManager.isIngame(uuid)) return true;
		}
		return false;
	}
	
	@EventHandler
	public void onInvClose(InventoryCloseEvent e) {
		for(GameManager manager : registeredGames.values()){
			if(manager.isIngame(e.getPlayer().getUniqueId())){
				manager.onInvClose(e);
			}
		}
	}
	
	public void shutDown() {
		for(GameManager manager : registeredGames.values()){
			manager.onDisable();
		}
	}
	
	private String chatColor(String message){
		return ChatColor.translateAlternateColorCodes('&', message);
	}
}
