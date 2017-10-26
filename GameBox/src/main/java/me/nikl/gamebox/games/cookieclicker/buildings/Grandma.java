package me.nikl.gamebox.games.cookieclicker.buildings;

import me.nikl.gamebox.games.cookieclicker.Main;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * Created by Niklas on 07.07.2017.
 */
public class Grandma extends Building {

    public Grandma(Main plugin, int slot, Buildings building) {
        super(plugin, slot, building);

        icon = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        icon.setAmount(1);
        SkullMeta skullMeta = (SkullMeta) icon.getItemMeta();
        skullMeta.setOwner("MHF_Villager");
        skullMeta.setDisplayName(lang.GAME_BUILDING_NAME.replace("%name%", name));
        icon.setItemMeta(skullMeta);

        this.baseCost = 100;
        this.productionPerSecond = 1;
    }
}
