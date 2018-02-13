package me.nikl.gamebox;

import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.events.LeftGameBoxEvent;
import me.nikl.gamebox.games.Game;
import me.nikl.gamebox.games.GameManager;
import me.nikl.gamebox.inventory.GUIManager;
import me.nikl.gamebox.inventory.timer.TitleTimer;
import me.nikl.gamebox.input.HandleInvitations;
import me.nikl.gamebox.input.HandleInviteInput;
import me.nikl.gamebox.nms.NMSUtil;
import me.nikl.gamebox.utility.ItemStackUtility;
import me.nikl.gamebox.utility.Permission;
import me.nikl.gamebox.utility.Sound;
import me.nikl.gamebox.utility.StringUtility;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
public class PluginManager implements Listener {

    // count the number of registered games
    public static int gamesRegistered = 0;

	// GameBox instance
	private GameBox plugin;

	// GameBoxLanguage
	private GameBoxLanguage lang;

	// plugin configuration
	private FileConfiguration config;
	
	private NMSUtil nms;
	
	private GUIManager guiManager;

	// save and manage players that we are waiting for to invite someone in the chat
	private HandleInviteInput handleInviteInput;
	// save and handle invitations
    private HandleInvitations handleInvitations;

	private Map<String, Game> games = new HashMap<>();

	// save the players inventory contents
	private Map<UUID, ItemStack[]> savedContents  = new HashMap<>();
    private Map<UUID, Integer> hotBarSlot  = new HashMap<>();

    // timer to reset the title after a title message
    private Map<UUID, TitleTimer> titleTimers  = new HashMap<>();

    // players
    private Map<UUID, GBPlayer> gbPlayers = new HashMap<>();

    // hot bar stuff
	public static int exitButtonSlot = 4;
    public static int toMainButtonSlot = 0;
    public static int toGameButtonSlot = 8;
    public static int emptyHotBarSlotToHold = 1;
    private static List<Integer> slotsToKeep = new ArrayList<>();
	private Map<Integer, ItemStack> hotbarButtons = new HashMap<>();

	// list of disabled worlds
	private List<String> blockedWorlds = new ArrayList<>();

	// hub stuff
    private boolean setOnWorldJoin;
    private ItemStack hubItem;
    private List<String> hubWorlds;
    private int hubItemSlot;

    //sounds
    private float volume = 0.5f, pitch= 10f;

	public PluginManager(GameBox plugin){
		this.plugin = plugin;
		this.lang = plugin.lang;
		this.nms = plugin.getNMS();
		this.config = plugin.getConfig();

		if(config.isList("settings.blockedWorlds")){
		    blockedWorlds = new ArrayList<>(config.getStringList("settings.blockedWorlds"));
        }

        setHotBar();

		if(GameBoxSettings.hubModeEnabled) getHub();

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}



    public void loadPlayers() {
	    for(Player player : Bukkit.getOnlinePlayers()){
            if(!blockedWorlds.contains(player.getLocation().getWorld().getName())){
                gbPlayers.putIfAbsent(player.getUniqueId(), new GBPlayer(plugin, player.getUniqueId()));
            }
        }
    }

    private void setHotBar() {
        exitButtonSlot = (config.getInt("guiSettings.hotBarNavigation.exitSlot", 4) >= 0 && config.getInt("guiSettings.hotBarNavigation.exitSlot", 4) < 9)? config.getInt("guiSettings.hotBarNavigation.exitSlot", 4):4;
        toMainButtonSlot = (config.getInt("guiSettings.hotBarNavigation.mainMenuSlot", 0) >= 0 && config.getInt("guiSettings.hotBarNavigation.mainMenuSlot", 0) < 9)? config.getInt("guiSettings.hotBarNavigation.mainMenuSlot", 0):0;
        toGameButtonSlot = (config.getInt("guiSettings.hotBarNavigation.gameMenuSlot", 8) >= 0 && config.getInt("guiSettings.hotBarNavigation.gameMenuSlot", 8) < 9)? config.getInt("guiSettings.hotBarNavigation.gameMenuSlot", 8):8;

        if(config.getInt("guiSettings.hotBarNavigation.exitSlot") < 0){
            plugin.getLogger().log(Level.WARNING, " The exit button is disabled!");
            exitButtonSlot = -999;
        }
        if(config.getInt("guiSettings.hotBarNavigation.mainMenuSlot") < 0){
            plugin.getLogger().log(Level.WARNING, " The back-to-main-menu button is disabled!");
            toMainButtonSlot = -999;
        }
        if(config.getInt("guiSettings.hotBarNavigation.gameMenuSlot") < 0){
            plugin.getLogger().log(Level.WARNING, " The back-to-game-menu button is disabled!");
            toGameButtonSlot = -999;
        }

        ItemStack toMainItem = ItemStackUtility.getItemStack(config.getString("guiSettings.hotBarNavigation.mainMenuMaterial"))
                , toGameItem = ItemStackUtility.getItemStack(config.getString("guiSettings.hotBarNavigation.gameMenuMaterial"))
                , exitItem = ItemStackUtility.getItemStack(config.getString("guiSettings.hotBarNavigation.exitMaterial"));

        if(toMainItem == null) {
            toMainItem = new ItemStack(Material.DARK_OAK_DOOR_ITEM);
            if(config.isString("guiSettings.hotBarNavigation.mainMenuMaterial"))
                plugin.getLogger().log(Level.WARNING, " guiSettings.hotBarNavigation.mainMenuMaterial is not a valid material");
        }
        if(toGameItem == null) {
            toGameItem = new ItemStack(Material.BIRCH_DOOR_ITEM);
            if(config.isString("guiSettings.hotBarNavigation.gameMenuMaterial"))
                plugin.getLogger().log(Level.WARNING, " guiSettings.hotBarNavigation.gameMenuMaterial is not a valid material");
        }
        if(exitItem == null) {
            exitItem = new ItemStack(Material.BARRIER);
            if(config.isString("guiSettings.hotBarNavigation.exitMaterial"))
                plugin.getLogger().log(Level.WARNING, " guiSettings.hotBarNavigation.exitMaterial is not a valid material");
        }

        // set count
        toGameItem.setAmount(1); toMainItem.setAmount(1); exitItem.setAmount(1);

        // set display name
        ItemMeta meta = toMainItem.getItemMeta(); meta.setDisplayName(StringUtility.color(lang.BUTTON_TO_MAIN_MENU)); toMainItem.setItemMeta(meta);
        meta = toGameItem.getItemMeta(); meta.setDisplayName(StringUtility.color(lang.BUTTON_TO_GAME_MENU)); toGameItem.setItemMeta(meta);
        meta = exitItem.getItemMeta(); meta.setDisplayName(StringUtility.color(lang.BUTTON_EXIT)); exitItem.setItemMeta(meta);

        if(toMainButtonSlot >= 0)hotbarButtons.put(toMainButtonSlot, toMainItem);
        if(exitButtonSlot >= 0)hotbarButtons.put(exitButtonSlot, exitItem);
        if(toGameButtonSlot >= 0)hotbarButtons.put(toGameButtonSlot, toGameItem);


        // load special hot bar slots which keep their items
        if(config.isSet("guiSettings.keepItemsSlots") && config.isList("guiSettings.keepItemsSlots")){
            slotsToKeep = config.getIntegerList("guiSettings.keepItemsSlots");
        }
        if(slotsToKeep == null) slotsToKeep = new ArrayList<>();

        // do not use the slots that are already taken by a navigation button
        Iterator<Integer> it = slotsToKeep.iterator();

        while(it.hasNext()){
            int slot = it.next();
            if(slot == toMainButtonSlot || slot == exitButtonSlot || slot == toGameButtonSlot) it.remove();
            if(slot < 0 || slot > 8) it.remove();
        }

        // try finding an empty hubItemSlot to hold while in GUI/game
        if(hotbarButtons.values().size() + slotsToKeep.size() < 9){
            while(emptyHotBarSlotToHold == exitButtonSlot || emptyHotBarSlotToHold == toMainButtonSlot || emptyHotBarSlotToHold == toGameButtonSlot || slotsToKeep.contains(emptyHotBarSlotToHold)){
                emptyHotBarSlotToHold++;
            }
        } else {
            while(emptyHotBarSlotToHold == exitButtonSlot || emptyHotBarSlotToHold == toMainButtonSlot || emptyHotBarSlotToHold == toGameButtonSlot){
                emptyHotBarSlotToHold++;
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
	    if(blockedWorlds.contains(event.getPlayer().getLocation().getWorld().getName())) return;
	    handleInviteInput.onChat(event);
    }

    private void getHub() {
        ConfigurationSection hubSec = config.getConfigurationSection("hubMode");
        if(!hubSec.isString("item.materialData") || !hubSec.isString("item.displayName")){
            Bukkit.getLogger().log(Level.WARNING, " missing configuration in the 'hubMode' section");
            GameBoxSettings.hubModeEnabled = false;
            return;
        }
        String matString = hubSec.getString("item.materialData");
        String[] matStrings = matString.split(":");
        Material mat = Material.getMaterial(matStrings[0]);
        if(mat == null){
            Bukkit.getLogger().log(Level.WARNING, " invalid material in the 'hubMode' section");
            GameBoxSettings.hubModeEnabled = false;
            return;
        }
        hubItem = new ItemStack(mat);
        if(matStrings.length == 2) {
            try {
                Short.parseShort(matStrings[1]);
            } catch (NumberFormatException exception) {
                exception.printStackTrace();
                GameBoxSettings.hubModeEnabled = false;
                return;
            }
            hubItem.setDurability(Short.parseShort(matStrings[1]));
        }
        ItemMeta meta = hubItem.getItemMeta();
        meta.setDisplayName(StringUtility.color(hubSec.getString("item.displayName")));
        if(hubSec.isList("item.lore")){
            meta.setLore(StringUtility.color(hubSec.getStringList("item.lore")));
        }
        hubItem.setItemMeta(meta);
        hubWorlds = new ArrayList<>(hubSec.getStringList("enabledWorlds"));
        hubItemSlot = hubSec.getInt("slot", 0);
        setOnWorldJoin = hubSec.getBoolean("giveItemOnWorldJoin", false);
    }

    @SuppressWarnings("deprecation")
    public void saveInventory(Player player){
		GameBox.debug("saving inventory contents...");

		// save currently hold item slot
        hotBarSlot.putIfAbsent(player.getUniqueId(), player.getInventory().getHeldItemSlot());

        // save inventory contents
		savedContents.putIfAbsent(player.getUniqueId(), player.getInventory().getContents().clone());

        if(GameBoxSettings.keepArmorWhileInGame){
            ItemStack[] content = savedContents.get(player.getUniqueId()).clone();

            // remove all non armor items from array. This works for newer versions (with shields) and old ones
            for (int i = 0; i < 36; i++){
                content[i] = null;
            }

            // overwrite all non armor items
            player.getInventory().setContents(content);
        } else {
            player.getInventory().clear();
        }

        // player will hold an empty slot, which removes the annoying animation when clicking in an inventory with an item in your hand
		player.getInventory().setHeldItemSlot(emptyHotBarSlotToHold);
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

        // in 1.8 there is a short delay necessary to display the restored inventory contents.
        if(GameBoxSettings.version1_8) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.updateInventory();
                }
            }.runTaskLater(plugin, 1);
        } else {
            player.updateInventory();
        }

		savedContents.remove(player.getUniqueId());
        hotBarSlot.remove(player.getUniqueId());
        new LeftGameBoxEvent(player);
	}

	public boolean hasSavedContents(UUID uuid){
		return savedContents.containsKey(uuid);
	}
	
	@EventHandler
	public void onInvClick(InventoryClickEvent event) {
		if(event.getSlot() < 0 || event.getInventory() == null || event.getWhoClicked() == null){
            return;
        }
        if(!(event.getWhoClicked() instanceof Player)){
            return;
        }

		UUID uuid = event.getWhoClicked().getUniqueId();


		GameBox.debug("checking gameManagers     clicked inv has " + event.getInventory().getSize() + " slots");

		for(String gameID : games.keySet()){
            GameManager gameManager = games.get(gameID).getGameManager();
			if(gameManager.isInGame(uuid)){
				event.setCancelled(true);
				if((event.getRawSlot() - event.getSlot()) < event.getView().getTopInventory().getSize()){
				    // click in the top or in the upper bottom inventory
                    // always handel in the game
                    GameBox.debug("click in top or middle inventory");
				    gameManager.onInventoryClick(event);
                } else {
				    if(games.get(gameID).getSettings().isHandleClicksOnHotbar()){
                        gameManager.onInventoryClick(event);
                    } else {
				        if(event.getView().getBottomInventory().getItem(event.getSlot()) == null){
				            GameBox.debug("empty hotbar slot clicked... returning");
				            return;
                        }
                        if(event.getSlot() == toGameButtonSlot){
                            gameManager.removeFromGame(event.getWhoClicked().getUniqueId());
                            guiManager.openGameGui((Player) event.getWhoClicked(), gameID, GUIManager.MAIN_GAME_GUI);
                            if(GameBoxSettings.playSounds && getPlayer(event.getWhoClicked().getUniqueId()).isPlaySounds()) {
                                ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.CLICK.bukkitSound(), volume, pitch);
                            }
                            return;
                        } else if(event.getSlot() == toMainButtonSlot){
                            gameManager.removeFromGame(event.getWhoClicked().getUniqueId());
                            guiManager.openMainGui((Player) event.getWhoClicked());
                            if(GameBoxSettings.playSounds && getPlayer(event.getWhoClicked().getUniqueId()).isPlaySounds()) {
                                ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.CLICK.bukkitSound(), volume, pitch);
                            }
                        } else if(event.getSlot() == exitButtonSlot){
                            event.getWhoClicked().closeInventory();
                            ((Player)event.getWhoClicked()).updateInventory();
                            if(GameBoxSettings.playSounds && getPlayer(event.getWhoClicked().getUniqueId()).isPlaySounds()) {
                                ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.CLICK.bukkitSound(), volume, pitch);
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


		for(Game game: games.values()){
		    GameManager manager = game.getGameManager();
			if(manager.isInGame(uuid)){
				if(manager.onInventoryClose(event) && !manager.isInGame(uuid)){
					restoreInventory((Player) event.getPlayer());
				}
				return;
			}
		}


		guiManager.onInvClose(event);
	}

	@EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event){
        if(!blockedWorlds.contains(event.getPlayer().getLocation().getWorld().getName())){
            if(!gbPlayers.containsKey(event.getPlayer().getUniqueId())){
                gbPlayers.put(event.getPlayer().getUniqueId(), new GBPlayer(plugin, event.getPlayer().getUniqueId()));
            }

            // hub stuff
            if(GameBoxSettings.hubModeEnabled && hubWorlds.contains(event.getPlayer().getLocation().getWorld().getName()) && setOnWorldJoin){
                GameBox.debug("in the hub world!");
                giveHubItem(event.getPlayer());
            }
        } else {
            if(gbPlayers.containsKey(event.getPlayer().getUniqueId())){
                removePlayer(event.getPlayer().getUniqueId());
            }
        }
    }

    private void giveHubItem(Player player) {
        Inventory inv = player.getInventory();
        // check the player inventory for the hubItem
        for(int i = 0; i < inv.getSize(); i++){
            if(inv.getItem(i) == null) continue;
            if(inv.getItem(i).isSimilar(hubItem)){
                GameBox.debug("found hub item in slot " + i);
                return;
            }
        }
        // item not found!
        // check the configured hubItemSlot and put it there if it is empty
        if(inv.getItem(hubItemSlot) == null || inv.getItem(hubItemSlot).getType() == Material.AIR){
            player.getInventory().setItem(hubItemSlot, hubItem);
        } else { // it's not empty so try to add it to the inventory
            if(!inv.addItem(hubItem).isEmpty()){
                // no space for the hubItem found...
                player.sendMessage(lang.PREFIX + " Failed to give you the hub item (Full inventory)");
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        if(!blockedWorlds.contains(event.getPlayer().getLocation().getWorld().getName())){
            gbPlayers.putIfAbsent(event.getPlayer().getUniqueId(), new GBPlayer(plugin, event.getPlayer().getUniqueId()));
        }
        if(GameBoxSettings.hubModeEnabled && hubWorlds.contains(event.getPlayer().getLocation().getWorld().getName()) && setOnWorldJoin){
            GameBox.debug("in the hub world!");
            giveHubItem(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        Player player = event.getPlayer();

        // close inventory when in a game or GUI. This should trigger InventoryCloseEvent
        if(isInGame(player.getUniqueId()) || guiManager.isInGUI(player.getUniqueId())
                || guiManager.getShopManager().inShop(player.getUniqueId())){
            player.closeInventory();
            restoreInventory(player);
        }

        // remove the player and all the personal GUIs. This also saves the GB options of that player.
        if(gbPlayers.keySet().contains(player.getUniqueId())) {
            removePlayer(event.getPlayer().getUniqueId());
        }
    }

    /**
     * Prevent players that had their inventories saved and cleaned from
     * picking up items to the GUI or to games.
     *
     * It there is space in the saved inventory the item is added.
     * Otherwise the PickUpEvent is cancelled.
     * @param playerPickupItemEvent called Event
     */
    @EventHandler
    public void onPickUp(PlayerPickupItemEvent playerPickupItemEvent){
        if(!isInGame(playerPickupItemEvent.getPlayer().getUniqueId()) && !guiManager.isInGUI(playerPickupItemEvent.getPlayer().getUniqueId()) && !guiManager.getShopManager().inShop(playerPickupItemEvent.getPlayer().getUniqueId())) return;

        // ToDo: change #addItem() and this method to allow for partial pick up
        if(addItem(playerPickupItemEvent.getPlayer().getUniqueId(), playerPickupItemEvent.getItem().getItemStack())){
            playerPickupItemEvent.getItem().remove();
        }

        playerPickupItemEvent.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event){
        if(isInGame(event.getEntity().getUniqueId()) || guiManager.isInGUI(event.getEntity().getUniqueId()) || guiManager.getShopManager().inShop(event.getEntity().getUniqueId())){
            plugin.getLogger().log(Level.SEVERE, " Player in-game in death event!");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(EntityDamageEvent event){
        if(!(event.getEntity() instanceof  Player)) return;

        if(guiManager == null ) {
            Bukkit.getConsoleSender().sendMessage(" The plugin dit not start correctly. Please check for previous errors!");
            return;
        }

        // if player is in gui or in game close the inventory
        // this should fire before death event and thus will fix problems with drops on death
        if((GameBoxSettings.closeInventoryOnDamage
                || event.getFinalDamage() >= ((Player) event.getEntity()).getHealth())
                && (isInGame(event.getEntity().getUniqueId())
                || guiManager.isInGUI(event.getEntity().getUniqueId())
                || guiManager.getShopManager().inShop(event.getEntity().getUniqueId()))){
            ((Player)event.getEntity()).closeInventory();
        }
    }

    public void removePlayer(UUID uuid){
        gbPlayers.get(uuid).remove();
        gbPlayers.remove(uuid);
    }

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent event){
        if(!GameBoxSettings.hubModeEnabled) return;
        if(event.getItem() == null || event.getItem().getType() != hubItem.getType() || event.getItem().getItemMeta() == null || event.getItem().getItemMeta().getDisplayName() == null) return;
        if(event.getItem().getItemMeta().getDisplayName().equals(hubItem.getItemMeta().getDisplayName())){
            event.setCancelled(true);
            if(hubWorlds.contains(event.getPlayer().getLocation().getWorld().getName())) {
                if(event.getPlayer().hasPermission(Permission.USE.getPermission())) {
                    guiManager.openMainGui(event.getPlayer());
                } else {
                    event.getPlayer().sendMessage(lang.PREFIX + lang.CMD_NO_PERM);
                }
            } else {
                event.getPlayer().sendMessage(lang.PREFIX + lang.CMD_DISABLED_WORLD);
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
                continue;
            }
            if(guiManager.getShopManager().inShop(player.getUniqueId())){
                player.closeInventory();
                restoreInventory(player);
            }
        }

        for(Game game : games.values()){
            GameBox.debug("disabling " + game.getGameLang().DEFAULT_PLAIN_NAME);
            game.onDisable();
        }

        gamesRegistered = 0;

        logging:
        if(savedContents.size() > 0){
            Bukkit.getLogger().log(Level.SEVERE, "-------------------------------------------------------------------");
            Bukkit.getLogger().log(Level.SEVERE, "There were left-over inventories after restoring for all players");
            String fileName = LocalDateTime.now().toString();
            // get rid of the milliseconds
            // the name now only includes the date and time
            fileName = fileName.split("\\.")[0];
            fileName = fileName.replace(":", "_");
            fileName += ".txt";
            Bukkit.getLogger().log(Level.SEVERE, "Saving those contents in a log file in the folder Logs as: " + fileName);

            File logFile = new File(plugin.getDataFolder().toString() + File.separatorChar + "Logs" + File.separatorChar + fileName);

            logFile.getParentFile().mkdirs();
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileConfiguration log;

            try {
                log = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(logFile), "UTF-8"));
            } catch (UnsupportedEncodingException | FileNotFoundException e) {
                e.printStackTrace();
                break logging;
            }
            ItemStack[] saves;
            for(UUID uuid: savedContents.keySet()){
                saves = savedContents.get(uuid);
                log.set(uuid.toString() + ".name", Bukkit.getOfflinePlayer(uuid) == null ? "null":Bukkit.getOfflinePlayer(uuid).getName());
                for(int i = 0; i< saves.length;i++){
                    if(saves[i] != null)log.set(uuid.toString() + ".items." + i, saves[i].toString());
                }
            }
            savedContents.clear();
            try {
                log.save(logFile);
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Could not save Log", e);
            }
            Bukkit.getLogger().log(Level.SEVERE, "-------------------------------------------------------------------");
        }

		for(GBPlayer player : gbPlayers.values()){
            player.remove();
        }
        gbPlayers.clear();
	}
	
	public GameBox getPlugin() {
		return this.plugin;
	}

    /**
     * Register a new game with GameBox
     *
     * @param game Game instance to register
     */
    public void registerGame(Game game){
        games.put(game.getGameID(), game);
        Permission.addGameID(game.getGameID());
        gamesRegistered ++;
    }

    public GameManager getGameManager(String gameID){
        Game game = getGame(gameID);
        return game == null ? null : game.getGameManager();
    }

	public GameManager getGameManager(Module module){
		return getGameManager(module.getModuleID());
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


    private GameManager getGameManager(UUID uuid){
        GameManager manager;
        for(String gameID : games.keySet()){
            manager = getGameManager(gameID);
            if(manager.isInGame(uuid))
                return manager;
        }
        return null;
    }

    public GBPlayer getPlayer(UUID uuid){
        return gbPlayers.get(uuid);
    }

    public boolean isInGame(UUID uuid){
        for(String gameID : games.keySet()){
            if(getGameManager(gameID).isInGame(uuid))
                return true;
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

    /**
     * Tries to add an itemStack to the players inventory
     * @param uuid Player
     * @param itemStack Item
     * @return item successfully given
     */
    public boolean addItem(UUID uuid, ItemStack itemStack){
        if(!savedContents.keySet().contains(uuid) || itemStack == null) return false;

        // map for the possibilities to fill already existing stacks up
        List<Integer> fillUpPossibilities = new ArrayList<>();

        GameBox.debug("trying to add an item...");
        ItemStack[] savedStacks = savedContents.get(uuid);
        for(int i = 0; i< 36;i++){
            if(savedStacks[i] == null) continue;
            if(savedStacks[i].isSimilar(itemStack)
                    && ((itemStack.getItemMeta() == null && savedStacks[i].getItemMeta() == null)
                    || (itemStack.getItemMeta().getDisplayName() == null && savedStacks[i].getItemMeta().getDisplayName() == null)
                    || itemStack.getItemMeta().getDisplayName().equals(savedStacks[i].getItemMeta().getDisplayName()))){
                if(itemStack.getMaxStackSize() >= itemStack.getAmount() + savedStacks[i].getAmount()){
                    savedStacks[i].setAmount(itemStack.getAmount() + savedStacks[i].getAmount());
                    if(!fillUpPossibilities.isEmpty()){
                        for(int slot : fillUpPossibilities){
                            savedStacks[slot].setAmount(itemStack.getMaxStackSize());
                        }
                    }
                    return true;
                } else if(itemStack.getMaxStackSize() > savedStacks[i].getAmount()){
                    int rest = itemStack.getAmount() - (itemStack.getMaxStackSize() - savedStacks[i].getAmount());
                    fillUpPossibilities.add(i);
                    itemStack.setAmount(rest);
                    continue;
                }
            }
        }
        for(int i = 0; i< 36;i++){
            if(savedStacks[i] == null){
                savedStacks[i] = itemStack;
                if(!fillUpPossibilities.isEmpty()){
                    for(int slot : fillUpPossibilities){
                        savedStacks[slot].setAmount(itemStack.getMaxStackSize());
                    }
                }
                return true;
            }
        }
        GameBox.debug("   Failed!");
        return false;
    }

    public List<String > getBlockedWorlds(){
        return this.blockedWorlds;
    }

    public boolean wonTokens(UUID player, int tokens, String gameID) {
        GBPlayer gbPlayer = gbPlayers.get(player);
        if(gbPlayer == null) return false;

        gbPlayer.setTokens(gbPlayer.getTokens() + tokens);
        Bukkit.getPlayer(player).sendMessage(lang.PREFIX + lang.WON_TOKEN
                .replace("%tokens%", String.valueOf(tokens))
                .replace("%game%", getGame(gameID).getGameLang().PLAIN_NAME));
        return true;
    }

    public void setItemsToKeep(Player player) {
        if(!savedContents.containsKey(player.getUniqueId())) return;
        GameBox.debug("setting the items to keep: " + slotsToKeep);
        for(int slot : slotsToKeep) {
            player.getInventory().setItem(slot, savedContents.get(player.getUniqueId())[slot]);
        }
    }

    public Map<String, Game> getGames(){
        return this.games;
    }

    public Game getGame(Module module) {
        return getGame(module.getModuleID());
    }

    public Game getGame(String gameID){
        return games.get(gameID);
    }

    public Game getGame(UUID uuid){
        for(Game game : games.values()){
            if(game.getGameManager().isInGame(uuid))
                return game;
        }
        return null;
    }

    public void addGame(Game game) {
        games.put(game.getGameID(), game);
    }

    /**
     * Read only!
     * @return players
     */
    public Map<UUID, GBPlayer> getGbPlayers(){
        return Collections.unmodifiableMap(this.gbPlayers);
    }
}
