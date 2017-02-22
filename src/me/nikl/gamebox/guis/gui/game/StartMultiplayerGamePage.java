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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Niklas on 22.02.2017.
 */
public class StartMultiplayerGamePage extends GameGuiPage {
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
        Inventory inv = Bukkit.createInventory(null, 54, "Your invite inv.");
        inv.setContents(inventory.getContents().clone());
        ItemStack item = new MaterialData(Material.SKULL_ITEM).toItemStack();
        item.setAmount(1);
        item.setDurability((short)3);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Your first inv");
        meta.setLore(Arrays.asList("","your uuid: "+uniqueId.toString()));
        item.setItemMeta(meta);
        inv.setItem(1, item);
        openInventories.put(uniqueId, inv);
    }

}
