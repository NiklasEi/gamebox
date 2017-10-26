package me.nikl.gamebox.games.cookieclicker.buildings;

import me.nikl.cookieclicker.Main;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

/**
 * Created by Niklas on 05.07.2017.
 */
public class Prism extends Building {

    public Prism(Main plugin, int slot, Buildings building) {
        super(plugin, slot, building);

        icon = new MaterialData(Material.EMERALD).toItemStack();
        icon.setAmount(1);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(lang.GAME_BUILDING_NAME.replace("%name%", name));
        icon.setItemMeta(meta);

        this.productionPerSecond = 2900000000.;
        this.baseCost = 2100000000000000.;
    }
}
