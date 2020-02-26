package me.nikl.gamebox.inventory;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxLanguage;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.events.EnterGameBoxEvent;
import me.nikl.gamebox.inventory.button.Button;
import me.nikl.gamebox.inventory.gui.AGui;
import me.nikl.gamebox.inventory.gui.MainGui;
import me.nikl.gamebox.inventory.gui.game.GameGui;
import me.nikl.gamebox.inventory.shop.ShopManager;
import me.nikl.gamebox.utility.Permission;
import me.nikl.nmsutilities.NmsFactory;
import me.nikl.nmsutilities.NmsUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Niklas Eicker
 * <p>
 * each game gets such a manager
 * listen for events called in the game's guis and pages
 * (not mainGUI and not game)
 */
public class GUIManager {
  public static final String TOP_LIST_KEY_ADDON = "topList";
  public static final String MAIN_GAME_GUI = "main";
  private GameBox plugin;
  private Map<String, Map<String, GameGui>> gameGuis;
  private NmsUtility nms;
  private GameBoxLanguage lang;
  private MainGui mainGui;
  private int titleMessageSeconds = 3;
  private ShopManager shopManager;

  public GUIManager(GameBox plugin) {
    this.plugin = plugin;
    this.nms = NmsFactory.getNmsUtility();
    this.lang = plugin.lang;
    this.gameGuis = new HashMap<>();
    this.mainGui = new MainGui(plugin, this);
    shopManager = new ShopManager(plugin, this);
    if (GameBoxSettings.tokensEnabled) mainGui.registerShop();
  }

  public boolean isInGUI(UUID uuid) {
    if (isInMainGUI(uuid)) return true;
    if (isInGameGUI(uuid)) return true;
    return false;
  }

  public boolean isInMainGUI(UUID uuid) {
    if (mainGui.isInGui(uuid)) {
      return true;
    }
    return false;
  }

  public boolean isInGameGUI(UUID uuid) {
    for (String gameID : gameGuis.keySet()) {
      if (isInGameGUI(uuid, gameID)) return true;
    }
    return false;
  }

  public boolean isInGameGUI(UUID uuid, String gameID) {
    Map<String, GameGui> guis = gameGuis.get(gameID);
    for (GameGui gui : guis.values()) {
      if (gui.isInGui(uuid)) {
        return true;
      }
    }
    return false;
  }

  public boolean openGameGui(Player whoClicked, String... args) {
    if (args == null || args.length != 2) {
      GameBox.debug("unknown number of arguments in GUIManager.openGameGui");
      return false;
    }

    if (!plugin.getPluginManager().hasSavedContents(whoClicked.getUniqueId())) {
      if (!plugin.getPluginManager().enterGameBox(whoClicked, args[0], args[1])) return false;
    }

    String gameID = args[0], key = args[1];
    if (Permission.OPEN_GAME_GUI.hasPermission(whoClicked, gameID)) {
      AGui gui = gameGuis.get(gameID).get(key);
      GameBox.openingNewGUI = true;
      boolean opened = gui.open(whoClicked);
      GameBox.openingNewGUI = false;
      if (opened) {
        nms.updateInventoryTitle(whoClicked, gui.getTitle().replace("%game%", plugin.getPluginManager().getGame(gameID).getGameLang().PLAIN_NAME).replace("%player%", whoClicked.getName()));
      } else {
        if (whoClicked.getOpenInventory() != null) {
          whoClicked.closeInventory();
        }
        plugin.getPluginManager().leaveGameBox(whoClicked);
      }
      return opened;
    } else {
      if (isInGUI(whoClicked.getUniqueId())) {
        plugin.getInventoryTitleMessenger().sendInventoryTitle(whoClicked, plugin.lang.TITLE_NO_PERM, titleMessageSeconds);
      } else {
        if (whoClicked.getOpenInventory() != null) {
          whoClicked.closeInventory();
        }
        plugin.getPluginManager().leaveGameBox(whoClicked);
      }
      whoClicked.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);
      return false;
    }
  }

  /**
   * Open the plugins main gui for the player
   *
   * @param whoClicked player
   * @return success in opening the gui
   */
  public boolean openMainGui(Player whoClicked) {
    if (!Permission.USE.hasPermission(whoClicked)) {
      whoClicked.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);
      return false;
    }

    if (!plugin.getPluginManager().hasSavedContents(whoClicked.getUniqueId())) {
      EnterGameBoxEvent enterEvent = new EnterGameBoxEvent(whoClicked, "main");
      if (!enterEvent.isCancelled()) {
        plugin.getPluginManager().saveInventory(whoClicked);
      } else {
        whoClicked.sendMessage("A game was canceled with the reason: " + enterEvent.getCancelMessage());
        return false;
      }
    }
    GameBox.openingNewGUI = true;
    boolean open = mainGui.open(whoClicked);
    GameBox.openingNewGUI = false;
    if (open) return true;
    // the gui didn't open. Make sure to restore all inventory content
    if (whoClicked.getOpenInventory() != null) {
      whoClicked.closeInventory();
    }
    plugin.getPluginManager().leaveGameBox(whoClicked);
    return false;
  }


  /**
   * Register a GUI
   * <p>
   * To use this method the args of the gui have to be set already
   *
   * @param gui Gui to register
   */
  public void registerGameGUI(GameGui gui) {
    if (gui.getArgs() == null || gui.getArgs().length != 2) {
      Bukkit.getConsoleSender().sendMessage(lang.PREFIX + ChatColor.RED + " Error while registering a gui");
      Bukkit.getConsoleSender().sendMessage(lang.PREFIX + ChatColor.RED + "   missing args");
      return;
    }
    String[] args = gui.getArgs();
    gameGuis.computeIfAbsent(args[0], k -> new HashMap<>());
    gameGuis.get(args[0]).put(args[1], gui);
    GameBox.debug("registered gamegui: " + args[0] + ", " + args[1]);
  }

  /**
   * Register the main GUI of a game
   *
   * @param gui    game gui to register
   * @param button button in the main gui that will open the game gui
   */
  public void registerMainGameGUI(GameGui gui, ItemStack button) {
    if (gui.getArgs() == null || gui.getArgs().length != 2) {
      Bukkit.getConsoleSender().sendMessage(lang.PREFIX + ChatColor.RED + " Error while registering a gui");
      Bukkit.getConsoleSender().sendMessage(lang.PREFIX + ChatColor.RED + "   missing args");
      return;
    }
    String[] args = gui.getArgs();
    gameGuis.computeIfAbsent(args[0], k -> new HashMap<>());
    gameGuis.get(args[0]).put(args[1], gui);
    GameBox.debug("registered gamegui: " + args[0] + ", " + args[1]);
    Button gameButton = new Button(button);
    gameButton.setItemMeta(button.getItemMeta());
    gameButton.setAction(ClickAction.OPEN_GAME_GUI);
    gameButton.setArgs(args[0], args[1]);
    mainGui.registerGameButton(gameButton, plugin.getGameRegistry().getPreferredMainMenuSlot(args[0]));
  }

  public AGui getCurrentGui(UUID uuid) {
    if (mainGui.isInGui(uuid)) {
      return mainGui;
    }
    for (String gameID : gameGuis.keySet()) {
      for (GameGui gui : gameGuis.get(gameID).values()) {
        if (gui.isInGui(uuid)) {
          return gui;
        }
      }
    }
    return shopManager.getShopGui(uuid);
  }

  public MainGui getMainGui() {
    return this.mainGui;
  }

  public void removePlayer(UUID uuid) {
    for (String gameID : gameGuis.keySet()) {
      Map<String, GameGui> guis = gameGuis.get(gameID);
      for (GameGui gui : guis.values()) {
        gui.removePlayer(uuid);
      }
    }
    this.mainGui.removePlayer(uuid);
  }

  public AGui getGameGui(String gameID, String key) {
    return gameGuis.get(gameID) == null ? null : gameGuis.get(gameID).get(key);
  }

  public boolean openShopPage(Player whoClicked, String[] args) {
    return shopManager.openShopPage(whoClicked, args);
  }

  public ShopManager getShopManager() {
    return shopManager;
  }

  public int getTitleMessageSeconds() {
    return titleMessageSeconds;
  }

  public void unregisterGame(String gameID) {
    gameGuis.remove(gameID);
    mainGui.unregisterGame(gameID);
  }
}
