package me.nikl.gamebox;

import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.events.EnterGameBoxEvent;
import me.nikl.gamebox.events.LeftGameBoxEvent;
import me.nikl.gamebox.game.Game;
import me.nikl.gamebox.game.manager.GameManager;
import me.nikl.gamebox.input.InvitationHandler;
import me.nikl.gamebox.input.InviteInputHandler;
import me.nikl.gamebox.inventory.GuiManager;
import me.nikl.gamebox.inventory.GameBoxHolder;
import me.nikl.gamebox.inventory.gui.AGui;
import me.nikl.gamebox.module.GameBoxGame;
import me.nikl.gamebox.utility.ItemStackUtility;
import me.nikl.gamebox.utility.Permission;
import me.nikl.gamebox.utility.Sound;
import me.nikl.gamebox.utility.StringUtility;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author Niklas Eicker
 * <p>
 * register all games
 * Clicks are managed here
 * Check what GUI is open for the player and then pass the click event on
 */
public class PluginManager implements Listener {

  // count the number of registered games
  public static int gamesRegistered = 0;
  private GameBox plugin;
  private GameBoxLanguage lang;
  private FileConfiguration config;
  private GuiManager guiManager;
  private InviteInputHandler inviteInputHandler;
  private InvitationHandler invitationHandler;
  private Map<String, Game> games = new HashMap<>();
  private Map<UUID, ItemStack[]> savedContents = new HashMap<>();
  private Map<UUID, Integer> hotBarSlot = new HashMap<>();
  private Map<UUID, GBPlayer> gbPlayers = new HashMap<>();
  private Map<Integer, ItemStack> hotbarButtons = new HashMap<>();
  private List<String> blockedWorlds = new ArrayList<>();
  private boolean setOnWorldJoin;
  private ItemStack hubItem;
  private List<String> hubWorlds;
  private int hubItemSlot;
  private float volume = 0.5f, pitch = 10f;

  public PluginManager(GameBox plugin) {
    this.plugin = plugin;
    this.lang = plugin.lang;
    this.config = plugin.getConfig();
    if (config.isList("settings.blockedWorlds")) {
      blockedWorlds = new ArrayList<>(config.getStringList("settings.blockedWorlds"));
    }
    setHotBar();
    if (GameBoxSettings.hubModeEnabled) getHub();
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }


  public void loadPlayers() {
    for (World world : Bukkit.getWorlds()) {
      if (blockedWorlds.contains(world.getName())) continue;
      for (Player player : world.getPlayers()) {
        loadPlayer(player.getUniqueId());
      }
    }
  }

  public void loadPlayer(UUID uniqueId) {
    GameBox.debug("loading gb player: " + uniqueId);
    gbPlayers.putIfAbsent(uniqueId, new GBPlayer(plugin, uniqueId));
  }

  private void setHotBar() {
    ItemStack toMainItem = ItemStackUtility.getItemStack(config.getString("guiSettings.hotBarNavigation.mainMenuMaterial")), toGameItem = ItemStackUtility.getItemStack(config.getString("guiSettings.hotBarNavigation.gameMenuMaterial")), exitItem = ItemStackUtility.getItemStack(config.getString("guiSettings.hotBarNavigation.exitMaterial"));

    if (toMainItem == null) {
      toMainItem = new ItemStack(ItemStackUtility.DARK_OAK_DOOR);
      if (config.isString("guiSettings.hotBarNavigation.mainMenuMaterial"))
        plugin.getLogger().log(Level.WARNING, " guiSettings.hotBarNavigation.mainMenuMaterial is not a valid material");
    }
    if (toGameItem == null) {
      toGameItem = new ItemStack(ItemStackUtility.BIRCH_DOOR);
      if (config.isString("guiSettings.hotBarNavigation.gameMenuMaterial"))
        plugin.getLogger().log(Level.WARNING, " guiSettings.hotBarNavigation.gameMenuMaterial is not a valid material");
    }
    if (exitItem == null) {
      exitItem = new ItemStack(ItemStackUtility.BARRIER);
      if (config.isString("guiSettings.hotBarNavigation.exitMaterial"))
        plugin.getLogger().log(Level.WARNING, " guiSettings.hotBarNavigation.exitMaterial is not a valid material");
    }

    // set count
    toGameItem.setAmount(1);
    toMainItem.setAmount(1);
    exitItem.setAmount(1);

    // set display name
    ItemMeta meta = toMainItem.getItemMeta();
    meta.setDisplayName(StringUtility.color(lang.BUTTON_TO_MAIN_MENU));
    toMainItem.setItemMeta(meta);
    meta = toGameItem.getItemMeta();
    meta.setDisplayName(StringUtility.color(lang.BUTTON_TO_GAME_MENU));
    toGameItem.setItemMeta(meta);
    meta = exitItem.getItemMeta();
    meta.setDisplayName(StringUtility.color(lang.BUTTON_EXIT));
    exitItem.setItemMeta(meta);

    if (GameBoxSettings.toMainButtonSlot >= 0)
      hotbarButtons.put(GameBoxSettings.toMainButtonSlot, toMainItem);
    if (GameBoxSettings.exitButtonSlot >= 0)
      hotbarButtons.put(GameBoxSettings.exitButtonSlot, exitItem);
    if (GameBoxSettings.toGameButtonSlot >= 0)
      hotbarButtons.put(GameBoxSettings.toGameButtonSlot, toGameItem);
  }

  @EventHandler
  public void onChat(AsyncPlayerChatEvent event) {
    if (blockedWorlds.contains(event.getPlayer().getLocation().getWorld().getName())) return;
    inviteInputHandler.onChat(event);
  }

  private void getHub() {
    ConfigurationSection hubSec = config.getConfigurationSection("hubMode");
    if (!hubSec.isString("item.materialData") || !hubSec.isString("item.displayName")) {
      Bukkit.getLogger().log(Level.WARNING, " missing configuration in the 'hubMode' section");
      GameBoxSettings.hubModeEnabled = false;
      return;
    }
    hubItem = ItemStackUtility.getItemStack(hubSec.getString("item.materialData", "CHEST"));
    if (hubItem == null) {
      Bukkit.getLogger().log(Level.WARNING, " invalid material in the 'hubMode' section");
      GameBoxSettings.hubModeEnabled = false;
      return;
    }
    ItemMeta meta = hubItem.getItemMeta();
    meta.setDisplayName(StringUtility.color(hubSec.getString("item.displayName")));
    if (hubSec.isList("item.lore")) {
      meta.setLore(StringUtility.color(hubSec.getStringList("item.lore")));
    }
    hubItem.setItemMeta(meta);
    hubWorlds = new ArrayList<>(hubSec.getStringList("enabledWorlds"));
    hubItemSlot = hubSec.getInt("slot", 0);
    setOnWorldJoin = hubSec.getBoolean("giveItemOnWorldJoin", false);
  }

  public void saveInventory(Player player) {
    GameBox.debug("saving inventory contents...");
    hotBarSlot.putIfAbsent(player.getUniqueId(), player.getInventory().getHeldItemSlot());
    savedContents.putIfAbsent(player.getUniqueId(), player.getInventory().getContents().clone());
    if (GameBoxSettings.keepArmorWhileInGame) {
      ItemStack[] content = savedContents.get(player.getUniqueId()).clone();
      // remove all non armor items from array. This works for newer versions (with shields) and old ones
      for (int i = 0; i < 36; i++) {
        content[i] = null;
      }
      player.getInventory().setContents(content);
    } else {
      player.getInventory().clear();
    }
    player.getInventory().setHeldItemSlot(GameBoxSettings.emptyHotBarSlotToHold);
  }

  public void restoreInventory(Player player) {
    if (!savedContents.containsKey(player.getUniqueId())) return;
    if (GameBox.openingNewGUI) {
      GameBox.debug("not restoring, because a new gui is being opened...");
      return;
    }
    GameBox.debug("restoring inventory contents...");
    player.getInventory().setContents(savedContents.get(player.getUniqueId()));
    player.getInventory().setHeldItemSlot(hotBarSlot.get(player.getUniqueId()));

    savedContents.remove(player.getUniqueId());
    hotBarSlot.remove(player.getUniqueId());
  }

  public boolean doesNotHaveSavedContents(UUID uuid) {
    return !savedContents.containsKey(uuid);
  }

  @EventHandler
  public void onInvClick(InventoryClickEvent event) {
    if (event.getSlot() < 0 || event.getInventory().getHolder() == null) {
      return;
    }
    if (!(event.getWhoClicked() instanceof Player)) {
      return;
    }
    GameBox.debug("checking gameManagers     clicked inv has " + event.getInventory().getSize() + " slots");
    if (event.getInventory().getHolder() instanceof GameManager) {
      GameBox.debug("found Game manager");
      GameManager gameManager = (GameManager) event.getInventory().getHolder();
      event.setCancelled(true);
      if ((event.getRawSlot() - event.getSlot()) < event.getView().getTopInventory().getSize()) {
        // click in the top or in the upper bottom inventory
        // always handel in the game
        GameBox.debug("click in top or middle inventory");
        gameManager.onInventoryClick(event);
      } else {
        Game game = getGame(event.getWhoClicked().getUniqueId());
        if (game.getSettings().isHandleClicksOnHotbar()) {
          gameManager.onInventoryClick(event);
        } else {
          if (event.getView().getBottomInventory().getItem(event.getSlot()) == null) {
            GameBox.debug("empty hotbar slot clicked... returning");
            return;
          }
          if (event.getSlot() == GameBoxSettings.toGameButtonSlot) {
            gameManager.removeFromGame(event.getWhoClicked().getUniqueId());
            guiManager.openGameGui((Player) event.getWhoClicked(), game.getGameID(), GuiManager.MAIN_GAME_GUI);
            if (GameBoxSettings.playSounds && getPlayer(event.getWhoClicked().getUniqueId()).isPlaySounds()) {
              ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.CLICK.bukkitSound(), volume, pitch);
            }
            return;
          } else if (event.getSlot() == GameBoxSettings.toMainButtonSlot) {
            gameManager.removeFromGame(event.getWhoClicked().getUniqueId());
            guiManager.openMainGui((Player) event.getWhoClicked());
            if (GameBoxSettings.playSounds && getPlayer(event.getWhoClicked().getUniqueId()).isPlaySounds()) {
              ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.CLICK.bukkitSound(), volume, pitch);
            }
          } else if (event.getSlot() == GameBoxSettings.exitButtonSlot) {
            event.getWhoClicked().closeInventory();
            //noinspection deprecation Todo: remove, when alternative given
            ((Player) event.getWhoClicked()).updateInventory();
            if (GameBoxSettings.playSounds && getPlayer(event.getWhoClicked().getUniqueId()).isPlaySounds()) {
              ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.CLICK.bukkitSound(), volume, pitch);
            }
            return;
          }
        }
        return;
      }
      return;
    }
    if (event.getInventory().getHolder() instanceof AGui) {
      GameBox.debug("found aGui");
      AGui aGui = (AGui) event.getInventory().getHolder();
      boolean topInv = event.getSlot() == event.getRawSlot();
      event.setCancelled(true);
      if (topInv) aGui.onInventoryClick(event);
      else aGui.onBottomInvClick(event);
      return;
    }
    GameBox.debug("none found...");
  }

  @EventHandler
  public void onInvClose(InventoryCloseEvent event) {
    if (!(event.getPlayer() instanceof Player)) return;
    plugin.getInventoryTitleMessenger().removeTitleMessage(event.getPlayer().getUniqueId());
    if (GameBox.openingNewGUI) {
      GameBox.debug("ignoring close because of flag: GameBox.openingNewGUI");
      return;
    }
    if (event.getInventory().getHolder() != null
            && event.getInventory().getHolder() instanceof GameBoxHolder) {
      ((GameBoxHolder) event.getInventory().getHolder()).onInventoryClose(event);
      leaveGameBox((Player) event.getPlayer());
    }
  }

  @EventHandler
  public void onWorldChange(PlayerChangedWorldEvent event) {
    if (!blockedWorlds.contains(event.getPlayer().getLocation().getWorld().getName())) {
      if (!gbPlayers.containsKey(event.getPlayer().getUniqueId())) {
        loadPlayer(event.getPlayer().getUniqueId());
      }
      // hub stuff
      if (GameBoxSettings.hubModeEnabled && hubWorlds.contains(event.getPlayer().getLocation().getWorld().getName()) && setOnWorldJoin) {
        GameBox.debug("in the hub world!");
        giveHubItem(event.getPlayer());
      }
    } else {
      if (gbPlayers.containsKey(event.getPlayer().getUniqueId())) {
        removePlayer(event.getPlayer().getUniqueId());
      }
    }
  }

  private void giveHubItem(Player player) {
    Inventory inv = player.getInventory();
    // check the player inventory for the hubItem
    for (int i = 0; i < inv.getSize(); i++) {
      if (inv.getItem(i) == null) continue;
      if (inv.getItem(i).isSimilar(hubItem)) {
        GameBox.debug("found hub item in slot " + i);
        return;
      }
    }
    // item not found!
    // check the configured hubItemSlot and put it there if it is empty
    if (inv.getItem(hubItemSlot) == null || inv.getItem(hubItemSlot).getType() == Material.AIR) {
      player.getInventory().setItem(hubItemSlot, hubItem);
    } else { // it's not empty so try to add it to the inventory
      if (!inv.addItem(hubItem).isEmpty()) {
        // no space for the hubItem found...
        player.sendMessage(lang.PREFIX + " Failed to give you the hub item (Full inventory)");
      }
    }
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    if (!blockedWorlds.contains(event.getPlayer().getLocation().getWorld().getName())) {
      loadPlayer(event.getPlayer().getUniqueId());
    }
    if (GameBoxSettings.hubModeEnabled && hubWorlds.contains(event.getPlayer().getLocation().getWorld().getName()) && setOnWorldJoin) {
      GameBox.debug("in the hub world!");
      giveHubItem(event.getPlayer());
    }
  }

  @EventHandler
  public void onPlayerLeave(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    // close inventory when in a game or GUI. This should trigger InventoryCloseEvent
    if (isInGame(player.getUniqueId()) || guiManager.isInGUI(player.getUniqueId())
            || guiManager.getShopManager().inShop(player.getUniqueId())) {
      player.closeInventory();
      restoreInventory(player);
    }
    // remove the player and all the personal GUIs. This also saves the GB options of that player.
    if (gbPlayers.containsKey(player.getUniqueId())) {
      removePlayer(event.getPlayer().getUniqueId());
    }
  }

  /**
   * Prevent players that had their inventories saved and cleaned from
   * picking up items to the GUI or to games.
   * <p>
   * It there is space in the saved inventory the item is added.
   * Otherwise the PickUpEvent is cancelled.
   *
   * @param entityPickupItemEvent called Event
   */
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPickUp(EntityPickupItemEvent entityPickupItemEvent) {
    if (!(entityPickupItemEvent.getEntity() instanceof Player)) {
      return;
    }
    Player player = (Player) entityPickupItemEvent.getEntity();
    if (entityPickupItemEvent.isCancelled()) return;
    if (!isInGame(player.getUniqueId()) && !guiManager.isInGUI(player.getUniqueId()) && !guiManager.getShopManager().inShop(player.getUniqueId()))
      return;
    // ToDo: change #addItem() and this method to allow for partial pick up
    if (addItem(player.getUniqueId(), entityPickupItemEvent.getItem().getItemStack())) {
      entityPickupItemEvent.getItem().remove();
    }
    entityPickupItemEvent.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerDeath(PlayerDeathEvent event) {
    if (isInGame(event.getEntity().getUniqueId()) || guiManager.isInGUI(event.getEntity().getUniqueId()) || guiManager.getShopManager().inShop(event.getEntity().getUniqueId())) {
      plugin.getLogger().log(Level.SEVERE, " Player in-game in death event!");
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerDeath(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player)) return;
    if (guiManager == null) {
      plugin.getLogger().warning("The plugin did not start correctly. Please check for previous errors!");
      return;
    }
    // if player is in gui or in game close the inventory
    // this should fire before death event and thus will fix problems with drops on death
    if ((GameBoxSettings.closeInventoryOnDamage
            || event.getFinalDamage() >= ((Player) event.getEntity()).getHealth())
            && (isInGame(event.getEntity().getUniqueId())
            || guiManager.isInGUI(event.getEntity().getUniqueId())
            || guiManager.getShopManager().inShop(event.getEntity().getUniqueId()))) {
      ((Player) event.getEntity()).closeInventory();
    }
  }

  public void removePlayer(UUID uuid) {
    gbPlayers.get(uuid).remove();
    gbPlayers.remove(uuid);
  }

  @EventHandler
  public void onInteractEvent(PlayerInteractEvent event) {
    if (!GameBoxSettings.hubModeEnabled) return;
    if (event.getItem() == null || event.getItem().getType() != hubItem.getType() || event.getItem().getItemMeta() == null || event.getItem().getItemMeta().getDisplayName() == null)
      return;
    if (event.getItem().getItemMeta().getDisplayName().equals(hubItem.getItemMeta().getDisplayName())) {
      event.setCancelled(true);
      if (hubWorlds.contains(event.getPlayer().getLocation().getWorld().getName())) {
        if (Permission.USE.hasPermission(event.getPlayer())) {
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
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (isInGame(player.getUniqueId()) || guiManager.isInGUI(player.getUniqueId()) || guiManager.getShopManager().inShop(player.getUniqueId())) {
        player.closeInventory();
        leaveGameBox(player);
      }
    }
    for (Game game : games.values()) {
      GameBox.debug("disabling " + game.getGameLang().DEFAULT_PLAIN_NAME);
      game.onDisable();
    }
    gamesRegistered = 0;
    if (savedContents.size() > 0) {
      handleLeftoverSavedContents();
    }
    for (GBPlayer player : gbPlayers.values()) {
      player.remove();
    }
    gbPlayers.clear();
  }

  private void handleLeftoverSavedContents() {
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
      log = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(logFile), StandardCharsets.UTF_8));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return;
    }
    ItemStack[] saves;
    for (UUID uuid : savedContents.keySet()) {
      saves = savedContents.get(uuid);
      log.set(uuid.toString() + ".name", Bukkit.getOfflinePlayer(uuid) == null ? "null" : Bukkit.getOfflinePlayer(uuid).getName());
      for (int i = 0; i < saves.length; i++) {
        if (saves[i] != null) log.set(uuid.toString() + ".items." + i, saves[i].toString());
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

  public GameBox getPlugin() {
    return this.plugin;
  }

  /**
   * Register a new game with GameBox
   *
   * @param game Game instance to register
   */
  public void registerGame(Game game) {
    games.put(game.getGameID(), game);
    Permission.registerModuleID(game.getGameID());
    gamesRegistered++;
  }

  public void unregisterGame(String gameID) {
    GameBox.debug("trying to unregister " + gameID);
    if (!games.containsKey(gameID)) return;
    gamesRegistered--;
    Permission.unregisterModuleID(gameID);
    GameBox.debug("unregistered permissions");
    Game game = games.get(gameID);
    if (game != null && game.getGameManager() != null) {
      GameBox.debug("kicking players...");
      GameManager gameManager = game.getGameManager();
      for (Player player : Bukkit.getOnlinePlayers()) {
        if (gameManager.isInGame(player.getUniqueId())) {
          player.closeInventory();
          leaveGameBox(player);
        }
      }
    }
    GameBox.debug("unregister in gui manager...");
    guiManager.unregisterGame(gameID);
    GameBox.debug("unregister in GameRegistry...");
    plugin.getGameRegistry().unregisterGame(gameID);
    GameBox.debug("done");
    games.remove(gameID);
  }

  public GameManager getGameManager(String gameID) {
    Game game = getGame(gameID);
    return game == null ? null : game.getGameManager();
  }

  public GameManager getGameManager(GameBoxGame module) {
    return getGameManager(module.getGameId());
  }

  public GuiManager getGuiManager() {
    return this.guiManager;
  }

  public void setGuiManager(GuiManager guiManager) {
    this.guiManager = guiManager;
  }

  public Map<Integer, ItemStack> getHotBarButtons() {
    return this.hotbarButtons;
  }

  private GameManager getGameManager(UUID uuid) {
    GameManager manager;
    for (String gameID : games.keySet()) {
      manager = getGameManager(gameID);
      if (manager.isInGame(uuid))
        return manager;
    }
    return null;
  }

  public GBPlayer getPlayer(UUID uuid) {
    return gbPlayers.get(uuid);
  }

  public boolean isInGame(UUID uuid) {
    for (String gameID : games.keySet()) {
      if (getGameManager(gameID).isInGame(uuid))
        return true;
    }
    return false;
  }

  public InviteInputHandler getInviteInputHandler() {
    return inviteInputHandler;
  }

  public void setInviteInputHandler(InviteInputHandler inviteInputHandler) {
    this.inviteInputHandler = inviteInputHandler;
  }

  public InvitationHandler getInvitationHandler() {
    return invitationHandler;
  }

  public void setInvitationHandler(InvitationHandler invitationHandler) {
    this.invitationHandler = invitationHandler;
  }

  /**
   * Tries to add an itemStack to the players inventory
   *
   * @param uuid      Player
   * @param itemStack Item
   * @return item successfully given
   */
  public boolean addItem(UUID uuid, ItemStack itemStack) {
    if (!savedContents.containsKey(uuid) || itemStack == null) return false;

    // map for the possibilities to fill already existing stacks up
    List<Integer> fillUpPossibilities = new ArrayList<>();

    GameBox.debug("trying to add an item...");
    ItemStack[] savedStacks = savedContents.get(uuid);
    for (int i = 0; i < 36; i++) {
      if (savedStacks[i] == null) continue;
      if (savedStacks[i].isSimilar(itemStack)
              && ((itemStack.getItemMeta() == null && savedStacks[i].getItemMeta() == null)
              || (itemStack.getItemMeta().getDisplayName() == null && savedStacks[i].getItemMeta().getDisplayName() == null)
              || itemStack.getItemMeta().getDisplayName().equals(savedStacks[i].getItemMeta().getDisplayName()))) {
        if (itemStack.getMaxStackSize() >= itemStack.getAmount() + savedStacks[i].getAmount()) {
          savedStacks[i].setAmount(itemStack.getAmount() + savedStacks[i].getAmount());
          if (!fillUpPossibilities.isEmpty()) {
            for (int slot : fillUpPossibilities) {
              savedStacks[slot].setAmount(itemStack.getMaxStackSize());
            }
          }
          return true;
        } else if (itemStack.getMaxStackSize() > savedStacks[i].getAmount()) {
          int rest = itemStack.getAmount() - (itemStack.getMaxStackSize() - savedStacks[i].getAmount());
          fillUpPossibilities.add(i);
          itemStack.setAmount(rest);
        }
      }
    }
    for (int i = 0; i < 36; i++) {
      if (savedStacks[i] == null) {
        savedStacks[i] = itemStack;
        if (!fillUpPossibilities.isEmpty()) {
          for (int slot : fillUpPossibilities) {
            savedStacks[slot].setAmount(itemStack.getMaxStackSize());
          }
        }
        return true;
      }
    }
    GameBox.debug("   Failed!");
    return false;
  }

  public List<String> getBlockedWorlds() {
    return this.blockedWorlds;
  }

  public void setItemsToKeep(Player player) {
    if (!savedContents.containsKey(player.getUniqueId())) return;
    GameBox.debug("setting the items to keep: " + GameBoxSettings.slotsToKeep);
    for (int slot : GameBoxSettings.slotsToKeep) {
      player.getInventory().setItem(slot, savedContents.get(player.getUniqueId())[slot]);
    }
  }

  public Map<String, Game> getGames() {
    return this.games;
  }

  public Game getGame(GameBoxGame module) {
    return getGame(module.getGameId());
  }

  public Game getGame(String gameID) {
    return games.get(gameID);
  }

  public Game getGame(UUID uuid) {
    for (Game game : games.values()) {
      if (game.getGameManager().isInGame(uuid))
        return game;
    }
    return null;
  }

  /**
   * Read only!
   *
   * @return players
   */
  public Map<UUID, GBPlayer> getGbPlayers() {
    return Collections.unmodifiableMap(this.gbPlayers);
  }

  public boolean enterGameBox(Player player, String moduleID, String menuID) {
    EnterGameBoxEvent enterEvent = new EnterGameBoxEvent(player, moduleID, menuID);
    if (!enterEvent.isCancelled()) {
      saveInventory(player);
      return true;
    } else {
      player.sendMessage(lang.PREFIX + enterEvent.getCancelMessage());
      return false;
    }
  }

  @SuppressWarnings("deprecation")
  public void leaveGameBox(Player player) {
    restoreInventory(player);
    plugin.getInventoryTitleMessenger().removeTitleMessage(player.getUniqueId());
    player.updateInventory();
    new LeftGameBoxEvent(player);
  }

  public boolean isBlockedWorld(String worldName) {
    return blockedWorlds.contains(worldName);
  }
}
