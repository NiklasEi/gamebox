package me.nikl.gamebox.inventory.menu;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.PluginManager;
import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.games.GameManager;
import me.nikl.gamebox.games.exceptions.GameStartException;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.inventory.GUIManager;
import me.nikl.gamebox.inventory.InventoryTitleMessenger;
import me.nikl.gamebox.inventory.button.AButton;
import me.nikl.gamebox.inventory.gui.AGui;
import me.nikl.gamebox.inventory.gui.MainGui;
import me.nikl.gamebox.inventory.gui.game.StartMultiplayerGamePage;
import me.nikl.gamebox.inventory.menu.games.StartMultiplayerGamePageMenu;
import me.nikl.gamebox.inventory.menu.main.MainMenu;
import me.nikl.gamebox.inventory.shop.Shop;
import me.nikl.gamebox.inventory.shop.ShopItem;
import me.nikl.gamebox.utility.InventoryUtility;
import me.nikl.gamebox.utility.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

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
    private PluginManager pluginManager;
    private GUIManager guiManager;

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
