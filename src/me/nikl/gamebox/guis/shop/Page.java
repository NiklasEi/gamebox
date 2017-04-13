package me.nikl.gamebox.guis.shop;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.guis.GUIManager;

/**
 * Created by Niklas on 13.04.2017.
 *
 * save a shop page
 */
public class Page extends Shop{
    private int page;
    public Page(GameBox plugin, GUIManager guiManager, int slots, int page, ShopManager shopManager) {
        super(plugin, guiManager, slots, shopManager);
        this.page = page;
    }

    protected int getPage(){
        return this.page;
    }
}
