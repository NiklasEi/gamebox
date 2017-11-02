package me.nikl.gamebox.games.cookieclicker.buildings;

import me.nikl.gamebox.games.cookieclicker.CCGame;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

/**
 * Created by Niklas on 05.07.2017.
 */
public class AntimatterCondenser extends Building {

    public AntimatterCondenser(CCGame plugin, int slot, Buildings building) {
        super(plugin, slot, building);

        icon = new MaterialData(Material.ENDER_PORTAL_FRAME).toItemStack();
        icon.setAmount(1);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(lang.GAME_BUILDING_NAME.replace("%name%", name));
        icon.setItemMeta(meta);

        this.productionPerSecond = 430000000.;
        this.baseCost = 170000000000000.;
    }
}
