package me.nikl.gamebox.inventory.menu.games;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.games.Game;
import me.nikl.gamebox.games.GameRule;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.inventory.button.Button;
import me.nikl.gamebox.nms.NmsFactory;
import me.nikl.gamebox.utility.InventoryUtility;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by nikl on 21.02.18.
 */
public class StartMultiplayerGamePageMenu extends GamePageMenu {
    private Map<UUID, ArrayList<UUID>> invitations = new HashMap<>();
    private Map<UUID, Button[]> invitationButtons = new HashMap<>();
    private GameRule rule;

    public StartMultiplayerGamePageMenu(GameBox gameBox, Game game, GameRule rule) {
        super(gameBox, game);
        this.rule = rule;

        Button button = new Button(new MaterialData(Material.IRON_BLOCK).toItemStack(1));
        button.setAction(ClickAction.START_PLAYER_INPUT);
        button.setArgs(getModuleID(), rule.getKey());
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName(gameBox.lang.BUTTON_INVITE_BUTTON_NAME);
        meta.setLore(gameBox.lang.BUTTON_INVITE_BUTTON_LORE);
        button.setItemMeta(meta);

        setButton(button, 0);
    }


    @Override
    public boolean open(Player player) {
        GameBox.debug("open called in StartMultiplayerGamePage");
        if (!playerInventories.containsKey(player.getUniqueId())) {
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
        Inventory inv = getNewInventory();
        inv.setContents(inventory.getContents().clone());

        playerInventories.put(uniqueId, inv);
    }

    public void addInvite(UUID uuid1, UUID uuid2) {
        // set maximal 53 invites in the inventory
        if (!invitations.keySet().contains(uuid2)) {
            invitations.put(uuid2, new ArrayList<>());
        }
        if (!invitationButtons.keySet().contains(uuid2)) {
            invitationButtons.put(uuid2, new Button[inventory.getSize()]);
        }
        invitations.get(uuid2).add(uuid1);
        updateInvitations(uuid2);
    }

    private void updateInvitations(UUID uuid2) {
        if (!playerInventories.containsKey(uuid2)) {
            playerInventories.put(uuid2, InventoryUtility.createInventory(null, 54, "Your invite inv."));
        }
        Inventory inv = playerInventories.get(uuid2);
        inv.setContents(inventory.getContents().clone());
        int i = 1;
        for (UUID uuid1 : invitations.get(uuid2)) {
            if (i >= inventory.getSize()) break;
            Player player1 = Bukkit.getPlayer(uuid1);
            if (player1 == null) continue;
            Button skull = new Button(new MaterialData(Material.SKULL_ITEM).toItemStack(1));
            skull.setDurability((short) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwner(player1.getName());
            meta.setDisplayName(gameBox.lang.BUTTON_INVITE_SKULL_NAME.replace("%player%", player1.getName()));
            meta.setLore(gameBox.lang.BUTTON_INVITE_SKULL_LORE);
            skull.setItemMeta(meta);
            skull.setAction(ClickAction.START_GAME);
            skull.setArgs(getModuleID(), rule.getKey(), uuid1.toString());
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
        if (!invitations.keySet().contains(uuid2)) {
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
