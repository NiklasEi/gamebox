package me.nikl.gamebox.inventory.gui;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.data.TokenListener;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.inventory.GUIManager;
import me.nikl.gamebox.inventory.button.AButton;
import me.nikl.gamebox.inventory.button.Button;
import me.nikl.gamebox.inventory.button.ButtonFactory;
import me.nikl.gamebox.inventory.button.DisplayButton;
import me.nikl.gamebox.inventory.button.ToggleButton;
import me.nikl.gamebox.utility.InventoryUtility;
import me.nikl.gamebox.utility.ItemStackUtility;
import me.nikl.nmsutilities.NmsFactory;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Niklas Eicker
 */
public class MainGui extends AGui implements TokenListener {
  private Map<UUID, ToggleButton> soundButtons = new HashMap<>();
  private Map<UUID, DisplayButton> tokenButtons = new HashMap<>();
  private Map<AButton, Integer> preferredSlotsOfGameButtons = new HashMap<>();
  private int soundToggleSlot = 52;
  private int tokenButtonSlot = 45;
  private int shopSlot = 46;

  public MainGui(GameBox plugin, GUIManager guiManager) {
    super(plugin, guiManager, 54, new String[]{}, plugin.lang.TITLE_MAIN_GUI);
    Button help = new Button(NmsFactory.getNmsUtility().addGlow(ItemStackUtility.createBookWithText(plugin.lang.BUTTON_MAIN_MENU_INFO)));
    help.setAction(ClickAction.NOTHING);
    setButton(help, 53);
    ToggleButton soundToggle = ButtonFactory.createToggleButton(plugin.lang);
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
  public boolean open(Player player) {
    if (!openInventories.containsKey(player.getUniqueId())) {
      if (!loadMainGui(player)) return false;
    }
    if (super.open(player)) {
      if (pluginManager.getGames().isEmpty()) {
        NmsFactory.getNmsUtility().updateInventoryTitle(player, ChatColor.translateAlternateColorCodes('&', "&c&l %player% you should get some games on Spigot ;)".replace("%player%", player.getName())));
      }
      return true;
    }
    return false;
  }

  public ToggleButton getSoundToggleButton(UUID uuid) {
    return soundButtons.get(uuid);
  }

  public boolean loadMainGui(Player player) {
    GBPlayer gbPlayer = pluginManager.getPlayer(player.getUniqueId());
    if (gbPlayer == null) {
      pluginManager.loadPlayer(player.getUniqueId());
      return false;
    }
    String title = this.title.replace("%player%", player.getName());
    Inventory inventory = InventoryUtility.createInventory(this, this.inventory.getSize(), title);
    inventory.setContents(this.inventory.getContents().clone());
    ToggleButton soundToggle = ButtonFactory.createToggleButton(gameBox.lang);
    soundToggle = gbPlayer.isPlaySounds() ? soundToggle : soundToggle.toggle();
    soundButtons.put(gbPlayer.getUuid(), soundToggle);
    inventory.setItem(soundToggleSlot, soundToggle);
    if (GameBoxSettings.tokensEnabled) {
      DisplayButton tokens = ButtonFactory.createTokenButton(gameBox.lang, gbPlayer.getTokens());
      tokenButtons.put(gbPlayer.getUuid(), tokens);
      inventory.setItem(tokenButtonSlot, tokens);
    }
    openInventories.putIfAbsent(gbPlayer.getUuid(), inventory);
    return true;
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

  public void registerGameButton(Button gameButton, int preferredMainMenuSlot) {
    preferredSlotsOfGameButtons.put(gameButton, preferredMainMenuSlot);
  }

  private void placeGameButtons() {
    boolean complications = false;
    List<AButton> noPreferredSlotButtons = new ArrayList<>();
    Map<Integer, AButton> preferredSlots = new HashMap<>();
    for (Map.Entry<AButton, Integer> entry : preferredSlotsOfGameButtons.entrySet()) {
      int preferredSlot = entry.getValue();
      if (preferredSlot < 0) {
        noPreferredSlotButtons.add(entry.getKey());
        continue;
      }
      if (preferredSlots.keySet().contains(entry.getValue())) {
        gameBox.warning("The preferred slot for " + entry.getKey().getArgs()[0] + " is already taken!");
        complications = true;
        noPreferredSlotButtons.add(entry.getKey());
        continue;
      }
      if (entry.getValue() > 44) {
        gameBox.warning("The preferred slot for " + entry.getKey().getArgs()[0] + " is > 44!");
        complications = true;
        noPreferredSlotButtons.add(entry.getKey());
        continue;
      }
      preferredSlots.put(entry.getValue(), entry.getKey());
    }
    if (complications) {
      gameBox.warning("Please fix the problem(s) above in 'games.yml'");
      gameBox.warning("Until then these preferred slots are ignored.");
    }
    placeGameButtonsInPreferredSlots(preferredSlots);
    placeGameButtons(noPreferredSlotButtons);
  }

  private void placeGameButtons(List<AButton> noPreferredSlotButtons) {
    for (AButton button : noPreferredSlotButtons) {
      setButton(button);
    }
  }

  private void placeGameButtonsInPreferredSlots(Map<Integer, AButton> preferredSlots) {
    for (Map.Entry<Integer, AButton> entry : preferredSlots.entrySet()) {
      setButton(entry.getValue(), entry.getKey());
    }
  }
}
