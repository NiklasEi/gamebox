package me.nikl.gamebox.guis.shop;

import me.nikl.gamebox.guis.GUIManager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Niklas on 13.04.2017.
 *
 *
 */
public class Category {

    private Map<Integer, Page> pages;
    private String key;
    private ShopManager shopManager;

    public Category(ShopManager shopManager, GUIManager guiManager, String key){
        this.key = key;
        this.shopManager = shopManager;

        pages = new HashMap<>();
    }

    protected boolean nextPage(UUID player){
        // put the player in the next page
        return false;
    }

    protected boolean lastPage(UUID player){
        //if(player on first page) go back to shop (MainShop)
        // put the player in the page before
        return false;
    }

    protected boolean inCategory(UUID uuid){
        for(Page page : pages.values()){
            if(page.isInGui(uuid)) return true;
        }
        return false;
    }

    public void onInvClick(InventoryClickEvent event) {
        for(Page page : pages.values()){
            if(page.isInGui(event.getWhoClicked().getUniqueId())) {
                page.onInvClick(event);
                return;
            }
        }
    }

    public void onBottomInvClick(InventoryClickEvent event) {
        for(Page page : pages.values()){
            if(page.isInGui(event.getWhoClicked().getUniqueId())) {
                page.onBottomInvClick(event);
                return;
            }
        }
    }

    public void onInvClose(InventoryCloseEvent event) {
        for(Page page : pages.values()){
            if(page.isInGui(event.getPlayer().getUniqueId())) {
                page.onInvClose(event);
                return;
            }
        }
    }
}
