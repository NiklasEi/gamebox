package me.nikl.gamebox.games.cookieclicker.buildings;

import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.games.cookieclicker.CookieClicker;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

/**
 * Created by Niklas on 05.07.2017.
 */
public class Mine extends Building {

    public Mine(CookieClicker plugin, int slot, Buildings building) {
        super(plugin, slot, building);

        icon = new MaterialData(Material.DIAMOND_PICKAXE).toItemStack();
        icon.setAmount(1);
        ItemMeta meta = icon.getItemMeta();
        if(!GameBoxSettings.version1_8) meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(lang.GAME_BUILDING_NAME.replace("%name%", name));
        icon.setItemMeta(meta);

        this.productionPerSecond = 47;
        this.baseCost = 12000;
    }
}
