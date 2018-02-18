package me.nikl.gamebox.inventory;

import me.nikl.gamebox.inventory.button.AButton;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Map;
import java.util.UUID;

/**
 * Created by nikl on 17.02.18.
 */
public class GameBoxHolder implements InventoryHolder {
    private Inventory inventory;
    private Map<UUID, Inventory> playerInventories;
    private Map<Integer, AButton> buttons;

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void onClick(InventoryClickEvent event){

    }


}
