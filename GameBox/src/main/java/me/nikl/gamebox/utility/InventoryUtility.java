package me.nikl.gamebox.utility;

import me.nikl.gamebox.GameBoxSettings;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * @author Niklas Eicker
 */
public class InventoryUtility {
    public static Inventory createInventory(InventoryHolder owner, int size, String title) {
        if (GameBoxSettings.checkInventoryLength && title.length() > 32) {
            title = StringUtility.shorten(title, 32);
        }
        if (owner == null) throw new IllegalArgumentException("InventoryHolder cannot be null");
        return Bukkit.createInventory(owner, size, title);
    }

    public static Inventory createInventory(InventoryHolder owner, InventoryType inventoryType, String title) {
        return Bukkit.createInventory(owner, inventoryType.getDefaultSize(), title);
    }
}
