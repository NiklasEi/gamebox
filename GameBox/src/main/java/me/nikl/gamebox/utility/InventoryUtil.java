package me.nikl.gamebox.utility;

import me.nikl.gamebox.GameBoxSettings;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Created by nikl on 30.11.17.
 * 
 */
public class InventoryUtil {

    public static Inventory createInventory(InventoryHolder owner, int size, String title){
        if(GameBoxSettings.checkInventoryLength && title.length() > 32){
            title = StringUtil.shorten(title, 32);
        }
        return Bukkit.createInventory(owner, size, title);
    }

    public static Inventory createInventory(InventoryHolder owner, InventoryType inventoryType, String title){
        if(GameBoxSettings.checkInventoryLength && title.length() > 32){
            title = StringUtil.shorten(title, 32);
        }
        return Bukkit.createInventory(owner, inventoryType, title);
    }
}
