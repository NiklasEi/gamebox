package me.nikl.gamebox.guis.gui;

import me.nikl.gamebox.*;
import me.nikl.gamebox.game.IGameManager;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import me.nikl.gamebox.guis.gui.game.GameGui;
import me.nikl.gamebox.guis.gui.game.StartMultiplayerGamePage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Level;

/**
 * Created by niklas on 2/5/17.
 *
 *
 */
public abstract class AGui {
	protected Inventory inventory;
	protected Map<UUID, Inventory> openInventories = new HashMap<>();
	private ArrayList<String> permissions = new ArrayList<>();
	protected GUIManager guiManager;
	protected Set<UUID> inGui;
	protected GameBox plugin;
	protected PluginManager pluginManager;

	protected float volume= 0.5f, pitch = 10f;

	protected final String MAIN = "main";

	protected AButton[] grid;
	protected AButton[] lowerGrid = new AButton[36];


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

		this.inventory = Bukkit.createInventory(null, slots, "This title should not show!");
	}

	public AGui(GameBox plugin, GUIManager guiManager, InventoryType inventoryType){
		this.plugin = plugin;
		this.guiManager = guiManager;
		this.pluginManager = plugin.getPluginManager();
		this.grid = new AButton[inventoryType.getDefaultSize()];
		inGui = new HashSet<>();
		permissions.add(Permissions.ADMIN.getPermission());

		this.inventory = Bukkit.createInventory(null, inventoryType, "GameGUI");
	}
	
	public boolean open(Player player){
		GameBox.debug("opening gui (method open in AGui)");
		// permissions are checked in the GUIManager
		if(openInventories.keySet().contains(player.getUniqueId())){
			GameBox.debug("found and now opening own inventory");
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
					Player[] player = args.length == 3?new Player[2]:new Player[1];
					player[0] = (Player) event.getWhoClicked();

					if(!event.getWhoClicked().hasPermission(Permissions.PLAY_ALL_GAMES.getPermission()) && !event.getWhoClicked().hasPermission(Permissions.PLAY_SPECIFIC_GAME.getPermission(gameID))){
						//event.getWhoClicked().sendMessage(plugin.lang.CMD_NO_PERM);

						guiManager.sentInventoryTitleMessage(player[0], plugin.lang.TITLE_NO_PERM, gameID);

						// remove flag
						GameBox.openingNewGUI = false;
						return false;
					}

					if(args.length == 3){
						// last entry should be a UUID
						try{
							UUID uuid = UUID.fromString(args[2]);
							Player player2 = Bukkit.getPlayer(uuid);
							if(player == null) return false;
							player[1] = player2;
						} catch (IllegalArgumentException exception){
							exception.printStackTrace();
							GameBox.debug("tried inviting with a not valid UUID");
							return false;
						}

						if(pluginManager.isInGame(player[1].getUniqueId())){
							guiManager.sentInventoryTitleMessage(player[0], plugin.lang.TITLE_ALREADY_IN_ANOTHER_GAME, gameID);
							return false;
						}
						if(!guiManager.isInGUI(player[1].getUniqueId())){
							pluginManager.saveInventory(player[1]);
						}
					}


					int returnedCode = manager.startGame(player, (GameBox.playSounds && pluginManager.getPlayer(player[0].getUniqueId()).isPlaySounds()), args[1]);
					if(returnedCode == GameBox.GAME_STARTED){
						GameBox.debug("started game "+ args[0]+" for player " + player[0].getName() + (player.length==2?" and " + player[1].getName():"") + " with the arguments: " + Arrays.asList(args));
						for(Player playerObj : player) {
							this.inGui.remove(playerObj.getUniqueId());
							for (int slot : pluginManager.getHotBarButtons().keySet()) {
								playerObj.getInventory().setItem(slot, pluginManager.getHotBarButtons().get(slot));
							}
						}
						// remove flag
						GameBox.openingNewGUI = false;
						return true;
					} else if(returnedCode == GameBox.GAME_NOT_ENOUGH_MONEY){
						guiManager.sentInventoryTitleMessage(player[0], plugin.lang.TITLE_NOT_ENOUGH_MONEY, gameID);
					} else if(returnedCode == GameBox.GAME_NOT_ENOUGH_MONEY_1){
						guiManager.sentInventoryTitleMessage(player[0], plugin.lang.TITLE_NOT_ENOUGH_MONEY, gameID);

						if(args.length == 3){
							if(guiManager.isInGUI(player[1].getUniqueId())) {
								guiManager.sentInventoryTitleMessage(player[1], plugin.lang.TITLE_OTHER_PLAYER_NOT_ENOUGH_MONEY, gameID);
							} else {
								//ToDo
								player[1].sendMessage(player[0].getName()+" Does not have enough money to start the game!");
							}
						}
					} else if(returnedCode == GameBox.GAME_NOT_ENOUGH_MONEY_2){
						if(args.length == 3){
							if(guiManager.isInGUI(player[1].getUniqueId())) {
								guiManager.sentInventoryTitleMessage(player[1], plugin.lang.TITLE_NOT_ENOUGH_MONEY, gameID);
							} else {
								//ToDo
								player[1].sendMessage(player[0].getName()+" tried starting a game with you. But you do not have enough money!");
							}
						}
						guiManager.sentInventoryTitleMessage(player[0], plugin.lang.TITLE_OTHER_PLAYER_NOT_ENOUGH_MONEY, gameID);
					} else if(returnedCode == GameBox.GAME_NOT_STARTED_ERROR){
						for(Player playerObj: player) {
							if(player.length > 1 && guiManager.isInGUI(player[1].getUniqueId())) {
								guiManager.sentInventoryTitleMessage(playerObj, plugin.lang.TITLE_ERROR, gameID);
							} else {
								playerObj.sendMessage("A game failed to start");
							}
						}
					}

					// remove flag
					GameBox.openingNewGUI = false;

					for(Player playerObj: player) {
						if (!guiManager.isInGUI(playerObj.getUniqueId()) && !pluginManager.isInGame(playerObj.getUniqueId())) {
							pluginManager.restoreInventory(playerObj);
							playerObj.updateInventory();
						}
					}
					GameBox.debug("did not start a game");
					return false;

					// try to start the game without special args

				}
				GameBox.debug("Game with id: " + args[0] + " was not found");
				return false;
			
			case OPEN_MAIN_GUI:
				if(this instanceof MainGui) return false;
				GameBox.debug("guimanager = null? " + String.valueOf(guiManager == null));
				if(guiManager.openMainGui((Player)event.getWhoClicked())){
					inGui.remove(event.getWhoClicked().getUniqueId());
					return true;
				}
				return false;


			case CHANGE_GAME_GUI:
				if(guiManager.openGameGui((Player)event.getWhoClicked(), args)){
					inGui.remove(event.getWhoClicked().getUniqueId());
					return true;
				}
				return false;


			case CLOSE:
				// do i need to do more here?
				event.getWhoClicked().closeInventory();
				((Player)event.getWhoClicked()).updateInventory();
				return true;


			case NOTHING:
				return true;

			case START_PLAYER_INPUT:
				long timeStamp = System.currentTimeMillis();
				boolean worked = pluginManager.getHandleInviteInput().addWaiting(event.getWhoClicked().getUniqueId(), timeStamp + GameBox.timeForPlayerInput*1000, args);
				if(worked){
					event.getWhoClicked().closeInventory();
					((Player)event.getWhoClicked()).updateInventory();
					event.getWhoClicked().sendMessage(plugin.lang.PREFIX + plugin.lang.INPUT_START_MESSAGE);
					for(String message : plugin.lang.INPUT_HELP_MESSAGE){
						event.getWhoClicked().sendMessage(message.replace("%seconds%", String.valueOf(GameBox.timeForPlayerInput)));
					}
					return true;
				}
				return false;

			case TOGGLE:
				if(args != null && args.length == 1){
					if(args[0].equals("sound")){
						pluginManager.getPlayer(event.getWhoClicked().getUniqueId()).toggleSound();
					}
				}
				event.getInventory().setItem(event.getSlot(), ((MainGui)this).getSoundToggleButton(event.getWhoClicked().getUniqueId()).toggle());
				((Player)event.getWhoClicked()).updateInventory();
				return true;

			case SHOW_TOP_LIST:
				if(args.length != 2){
					Bukkit.getLogger().log(Level.WARNING, "show top list click has the wrong number of arguments: " + args.length);
					return false;
				}

				if(guiManager.openGameGui((Player) event.getWhoClicked(),args)){
					inGui.remove(event.getWhoClicked().getUniqueId());
					return true;
				}
				return false;

			case OPEN_SHOP_PAGE:
				if(args.length != 2){
					Bukkit.getLogger().log(Level.WARNING, "OPEN_SHOP_PAGE has the wrong number of arguments: " + args.length);
					return false;
				}
				if(guiManager.openShopPage((Player)event.getWhoClicked(), args)){
					inGui.remove(event.getWhoClicked().getUniqueId());
					return true;
				}

				return false;

			case BUY:

				// check for closed shop
				if(guiManager.getShopManager().isClosed()){
					guiManager.sentInventoryTitleMessage((Player)event.getWhoClicked(), plugin.lang.SHOP_IS_CLOSED, null);
					return false;
				}

				// check whether player can pay
				int tokens, money;
				try{
					tokens = Integer.parseInt(args[2]);
					money = Integer.parseInt(args[3]);
				} catch (NumberFormatException exception){
					exception.printStackTrace();
					Bukkit.getLogger().log(Level.WARNING, "a shop item had wrong cost-info args: " + Arrays.asList(args));
					return false;
				}

				int hasToken = 0;
				if(tokens > 0 ){
					if((hasToken = pluginManager.getPlayer(event.getWhoClicked().getUniqueId()).getTokens()) < tokens){
						guiManager.sentInventoryTitleMessage((Player)event.getWhoClicked(), plugin.lang.SHOP_TITLE_NOT_ENOUGH_TOKEN, null);
						return false;
					}
				}
				if(money > 0){
					if(!plugin.getEconEnabled() || GameBox.econ.getBalance((OfflinePlayer) event.getWhoClicked()) < money){
						guiManager.sentInventoryTitleMessage((Player)event.getWhoClicked(), plugin.lang.SHOP_TITLE_NOT_ENOUGH_MONEY, null);
						return false;
					}
				}

				ItemStack item = guiManager.getShopManager().getShopItem(args[0], args[1]).clone();
				if(item == null) return false;


				if(!pluginManager.addItem(event.getWhoClicked().getUniqueId(), item)){
					guiManager.sentInventoryTitleMessage((Player)event.getWhoClicked(), plugin.lang.SHOP_TITLE_INVENTORY_FULL, null);
					return false;
				} else {
					guiManager.sentInventoryTitleMessage((Player)event.getWhoClicked(), plugin.lang.SHOP_TITLE_BOUGHT_SUCCESSFULLY, null);
				}

				if(tokens > 0 ){
					pluginManager.getPlayer(event.getWhoClicked().getUniqueId()).setTokens(hasToken - tokens);
				}
				if(money > 0){
					GameBox.econ.withdrawPlayer((OfflinePlayer) event.getWhoClicked(), money);
				}

				return true;

			default:
				Bukkit.getConsoleSender().sendMessage("Missing case: "+action);
				return false;
		}
	}

	public void onInvClick(InventoryClickEvent event){
		if(event.getCurrentItem() == null) return;
		AButton button = grid[event.getRawSlot()];
		boolean perInvitation = false;
		StartMultiplayerGamePage mpGui = null;
		if(button == null){
			if(event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR){
				if(guiManager.getCurrentGui(event.getWhoClicked().getUniqueId()) instanceof StartMultiplayerGamePage){
					mpGui = (StartMultiplayerGamePage)guiManager.getCurrentGui(event.getWhoClicked().getUniqueId());
					button = mpGui.getButton(event.getWhoClicked().getUniqueId(), event.getSlot());
					if(button == null) return;
					perInvitation = true;
				} else {
					return;
				}
			} else {
				return;
			}
		}

		if(action(event, button.getAction(), button.getArgs())){
			if(GameBox.playSounds && pluginManager.getPlayer(event.getWhoClicked().getUniqueId()).isPlaySounds() && button.getAction() != ClickAction.NOTHING) {
				((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sounds.CLICK.bukkitSound(), volume, pitch);
			}
			if(perInvitation){
				mpGui.removeInvite( UUID.fromString(button.getArgs()[2]), event.getWhoClicked().getUniqueId());
			}
		} else {
			if(GameBox.playSounds && pluginManager.getPlayer(event.getWhoClicked().getUniqueId()).isPlaySounds() && button.getAction() != ClickAction.NOTHING) {
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

	public void removePlayer(UUID uuid){
		openInventories.remove(uuid);
	}
}
