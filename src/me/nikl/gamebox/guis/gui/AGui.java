package me.nikl.gamebox.guis.gui;

import me.nikl.gamebox.ClickAction;
import me.nikl.gamebox.Main;
import me.nikl.gamebox.Permissions;
import me.nikl.gamebox.game.IGameManager;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created by niklas on 2/5/17.
 *
 *
 */
public abstract class AGui {
	protected Inventory inventory;
	private String permission;
	private GUIManager guiManager;
	private Set<UUID> inGui;
	protected Main plugin;

	protected final String MAIN = "main";

	private AButton[] grid;
	private AButton[] lowerGrid = new AButton[36];

	/**
	 * Constructor for a gui
	 * @param plugin plugin instance
	 * @param guiManager GUIManager instance
	 * @param slots number of slots in the inventory
	 */
	public AGui(Main plugin, GUIManager guiManager, int slots){
		this.plugin = plugin;
		this.guiManager = guiManager;
		this.grid = new AButton[slots];
		inGui = new HashSet<>();

		this.inventory = Bukkit.createInventory(null, 54, "GameGUI");
	}
	
	public boolean open(Player player){
		if(permission != null && !player.hasPermission(permission) && !player.hasPermission(Permissions.ADMIN.getPermission())){
			return false;
		}
		player.openInventory(inventory);
		inGui.add(player.getUniqueId());
		return true;
	}
	
	public boolean action(InventoryClickEvent event, ClickAction action, String[] args){
		
		if(Main.debug) Bukkit.getConsoleSender().sendMessage("action called: " + action.toString() + " with the args: " + (args == null?"": args));
		switch (action){
			case OPEN_GAME_GUI:
				if(this instanceof GameGui){
					GameGui gui = (GameGui) this;
					if(gui.getGameID().equalsIgnoreCase(args[0]) && gui.getKey().equalsIgnoreCase(GUIManager.MAIN_GAME_GUI)){
						Main.debug("Already in said game gui");
						return false;
					}
				}
				if(guiManager.openGameGui((Player)event.getWhoClicked(), args[0], GUIManager.MAIN_GAME_GUI)){
					inGui.remove(event.getWhoClicked().getUniqueId());
					return true;
				}
				return false;
			
			case START_GAME:
				if(args == null || args.length < 1){
					Main.debug("missing gameID to start a game");
					return false;
				}
				String gameID = args[0];
				IGameManager manager;
				if((manager = plugin.getPluginManager().getGameManager(gameID)) != null){
					Player[] player = new Player[1];
					player[0] = (Player) event.getWhoClicked();
					String[] stripedArgs;
					if(!event.getWhoClicked().hasPermission(Permissions.PLAY_ALL_GAMES.getPermission()) && !event.getWhoClicked().hasPermission(Permissions.PLAY_GAME.getPermission(gameID))){
						event.getWhoClicked().sendMessage(color(plugin.lang.CMD_NO_PERM));
						return false;
					}
					if(args.length > 1) {
						stripedArgs = new String[args.length - 1];
						for (int i = 1; i < args.length; i++) {
							stripedArgs[i - 1] = args[1];
						}
						if(manager.startGame(player, stripedArgs)){
							Main.debug("started game "+ args[0]+" for player " + player[0].getName() + " with the arguments: " + stripedArgs.toString());
							this.inGui.remove(player[0].getUniqueId());
							return true;
						}
					} else {
						if(manager.startGame(player)){
							Main.debug("started game "+ args[0]+" for player " + player[0].getName());
							this.inGui.remove(player[0].getUniqueId());
							return true;
						}
					}

				}
				Main.debug("Game with id: " + args[0] + " was not found");
				return false;
			
			case OPEN_MAIN_GUI:
				if(this instanceof MainGui) return false;
				if(guiManager.openMainGui((Player)event.getWhoClicked())){
					inGui.remove(event.getWhoClicked().getUniqueId());
					return true;
				}
				return false;
				
			case NOTHING:
				return true;
				
			default:
				Bukkit.getLogger().log(Level.WARNING, "not valid action called in gui: " + action.toString());
				return false;
		}
	}

	public void onInvClick(InventoryClickEvent event){
		AButton button = grid[event.getRawSlot()];
		if(button == null) return;

		if(event.getSlot() == event.getRawSlot()) {
			action(event, button.getAction(), button.getArgs());
		} else {
			// click in the players inventory
			onPlayerInvClick(event);
		}
	}

	private void onPlayerInvClick(InventoryClickEvent event) {

	}

	public void onInvClose(InventoryCloseEvent event){
		inGui.remove(event.getPlayer().getUniqueId());
		Main.debug("GUI was closed");
	}

	public boolean isInGui(UUID uuid){
		return inGui.contains(uuid);
	}

	public boolean isInGui(Player uuid){
		return inGui.contains(uuid.getUniqueId());
	}

	public void setButton(AButton button, int slot){
		grid[slot] = button;
		this.inventory.setItem(slot, button);
	}

	public void setButton(AButton button){
		int i = 0;
		while(grid[i] != null){
			i++;
		}
		setButton(button, i);
	}

	private String color(String message){
		return ChatColor.translateAlternateColorCodes('&', message);
	}
}
