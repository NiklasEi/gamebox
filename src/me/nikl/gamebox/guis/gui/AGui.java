package me.nikl.gamebox.guis.gui;

import me.nikl.gamebox.*;
import me.nikl.gamebox.game.IGameManager;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import me.nikl.gamebox.guis.button.ToggleButton;
import me.nikl.gamebox.guis.gui.game.GameGui;
import me.nikl.gamebox.guis.gui.game.GameGuiPage;
import net.minecraft.server.v1_11_R1.PacketPlayOutSetSlot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.*;

/**
 * Created by niklas on 2/5/17.
 *
 *
 */
public abstract class AGui {
	protected Inventory inventory;
	protected Map<UUID, Inventory> openInventories = new HashMap<>();
	private ArrayList<String> permissions = new ArrayList<>();
	private GUIManager guiManager;
	private Set<UUID> inGui;
	protected GameBox plugin;
	protected PluginManager pluginManager;

	protected float volume= 0.5f, pitch = 10f;

	protected final String MAIN = "main";

	protected AButton[] grid;
	protected AButton[] lowerGrid = new AButton[36];


	private int titleMessageSeconds = 3;

	/**
	 * Constructor for a gui
	 * @param plugin plugin instance
	 * @param guiManager GUIManager instance
	 * @param slots number of slots in the inventory
	 */
	public AGui(GameBox plugin, GUIManager guiManager, int slots){
		this.plugin = plugin;
		this.guiManager = guiManager;
		this.pluginManager = plugin.getPluginManager();
		this.grid = new AButton[slots];
		inGui = new HashSet<>();
		permissions.add(Permissions.ADMIN.getPermission());

		this.inventory = Bukkit.createInventory(null, 54, "GameGUI");
	}
	
	public boolean open(Player player){
		// permissions are checked in the GUIManager
		if(openInventories.keySet().contains(player.getUniqueId())){
			player.openInventory(openInventories.get(player.getUniqueId()));
		} else {
			player.openInventory(inventory);
		}
		player.getOpenInventory().getBottomInventory().setContents(lowerGrid);
		inGui.add(player.getUniqueId());
		return true;
	}
	
	public boolean action(InventoryClickEvent event, ClickAction action, String[] args){
		
		if(GameBox.debug) Bukkit.getConsoleSender().sendMessage("action called: " + action.toString() + " with the args: " + (args == null?"": Arrays.asList(args)));
		switch (action){
			case OPEN_GAME_GUI:
				if(this instanceof GameGui){
					GameGui gui = (GameGui) this;
					if(gui.getGameID().equalsIgnoreCase(args[0]) && gui.getKey().equalsIgnoreCase(GUIManager.MAIN_GAME_GUI)){
						GameBox.debug("Already in said game gui");
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
					GameBox.debug("missing gameID to start a game");
					return false;
				}
				String gameID = args[0];
				IGameManager manager;
				if((manager = pluginManager.getGameManager(gameID)) != null){
					// set flag
					GameBox.openingNewGUI = true;
					Player[] player = new Player[1];
					player[0] = (Player) event.getWhoClicked();
					String[] stripedArgs;
					if(!event.getWhoClicked().hasPermission(Permissions.PLAY_ALL_GAMES.getPermission()) && !event.getWhoClicked().hasPermission(Permissions.PLAY_GAME.getPermission(gameID))){
						event.getWhoClicked().sendMessage(color(plugin.lang.CMD_NO_PERM));


						String currentTitle = plugin.lang.TITLE_MAIN_GUI.replace("%player%", player[0].getName());
						AGui gui = guiManager.getCurrentGui(player[0].getUniqueId());
						if(gui != null){
							if(gui instanceof GameGuiPage){
								currentTitle = ((GameGuiPage) gui).getTitle().replace("%player%", player[0].getName());
							} else if(gui instanceof  GameGui){
								currentTitle = plugin.lang.TITLE_GAME_GUI.replace("%game%", pluginManager.getGame(gameID).getName()).replace("%player%", player[0].getName());
							}
						}
						pluginManager.startTitleTimer(player[0], currentTitle, titleMessageSeconds);
						plugin.getNMS().updateInventoryTitle(player[0], plugin.lang.TITLE_NO_PERM);


						// remove flag
						GameBox.openingNewGUI = false;
						return false;
					}


					if(args.length > 1) {
						// try starting a game with args

						// first get rid of the first entry (gameID)
						stripedArgs = new String[args.length - 1];
						for (int i = 1; i < args.length; i++) {
							stripedArgs[i - 1] = args[i];
						}
						if(manager.startGame(player, stripedArgs)){
							GameBox.debug("started game "+ args[0]+" for player " + player[0].getName() + " with the arguments: " + Arrays.asList(stripedArgs));
							this.inGui.remove(player[0].getUniqueId());
							for(int slot : pluginManager.getHotBarButtons().keySet()){
								player[0].getInventory().setItem(slot, pluginManager.getHotBarButtons().get(slot));
							}
							// remove flag
							GameBox.openingNewGUI = false;
							return true;
						}

						String currentTitle = plugin.lang.TITLE_MAIN_GUI.replace("%player%", player[0].getName());
						AGui gui = guiManager.getCurrentGui(player[0].getUniqueId());
						if(gui != null){
							if(gui instanceof GameGuiPage){
								currentTitle = ((GameGuiPage) gui).getTitle().replace("%player%", player[0].getName());
							} else if(gui instanceof  GameGui){
								currentTitle = plugin.lang.TITLE_GAME_GUI.replace("%game%", pluginManager.getGame(gameID).getName()).replace("%player%", player[0].getName());
							}
						}
						pluginManager.startTitleTimer(player[0], currentTitle, titleMessageSeconds);
						plugin.getNMS().updateInventoryTitle(player[0], plugin.lang.TITLE_NOT_ENOUGH_MONEY);


						// remove flag
						GameBox.openingNewGUI = false;
						GameBox.debug("did not start a game");
						return false;

						// trie to start the game without special args
					} else {
						if(manager.startGame(player)){
							GameBox.debug("started game "+ args[0]+" for player " + player[0].getName());
							this.inGui.remove(player[0].getUniqueId());
							for(int slot : pluginManager.getHotBarButtons().keySet()){
								player[0].getInventory().setItem(slot, pluginManager.getHotBarButtons().get(slot));
							}
							// remove flag
							GameBox.openingNewGUI = false;
							return true;
						}


						String currentTitle = plugin.lang.TITLE_MAIN_GUI.replace("%player%", player[0].getName());
						AGui gui = guiManager.getCurrentGui(player[0].getUniqueId());
						if(gui != null){
							if(gui instanceof GameGuiPage){
								currentTitle = ((GameGuiPage) gui).getTitle().replace("%player%", player[0].getName());
							} else if(gui instanceof  GameGui){
								currentTitle = plugin.lang.TITLE_GAME_GUI.replace("%game%", pluginManager.getGame(gameID).getName()).replace("%player%", player[0].getName());
							}
						}
						pluginManager.startTitleTimer(player[0], currentTitle, titleMessageSeconds);
						plugin.getNMS().updateInventoryTitle(player[0], plugin.lang.TITLE_NOT_ENOUGH_MONEY);


						// remove flag
						GameBox.openingNewGUI = false;
						GameBox.debug("did not start a game");
						return false;
					}

				}
				GameBox.debug("Game with id: " + args[0] + " was not found");
				return false;
			
			case OPEN_MAIN_GUI:
				if(this instanceof MainGui) return false;
				if(guiManager.openMainGui((Player)event.getWhoClicked())){
					inGui.remove(event.getWhoClicked().getUniqueId());
					return true;
				}
				return false;


			case CHANGE_GAME_GUI:
				// ToDo
				GameBox.debug("ToDo");
				return false;


			case CLOSE:
				// do i need to do more here?
				event.getWhoClicked().closeInventory();
				((Player)event.getWhoClicked()).updateInventory();
				return true;


			case NOTHING:
				return true;

			case TOGGLE:
				if(args != null && args.length == 1){
					if(args[0].equals("sound")){
						pluginManager.getPlayer(event.getWhoClicked().getUniqueId()).toggleSound();
					}
				}
				event.getClickedInventory().setItem(event.getSlot(), ((MainGui)this).getSoundToggleButton(event.getWhoClicked().getUniqueId()).toggle());
				((Player)event.getWhoClicked()).updateInventory();
				return true;


			default:
				Bukkit.getConsoleSender().sendMessage("Missing case: "+action);
				return false;
		}
	}

	public void onInvClick(InventoryClickEvent event){
		AButton button = grid[event.getRawSlot()];
		if(button == null) return;

		if(action(event, button.getAction(), button.getArgs())){
			if(GameBox.playSounds && pluginManager.getPlayer(event.getWhoClicked().getUniqueId()).isPlaySounds()) {
				((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sounds.CLICK.bukkitSound(), volume, pitch);
			}
		} else {
			if(GameBox.playSounds && pluginManager.getPlayer(event.getWhoClicked().getUniqueId()).isPlaySounds()) {
				((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sounds.VILLAGER_NO.bukkitSound(), volume, pitch);
			}
		}
	}

	public void onInvClose(InventoryCloseEvent event){
		inGui.remove(event.getPlayer().getUniqueId());
		GameBox.debug("GUI was closed");
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

	public void setLowerButton(AButton button, int slot){
		lowerGrid[slot] = button;
	}

	public void setLowerButton(AButton button){
		int i = 0;
		while(lowerGrid[i] != null){
			i++;
		}
		setLowerButton(button, i);
	}

	private String color(String message){
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public void onBottomInvClick(InventoryClickEvent event) {
		if(lowerGrid != null && lowerGrid[event.getSlot()] != null){
			if(action(event, lowerGrid[event.getSlot()].getAction(), lowerGrid[event.getSlot()].getArgs())){
				if(GameBox.playSounds && pluginManager.getPlayer(event.getWhoClicked().getUniqueId()).isPlaySounds()) {
					((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sounds.CLICK.bukkitSound(), volume, pitch);
				}
			} else {
				((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sounds.VILLAGER_NO.bukkitSound(), volume, pitch);
			}
		}
	}

	public void addPermission(String perm){
		this.permissions.add(perm);
	}

	public void removePermission(String perm){
		this.permissions.remove(perm);
	}

	public AButton[] getLowerGrid(){
		return this.lowerGrid;
	}

	public AButton getButton(int slot){
		return grid[slot];
	}
}
