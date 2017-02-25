package me.nikl.gamebox;

import me.nikl.gamebox.game.GameContainer;
import me.nikl.gamebox.game.IGameManager;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.timer.TitleTimer;
import me.nikl.gamebox.nms.NMSUtil;
import me.nikl.gamebox.players.GBPlayer;
import me.nikl.gamebox.players.HandleInvitations;
import me.nikl.gamebox.players.HandleInviteInput;
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
import org.bukkit.event.player.*;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created by niklas on 10/17/16.
 *
 * register all games
 * Clicks are managed here
 * Check what GUI is open for the player and then pass the click event on
 */
public class PluginManager implements Listener{
	
	// GameBox instance
	private GameBox plugin;

	// Language
	private Language lang;

	// plugin configuration
	private FileConfiguration config;
	
	private NMSUtil nms;
	
	private GUIManager guiManager;

	// save and manage players that we are waiting for to invite someone in the chat
	private HandleInviteInput handleInviteInput;
	// save and handle invitations
    private HandleInvitations handleInvitations;

	private Map<String, GameContainer> games = new HashMap<>();

	// save the players inventory contents
	private Map<UUID, ItemStack[]> savedContents  = new HashMap<>();
    private Map<UUID, Integer> hotBarSlot  = new HashMap<>();

    // timer to reset the title after a title message
    private Map<UUID, TitleTimer> titleTimers  = new HashMap<>();

    // players
    private Map<UUID, GBPlayer> gbPlayers = new HashMap<>();

    // hot bar stuff
	public static int exit, toMain, toGame, toHold = 0;
	private Map<Integer, ItemStack> hotbarButtons = new HashMap<>();

	// list of disabled worlds
	private ArrayList<String> disabledWorlds = new ArrayList<>();

	// hub stuff
    private boolean hub, setOnWorldJoin;
    private ItemStack hubItem;
    private ArrayList<String> hubWorlds;
    private int slot;

    //sounds
    private float volume = 0.5f, pitch= 10f;
	
	public PluginManager(GameBox plugin){
		this.plugin = plugin;
		this.lang = plugin.lang;
		this.nms = plugin.getNMS();
		this.config = plugin.getConfig();

		if(config.isList("blockedWorlds")){
		    disabledWorlds = new ArrayList<>(config.getStringList("blockedWorlds"));
        }

        setHotBar();

		hub = config.getBoolean("hubMode.enabled", false);
		if(hub) getHub();

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

    public void loadPlayers() {
	    for(Player player : Bukkit.getOnlinePlayers()){
            if(!disabledWorlds.contains(player.getLocation().getWorld().getName())){
                gbPlayers.putIfAbsent(player.getUniqueId(), new GBPlayer(plugin, player.getUniqueId()));
            }
        }
    }

    private void setHotBar() {
        exit = (config.getInt("guiSettings.hotBarNavigation.exitSlot", 4) >= 0 && config.getInt("guiSettings.hotBarNavigation.exitSlot", 4) < 9)? config.getInt("guiSettings.hotBarNavigation.exitSlot", 4):4;
        toMain = (config.getInt("guiSettings.hotBarNavigation.mainMenuSlot", 0) >= 0 && config.getInt("guiSettings.hotBarNavigation.mainMenuSlot", 0) < 9)? config.getInt("guiSettings.hotBarNavigation.mainMenuSlot", 0):0;
        toGame = (config.getInt("guiSettings.hotBarNavigation.gameMenuSlot", 8) >= 0 && config.getInt("guiSettings.hotBarNavigation.gameMenuSlot", 8) < 9)? config.getInt("guiSettings.hotBarNavigation.gameMenuSlot", 8):8;
        while(toHold == exit || toHold == toMain  || toHold == toGame ){
            toHold++;
        }


        ItemStack toMainItem = new ItemStack(Material.DARK_OAK_DOOR_ITEM), toGameItem = new ItemStack(Material.BIRCH_DOOR_ITEM), exitItem = new ItemStack(Material.BARRIER);
        ItemMeta meta = toMainItem.getItemMeta(); meta.setDisplayName(chatColor(lang.BUTTON_TO_MAIN_MENU)); toMainItem.setItemMeta(meta);
        meta = toGameItem.getItemMeta(); meta.setDisplayName(chatColor(lang.BUTTON_TO_GAME_MENU)); toGameItem.setItemMeta(meta);
        meta = exitItem.getItemMeta(); meta.setDisplayName(chatColor(lang.BUTTON_EXIT)); exitItem.setItemMeta(meta);
        hotbarButtons.put(toMain, toMainItem); hotbarButtons.put(exit, exitItem); hotbarButtons.put(toGame, toGameItem);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
	    if(disabledWorlds.contains(event.getPlayer().getLocation().getWorld().getName())) return;
	    handleInviteInput.onChat(event);
    }

    private void getHub() {
        ConfigurationSection hubSec = config.getConfigurationSection("hubMode");
        if(!hubSec.isString("item.materialData") || !hubSec.isString("item.displayName")){
            Bukkit.getLogger().log(Level.WARNING, " missing configuration in the 'hubMode' section");
            hub = false;
            return;
        }
        String matString = hubSec.getString("item.materialData");
        String[] matStrings = matString.split(":");
        Material mat = Material.getMaterial(matStrings[0]);
        if(mat == null){
            Bukkit.getLogger().log(Level.WARNING, " invalid material in the 'hubMode' section");
            hub = false;
            return;
        }
        hubItem = new ItemStack(mat);
        if(matStrings.length == 2) {
            try {
                Short.parseShort(matStrings[1]);
            } catch (NumberFormatException exception) {
                exception.printStackTrace();
                hub = false;
                return;
            }
            hubItem.setDurability(Short.parseShort(matStrings[1]));
        }
        ItemMeta meta = hubItem.getItemMeta();
        meta.setDisplayName(chatColor(hubSec.getString("item.displayName")));
        ArrayList<String> lore;
        if(hubSec.isList("item.lore")){
            lore = new ArrayList<>(hubSec.getStringList("item.lore"));
            for(int i = 0; i < lore.size();i++){
                lore.set(i, chatColor(lore.get(i)));
            }
            meta.setLore(lore);
        }
        hubItem.setItemMeta(meta);
        hubWorlds = new ArrayList<>(hubSec.getStringList("enabledWorlds"));
        slot = hubSec.getInt("slot", 1);
        setOnWorldJoin = hubSec.getBoolean("giveItemOnWorldJoin", false);
    }

    public void saveInventory(Player player){
		GameBox.debug("saving inventory contents...");
        hotBarSlot.putIfAbsent(player.getUniqueId(), player.getInventory().getHeldItemSlot());
		savedContents.putIfAbsent(player.getUniqueId(), player.getInventory().getContents().clone());

		player.getInventory().clear();
		player.getInventory().setHeldItemSlot(toHold);
	}

	public void restoreInventory(Player player){
		if(!savedContents.containsKey(player.getUniqueId())) return;
		if(GameBox.openingNewGUI){
			GameBox.debug("not restoring, because a new gui is being opened...");
			return;
		}
		GameBox.debug("restoring inventory contents...");
		player.getInventory().setContents(savedContents.get(player.getUniqueId()));
        player.getInventory().setHeldItemSlot(hotBarSlot.get(player.getUniqueId()));
		savedContents.remove(player.getUniqueId());
        hotBarSlot.remove(player.getUniqueId());
	}

	public boolean hasSavedContents(UUID uuid){
		return savedContents.containsKey(uuid);
	}
	
	@EventHandler
	public void onInvClick(InventoryClickEvent event) {
		if(event.getClickedInventory() == null || event.getWhoClicked() == null){
            return;
        }
        if(!(event.getWhoClicked() instanceof Player)){
            return;
        }

		UUID uuid = event.getWhoClicked().getUniqueId();


		GameBox.debug("checking gameManagers");
		boolean topInv = event.getRawSlot() == event.getSlot();
		for(String gameID: games.keySet()){
            IGameManager gameManager = games.get(gameID).getGameManager();
			if(gameManager.isInGame(uuid)){
				event.setCancelled(true);
				if(topInv || event.getSlot() > 8){
				    // click in the top or in the upper bottom inventory
                    // always handel in the game
				    gameManager.onInventoryClick(event);
                } else {
				    if(games.get(gameID).handleClicksOnHotbar()){
                        gameManager.onInventoryClick(event);
                    } else {
				        if(event.getClickedInventory().getItem(event.getSlot()) == null){
				            GameBox.debug("empty hotbar slot clicked... returning");
				            return;
                        }
                        if(event.getSlot() == this.toGame){
                            guiManager.openGameGui((Player) event.getWhoClicked(), gameID, GUIManager.MAIN_GAME_GUI);
                            gameManager.removeFromGame(event.getWhoClicked().getUniqueId());
                            if(GameBox.playSounds && getPlayer(event.getWhoClicked().getUniqueId()).isPlaySounds()) {
                                ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sounds.CLICK.bukkitSound(), volume, pitch);
                            }
                            return;
                        } else if(event.getSlot() == this.toMain){
                            guiManager.openMainGui((Player) event.getWhoClicked());
                            gameManager.removeFromGame(event.getWhoClicked().getUniqueId());
                            if(GameBox.playSounds && getPlayer(event.getWhoClicked().getUniqueId()).isPlaySounds()) {
                                ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sounds.CLICK.bukkitSound(), volume, pitch);
                            }
                        } else if(event.getSlot() == this.exit){
                            event.getWhoClicked().closeInventory();
                            ((Player)event.getWhoClicked()).updateInventory();
                            if(GameBox.playSounds && getPlayer(event.getWhoClicked().getUniqueId()).isPlaySounds()) {
                                ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sounds.CLICK.bukkitSound(), volume, pitch);
                            }
                            return;
                        }
                    }
                }


				//if(!topInv)guiManager.onInGameBottomInvClick(event, gameID);
				return;
			}
		}
		GameBox.debug("none found... checking GUIs...");
		guiManager.onInvClick(event);
	}
	
	@EventHandler
	public void onInvClose(InventoryCloseEvent event) {
		if(!(event.getPlayer() instanceof Player)) return;
		removeTitleTimer(event.getPlayer().getUniqueId());
		if(GameBox.openingNewGUI){
			GameBox.debug("ignoring close because of flag: GameBox.openingNewGUI");
			return;
		}
		UUID uuid = event.getPlayer().getUniqueId();


		for(GameContainer game: games.values()){
		    IGameManager manager = game.getGameManager();
			if(manager.isInGame(uuid)){
                manager.onInventoryClose(event);
				if(!manager.isInGame(uuid)){
					restoreInventory((Player) event.getPlayer());
					// update to actually display stuff like shield and armor
                    ((Player)event.getPlayer()).updateInventory();
				}
				return;
			}
		}


		guiManager.onInvClose(event);
	}

	@EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event){
        if(!disabledWorlds.contains(event.getPlayer().getLocation().getWorld().getName())){
            gbPlayers.putIfAbsent(event.getPlayer().getUniqueId(), new GBPlayer(plugin, event.getPlayer().getUniqueId()));
        } else {
            removePlayer(event.getPlayer().getUniqueId());
        }
	    if(hubWorlds.contains(event.getPlayer().getLocation().getWorld().getName())){
            if(hub && setOnWorldJoin) {
                GameBox.debug("in the hub world!");
                event.getPlayer().getInventory().setItem(slot, hubItem);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        if(!disabledWorlds.contains(event.getPlayer().getLocation().getWorld().getName())){
            gbPlayers.putIfAbsent(event.getPlayer().getUniqueId(), new GBPlayer(plugin, event.getPlayer().getUniqueId()));
        }
        if(hubWorlds.contains(event.getPlayer().getLocation().getWorld().getName())){
            if(hub && setOnWorldJoin) {
                GameBox.debug("in the hub world!");
                event.getPlayer().getInventory().setItem(slot, hubItem);
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        if(gbPlayers.keySet().contains(event.getPlayer().getUniqueId())) {
            removePlayer(event.getPlayer().getUniqueId());
        }
    }

    public void removePlayer(UUID uuid){
        gbPlayers.get(uuid).remove();
        gbPlayers.remove(uuid);
    }

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent event){
        if(!hub) return;
        if(hubWorlds.contains(event.getPlayer().getLocation().getWorld().getName())) {
            if(event.getItem() == null || event.getItem().getType() != hubItem.getType() || event.getItem().getItemMeta() == null || event.getItem().getItemMeta().getDisplayName() == null) return;
            if(event.getItem().getItemMeta().getDisplayName().equals(hubItem.getItemMeta().getDisplayName())){
                event.setCancelled(true);
                guiManager.openMainGui(event.getPlayer());
            }
        }
    }

	public void shutDown() {
        for(Player player : Bukkit.getOnlinePlayers()){
            if(isInGame(player.getUniqueId())){
                player.closeInventory();
                restoreInventory(player);
                continue;
            }
            if(guiManager.isInGUI(player.getUniqueId())){
                player.closeInventory();
                restoreInventory(player);
            }
        }
        if(savedContents.size() > 0){
            Bukkit.getLogger().log(Level.SEVERE, "There were left-over inventories after restoring for all players");
        }
		for(GBPlayer player : gbPlayers.values()){
            player.remove();
        }
        gbPlayers.clear();
	}
	
	public GameBox getPlugin() {
		return this.plugin;
	}
	
	private String chatColor(String message){
		return ChatColor.translateAlternateColorCodes('&', message);
	}


    public void registerGame(IGameManager gameManager, String gameID, String gameName, int playerNum){
	    registerGame(gameManager, gameID, gameName, playerNum, false);
    }

	public void registerGame(IGameManager gameManager, String gameID, String gameName, int playerNum, boolean handleClicksOnHotbar){
        GameContainer game = new GameContainer(gameID, gameManager);
        game.setHandleClicksOnHotbar(handleClicksOnHotbar);
        game.setName(gameName);
        game.setPlainName(ChatColor.stripColor(gameName));
        game.setPlayerNum(playerNum);
		games.put(gameID, game);
		Permissions.addGameID(gameID);
	}

	public IGameManager getGameManager(String gameID){
		return games.get(gameID).getGameManager();
	}

	public GUIManager getGuiManager(){
		return this.guiManager;
	}

	public Map<Integer, ItemStack> getHotBarButtons(){
	    return this.hotbarButtons;
    }

    public void setGuiManager(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    public GameContainer getGame(String gameID){
	    return games.get(gameID);
    }

    public GameContainer getGame(UUID uuid){
        for(String gameID : games.keySet()){
            if(games.get(gameID).getGameManager().isInGame(uuid)) return games.get(gameID);
        }
        return null;
    }


    public void startTitleTimer(Player player, String title, int seconds){
        UUID uuid = player.getUniqueId();
        if(titleTimers.keySet().contains(uuid)){
            titleTimers.get(uuid).cancel();
        }
        titleTimers.put(uuid, new TitleTimer(plugin, title, player, System.currentTimeMillis()+seconds*1000));
    }

    public void removeTitleTimer(UUID uuid) {
        if(titleTimers.keySet().contains(uuid)){
            titleTimers.get(uuid).cancel();
            titleTimers.remove(uuid);
        }
    }


    private IGameManager getGameManager(UUID uuid){
        for(String gameID : games.keySet()){
            if(games.get(gameID).getGameManager().isInGame(uuid)) return games.get(gameID).getGameManager();
        }
        return null;
    }

    public GBPlayer getPlayer(UUID uuid){
        return gbPlayers.get(uuid);
    }

    public boolean isInGame(UUID uuid){
        for(String gameID : games.keySet()){
            if(games.get(gameID).getGameManager().isInGame(uuid)) return true;
        }
        return false;
    }

    public HandleInviteInput getHandleInviteInput() {
        return handleInviteInput;
    }

    public void setHandleInviteInput(HandleInviteInput handleInviteInput) {
        this.handleInviteInput = handleInviteInput;
    }

    public HandleInvitations getHandleInvitations() {
        return handleInvitations;
    }

    public void setHandleInvitations(HandleInvitations handleInvitations) {
        this.handleInvitations = handleInvitations;
    }

    public boolean addItem(UUID uuid, ItemStack itemStack){
        if(!savedContents.keySet().contains(uuid) || itemStack == null) return false;
        GameBox.debug("length is: " + savedContents.get(uuid).length);
        ItemStack[] savedStacks = savedContents.get(uuid);
        for(int i = 0; i< 36;i++){
            if(savedStacks[i] == null) continue;
            if(savedStacks[i].isSimilar(itemStack) && ((itemStack.getItemMeta().getDisplayName() == null && savedStacks[i].getItemMeta().getDisplayName() == null) || itemStack.getItemMeta().getDisplayName().equals(savedStacks[i].getItemMeta().getDisplayName()))){
                if(itemStack.getMaxStackSize() >= itemStack.getAmount() + savedStacks[i].getAmount()){
                    savedStacks[i].setAmount(itemStack.getAmount() + savedStacks[i].getAmount());
                    return true;
                } else if(itemStack.getMaxStackSize() >= savedStacks[i].getAmount()){
                    int rest = itemStack.getAmount() - (itemStack.getMaxStackSize() - savedStacks[i].getAmount());
                    savedStacks[i].setAmount(itemStack.getMaxStackSize());
                    itemStack.setAmount(rest);
                    continue;
                }
            }
        }
        for(int i = 0; i< 36;i++){
            if(savedStacks[i] == null){
                savedStacks[i] = itemStack;
                return true;
            }
        }
        return false;
    }
}
