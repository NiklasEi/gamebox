package me.nikl.gamebox.inventory.gui.game;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.inventory.GuiManager;
import me.nikl.gamebox.inventory.button.Button;
import me.nikl.gamebox.utility.InventoryUtility;
import me.nikl.gamebox.utility.ItemStackUtility;
import me.nikl.nmsutilities.NmsFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Niklas Eicker
 * <p>
 * GUI
 */
public class StartMultiplayerGamePage extends GameGuiPage {
  private Map<UUID, ArrayList<UUID>> invitations = new HashMap<>();
  private Map<UUID, Button[]> invitationButtons = new HashMap<>();

  public StartMultiplayerGamePage(GameBox plugin, GuiManager guiManager, int slots, String gameID, String key, String title) {
    super(plugin, guiManager, slots, gameID, key, title);


    Button button = new Button(new ItemStack(Material.IRON_BLOCK, 1));
    button.setAction(ClickAction.START_PLAYER_INPUT);
    button.setArgs(gameID, key);
    ItemMeta meta = button.getItemMeta();
    meta.setDisplayName(plugin.lang.BUTTON_INVITE_BUTTON_NAME);
    meta.setLore(plugin.lang.BUTTON_INVITE_BUTTON_LORE);
    button.setItemMeta(meta);

    setButton(button, 0);
  }

  @Override
  public boolean open(Player player) {
    GameBox.debug("open called in StartMultiplayerGamePage");
    if (!openInventories.containsKey(player.getUniqueId())) {
      loadInvites(player.getUniqueId());
    }
    if (super.open(player)) {
      NmsFactory.getNmsUtility().updateInventoryTitle(player, gameBox.lang.TITLE_MAIN_GUI.replace("%player%", player.getName()));
      return true;
    }
    return false;
  }

  private void loadInvites(UUID uniqueId) {
    GameBox.debug("loading inventory");
    invitations.put(uniqueId, new ArrayList<>());
    invitationButtons.put(uniqueId, new Button[inventory.getSize()]);
    Inventory inv = InventoryUtility.createInventory(this, 54, "Your invite inv.");
    inv.setContents(inventory.getContents().clone());

    openInventories.put(uniqueId, inv);
  }

  public void addInvite(UUID uuid1, UUID uuid2) {
    // set maximal 53 invites in the inventory
    if (!invitations.containsKey(uuid2)) {
      invitations.put(uuid2, new ArrayList<>());
    }
    if (!invitationButtons.containsKey(uuid2)) {
      invitationButtons.put(uuid2, new Button[inventory.getSize()]);
    }
    invitations.get(uuid2).add(uuid1);
    updateInvitations(uuid2);
  }

  private void updateInvitations(UUID uuid2) {
    if (!openInventories.containsKey(uuid2)) {
      openInventories.put(uuid2, InventoryUtility.createInventory(this, 54, "Your invite inv."));
    }
    Inventory inv = openInventories.get(uuid2);
    inv.setContents(inventory.getContents().clone());
    int i = 1;
    for (UUID uuid1 : invitations.get(uuid2)) {
      if (i >= inventory.getSize()) break;
      Player player1 = Bukkit.getPlayer(uuid1);
      if (player1 == null) continue;
      Button skull = new Button(new ItemStack(ItemStackUtility.PLAYER_HEAD, 1));
      SkullMeta meta = (SkullMeta) skull.getItemMeta();
      meta.setOwningPlayer(player1);
      meta.setDisplayName(gameBox.lang.BUTTON_INVITE_SKULL_NAME.replace("%player%", player1.getName()));
      meta.setLore(gameBox.lang.BUTTON_INVITE_SKULL_LORE);
      skull.setItemMeta(meta);
      skull.setAction(ClickAction.START_GAME);
      skull.setArgs(args[0], args[1], uuid1.toString());
      inv.setItem(i, skull);
      invitationButtons.get(uuid2)[i] = skull;
      i++;
    }
  }

  @Override
  public void removePlayer(UUID uuid) {
    invitations.remove(uuid);
    super.removePlayer(uuid);
  }

  public void removeInvite(UUID uuid1, UUID uuid2) {
    if (!invitations.containsKey(uuid2)) {
      return;
    }
    invitations.get(uuid2).remove(uuid1);
    updateInvitations(uuid2);
  }

  public Button getButton(UUID uuid, int slot) {
    if (invitationButtons.containsKey(uuid)) {
      return invitationButtons.get(uuid)[slot];
    }
    return null;
  }
}
