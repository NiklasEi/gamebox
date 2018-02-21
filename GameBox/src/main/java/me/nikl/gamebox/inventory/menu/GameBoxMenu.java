package me.nikl.gamebox.inventory.menu;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.inventory.InventoryTitleMessenger;
import me.nikl.gamebox.inventory.button.AButton;
import me.nikl.gamebox.utility.InventoryUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Niklas Eicker
 */
public abstract class GameBoxMenu implements InventoryHolder {
    protected int size;
    protected String defaultTitle = "GameBox";
    protected GameBox gameBox;
    protected Set<UUID> players = new HashSet<>();
    private InventoryTitleMessenger inventoryTitleMessenger;
    protected int titleMessageSeconds = 3;
    protected Inventory inventory;

    public GameBoxMenu(GameBox gameBox){
        this.gameBox = gameBox;
        inventoryTitleMessenger = gameBox.getInventoryTitleMessenger();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void onClick(InventoryClickEvent event) {
        if (event.getSlot() == event.getRawSlot()) {
            onUpperClick(event);
        } else {
            onLowerClick(event);
        }
    }

    protected abstract void onUpperClick(InventoryClickEvent event);

    protected abstract void onLowerClick(InventoryClickEvent event);

    public abstract boolean open(Player player);

    public abstract String getPlayerSpezificTitle(Player player);

    public String getDefaultTitle() {
        return defaultTitle;
    }

    protected void sentInventoryTitleMessage(Player player, String message) {
        inventoryTitleMessenger.sendInventoryTitle(player, message, titleMessageSeconds);
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

    public boolean isInMenu(UUID uuid) {
        return players.contains(uuid);
    }

    public Inventory getNewInventory() {
        return InventoryUtility.createInventory(this, size, getDefaultTitle());
    }

    public Inventory getNewInventory(Player player) {
        return InventoryUtility.createInventory(this, size, getPlayerSpezificTitle(player));
    }
}
