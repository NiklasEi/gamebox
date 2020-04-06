package me.nikl.gamebox.inventory.gui;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.data.TokenListener;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.inventory.GuiManager;
import me.nikl.gamebox.inventory.button.AButton;
import me.nikl.gamebox.inventory.button.Button;
import me.nikl.gamebox.inventory.button.ButtonFactory;
import me.nikl.gamebox.inventory.button.DisplayButton;
import me.nikl.gamebox.inventory.button.ToggleButton;
import me.nikl.gamebox.utility.InventoryUtility;
import me.nikl.gamebox.utility.ItemStackUtility;
import me.nikl.gamebox.utility.Permission;
import me.nikl.nmsutilities.NmsFactory;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Niklas Eicker
 */
public class MainGui extends AGui implements TokenListener {
  private Map<UUID, ToggleButton> soundButtons = new HashMap<>();
  private Map<UUID, DisplayButton> tokenButtons = new HashMap<>();
  private Map<String, AButton> gameButtons = new HashMap<>();
  private Map<UUID, AButton[]> playerGrids = new HashMap<>();
  private int soundToggleSlot = 52;
  private int tokenButtonSlot = 45;
  private int modulesGuiSlot = 49;
  private int shopSlot = 46;

  public MainGui(GameBox plugin, GuiManager guiManager) {
    super(plugin, guiManager, 54, new String[]{}, plugin.lang.TITLE_MAIN_GUI);
    Button help = new Button(NmsFactory.getNmsUtility().addGlow(ItemStackUtility.createBookWithText(plugin.lang.BUTTON_MAIN_MENU_INFO)));
    help.setAction(ClickAction.NOTHING);
    setButton(help, 53);
    ToggleButton soundToggle = ButtonFactory.createSoundToggleButton(plugin.lang);
    setButton(soundToggle, soundToggleSlot);
    if (GameBoxSettings.tokensEnabled) {
      GBPlayer.addTokenListener(this);
      DisplayButton tokens = ButtonFactory.createTokenButton(plugin.lang, 0);
      setButton(tokens, tokenButtonSlot);
    }
    Map<Integer, ItemStack> hotBarButtons = plugin.getPluginManager().getHotBarButtons();
    // set lower grid
    if (hotBarButtons.containsKey(GameBoxSettings.exitButtonSlot)) {
      Button exit = new Button(hotBarButtons.get(GameBoxSettings.exitButtonSlot));
      ItemMeta meta = hotBarButtons.get(GameBoxSettings.exitButtonSlot).getItemMeta();
      exit.setItemMeta(meta);
      exit.setAction(ClickAction.CLOSE);
      setLowerButton(exit, GameBoxSettings.exitButtonSlot);
    }

    // game buttons are registered after gamebox finished loading up. Since they can define preferred slots, we need to collect all buttons and slots first, then place them.
    new BukkitRunnable() {
      @Override
      public void run() {
        placeGameButtons();
      }
    }.runTask(plugin);
  }

  public void registerShop() {
    setButton(guiManager.getShopManager().getMainButton(), shopSlot);
  }

  @Override
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getCurrentItem() == null) return;
    AButton[] playerGrid = this.playerGrids.get(event.getWhoClicked().getUniqueId());
    this.handleInventoryClick(event, playerGrid);
  }

  @Override
  public boolean open(Player player) {
    if (!openInventories.containsKey(player.getUniqueId())) {
      if (!loadMainGui(player)) return false;
    }
    if (super.open(player)) {
      if (pluginManager.getGames().isEmpty()) {
        NmsFactory.getNmsUtility().updateInventoryTitle(player, ChatColor.translateAlternateColorCodes('&', "&1No games &4:(&1 You should install some"));
      }
      return true;
    }
    return false;
  }

  public ToggleButton getSoundToggleButton(UUID uuid) {
    return soundButtons.get(uuid);
  }

  public boolean loadMainGui(Player player) {
    AButton[] playerGrid = loadPlayerGrid(player);
    this.playerGrids.put(player.getUniqueId(), playerGrid);
    GBPlayer gbPlayer = pluginManager.getPlayer(player.getUniqueId());
    if (gbPlayer == null) {
      pluginManager.loadPlayer(player.getUniqueId());
      return false;
    }
    String title = this.title.replace("%player%", player.getName());
    Inventory inventory = InventoryUtility.createInventory(this, this.inventory.getSize(), title);
    ItemStack[] contents = this.inventory.getContents().clone();
    // overwrite game buttons
    System.arraycopy(playerGrid, 0, contents, 0, 45);
    if (Permission.ADMIN_MODULES.hasPermission(player)) {
      playerGrid[modulesGuiSlot] = guiManager.getModulesGuiManager().getMainButton();
      contents[modulesGuiSlot] = guiManager.getModulesGuiManager().getMainButton();
    }
    inventory.setContents(contents);
    ToggleButton soundToggle = ButtonFactory.createSoundToggleButton(gameBox.lang);
    soundToggle = gbPlayer.isPlaySounds() ? soundToggle : soundToggle.toggle();
    soundButtons.put(gbPlayer.getUuid(), soundToggle);
    inventory.setItem(soundToggleSlot, soundToggle);
    if (GameBoxSettings.tokensEnabled) {
      DisplayButton tokens = ButtonFactory.createTokenButton(gameBox.lang, gbPlayer.getTokens());
      tokenButtons.put(gbPlayer.getUuid(), tokens);
      inventory.setItem(tokenButtonSlot, tokens);
    }
    openInventories.put(gbPlayer.getUuid(), inventory);
    return true;
  }

  private AButton[] loadPlayerGrid(Player player) {
    AButton[] cleanedGrid = grid.clone();
    for (int slot=0; slot<45; slot++) {
      cleanedGrid[slot] = null; // remove all game buttons from grid
    }
    List<String> buttonGameIds = new ArrayList<>(gameButtons.keySet());
    buttonGameIds = buttonGameIds.stream().filter((id) -> Permission.PLAY_GAME.hasPermission(player, id)).collect(Collectors.toList());
    Collections.sort(buttonGameIds);
    for (int slot = 0; slot < buttonGameIds.size(); slot++) {
      cleanedGrid[slot] = gameButtons.get(buttonGameIds.get(slot));
    }
    return cleanedGrid;
  }

  @Override
  public void updateToken(GBPlayer player) {
    DisplayButton tokenButton = tokenButtons.get(player.getUuid());
    if (tokenButton != null) {
      tokenButton.update("%tokens%", player.getTokens());
      openInventories.get(player.getUuid()).setItem(tokenButtonSlot, tokenButton);
    }
  }

  @Override
  public void removePlayer(UUID uuid) {
    soundButtons.remove(uuid);
    tokenButtons.remove(uuid);
    playerGrids.remove(uuid);
    super.removePlayer(uuid);
  }

  public void unregisterGame(String gameID) {
    AButton button;
    for (int i = 0; i < grid.length; i++) {
      button = grid[i];
      if (button != null && button.getAction() == ClickAction.OPEN_GAME_GUI && button.getArgs()[0].equals(gameID)) {
        grid[i] = null;
        inventory.setItem(i, null);
        for (Inventory inventory : openInventories.values()) {
          inventory.setItem(i, null);
        }
      }
    }
  }

  public void registerGameButton(Button gameButton, String gameId) {
    gameButtons.put(gameId, gameButton);
  }

  private void placeGameButtons() {
    List<String> buttonGameIds = new ArrayList<>(gameButtons.keySet());
    Collections.sort(buttonGameIds);
    if (buttonGameIds.size() > 45) {
      gameBox.warning("The number of installed game buttons is bigger than 45!");
      gameBox.warning("This is currently not supported");
      buttonGameIds = buttonGameIds.subList(0, 45);
    }
    for (int slot = 0; slot < buttonGameIds.size(); slot++) {
      setButton(gameButtons.get(buttonGameIds.get(slot)), slot);
    }
  }
}
