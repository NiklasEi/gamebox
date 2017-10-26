package me.nikl.gamebox.games.cookieclicker.buildings;

import me.nikl.cookieclicker.Main;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.material.MaterialData;

/**
 * Created by Niklas on 05.07.2017.
 */
public class AlchemyLab extends Building {

    public AlchemyLab(Main plugin, int slot, Buildings building) {
        super(plugin, slot, building);

        // old minecraft versions are missing the material SPLASH_POTION
        Material mat;
        try{
            mat = Material.SPLASH_POTION;
        } catch (NoSuchFieldError tooOldVersion){
            mat = Material.POTION;
        }

        icon = new MaterialData(mat).toItemStack();
        icon.setAmount(1);
        PotionMeta meta = (PotionMeta) icon.getItemMeta();
        meta.addItemFlags(ItemFlag.values());
        meta.setDisplayName(lang.GAME_BUILDING_NAME.replace("%name%", name));
        icon.setItemMeta(meta);

        this.productionPerSecond = 1600000.;
        this.baseCost = 75000000000.;
    }
}
