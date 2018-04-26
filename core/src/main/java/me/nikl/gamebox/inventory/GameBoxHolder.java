package me.nikl.gamebox.inventory;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Created by nikl on 28.02.18.
 */
public interface GameBoxHolder extends InventoryHolder {
    /**
     * Handle a click in the game
     *
     * @param event ClickEvent
     */
    void onInventoryClick(InventoryClickEvent event);


    /**
     * Handle an InventoryCloseEvent
     * You should do the same as in IGameManager.removeFromGame
     *
     * @param event CloseEvent
     */
    void onInventoryClose(InventoryCloseEvent event);
}
