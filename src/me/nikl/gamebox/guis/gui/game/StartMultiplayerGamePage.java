package me.nikl.gamebox.guis.gui.game;

import me.nikl.gamebox.ClickAction;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

import java.util.*;

/**
 * Created by Niklas on 22.02.2017.
 *
 * GUI
 */
public class StartMultiplayerGamePage extends GameGuiPage {
    private Map<UUID, ArrayList<UUID>> invitations = new HashMap<>();
    private Map<UUID, AButton[]> invitationButtons = new HashMap<>();

    public StartMultiplayerGamePage(GameBox plugin, GUIManager guiManager, int slots, String gameID, String key, String title) {
        super(plugin, guiManager, slots, gameID, key, title);


        AButton button = new AButton(new MaterialData(Material.IRON_BLOCK), 1);
        button.setAction(ClickAction.START_PLAYER_INPUT);
        button.setArgs(gameID, key);
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Invite someone");
        meta.setLore(Arrays.asList("",ChatColor.BLUE+"Click to invite someone over chat"));
        button.setItemMeta(meta);

        setButton(button, 0);
    }

    @Override
    public boolean open(Player player){
        GameBox.debug("open called in StartMultiplayerGamePage");
        if(!openInventories.containsKey(player.getUniqueId())){
            loadInvites(player.getUniqueId());
        }
        if(super.open(player)){
            plugin.getNMS().updateInventoryTitle(player, plugin.lang.TITLE_MAIN_GUI.replace("%player%", player.getName()));
            return true;
        }
        return false;
    }

    private void loadInvites(UUID uniqueId) {
        GameBox.debug("loading inventory");
        invitations.put(uniqueId, new ArrayList<>());
        invitationButtons.put(uniqueId, new AButton[inventory.getSize()]);
        Inventory inv = Bukkit.createInventory(null, 54, "Your invite inv.");
        inv.setContents(inventory.getContents().clone());

        openInventories.put(uniqueId, inv);
    }

    public void addInvite(UUID uuid1, UUID uuid2){
        // set maximal 53 invites in the inventory
        if(!invitations.keySet().contains(uuid2)){
            invitations.put(uuid2, new ArrayList<>());
        }
        if(!invitationButtons.keySet().contains(uuid2)){
            invitationButtons.put(uuid2, new AButton[inventory.getSize()]);
        }
        invitations.get(uuid2).add(uuid1);
        updateInvitations(uuid2);
    }

    private void updateInvitations(UUID uuid2) {
        if(!openInventories.containsKey(uuid2)){
            openInventories.put(uuid2, Bukkit.createInventory(null, 54, "Your invite inv."));
        }
        Inventory inv = openInventories.get(uuid2);
        inv.setContents(inventory.getContents().clone());
        int i = 1;
        for(UUID uuid1 : invitations.get(uuid2)){
            if(i >= inventory.getSize()) break;
            Player player1 = Bukkit.getPlayer(uuid1);
            if(player1 == null) continue;
            AButton skull =  new AButton(new MaterialData(Material.SKULL_ITEM), 1);
            skull.setDurability((short) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwner(player1.getName());
            meta.setDisplayName(ChatColor.GOLD + player1.getName());
            meta.setLore(Arrays.asList("", ChatColor.BLUE+"Click to accept the invitation"));
            skull.setItemMeta(meta);
            skull.setAction(ClickAction.START_GAME);
            skull.setArgs(gameID, key, uuid1.toString());
            inv.setItem(i, skull);
            invitationButtons.get(uuid2)[i] = skull;
            i++;
        }
    }

    @Override
    public void removePlayer(UUID uuid){
        invitations.remove(uuid);
        super.removePlayer(uuid);
    }

    public void removeInvite(UUID uuid1, UUID uuid2) {
        if(!invitations.keySet().contains(uuid2)){
            return;
        }
        invitations.get(uuid2).remove(uuid1);
        updateInvitations(uuid2);
    }

    public AButton getButton(UUID uuid, int slot) {
        if(invitationButtons.containsKey(uuid)){
            return invitationButtons.get(uuid)[slot];
        }
        return null;
    }
}
