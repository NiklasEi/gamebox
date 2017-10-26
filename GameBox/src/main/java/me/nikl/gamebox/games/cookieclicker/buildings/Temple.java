package me.nikl.gamebox.games.cookieclicker.buildings;

import me.nikl.gamebox.games.cookieclicker.Main;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

/**
 * Created by Niklas on 05.07.2017.
 */
public class Temple extends Building {

    public Temple(Main plugin, int slot, Buildings building) {
        super(plugin, slot, building);

        icon = new MaterialData(Material.ENCHANTMENT_TABLE).toItemStack();
        icon.setAmount(1);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(lang.GAME_BUILDING_NAME.replace("%name%", name));
        icon.setItemMeta(meta);

        this.productionPerSecond = 7800;
        this.baseCost = 20000000;
    }
}
