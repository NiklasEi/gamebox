package me.nikl.gamebox.games.cookieclicker.upgrades.grandma;

import me.nikl.gamebox.games.cookieclicker.CCGame;
import me.nikl.gamebox.games.cookieclicker.buildings.Buildings;
import me.nikl.gamebox.games.cookieclicker.upgrades.Upgrade;
import me.nikl.gamebox.games.cookieclicker.upgrades.UpgradeType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * @author Niklas Eicker
 */
public class ForwardsFromGrandma extends Upgrade {

    public ForwardsFromGrandma(CCGame game) {
        super(game, 7);
        this.cost = 1000;
        productionsRequirements.put(Buildings.GRANDMA, 1);

        icon = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        icon.setAmount(1);
        SkullMeta skullMeta = (SkullMeta) icon.getItemMeta();
        skullMeta.setOwner("MHF_Villager");
        icon.setItemMeta(skullMeta);

        loadLanguage(UpgradeType.CLASSIC, Buildings.GRANDMA);
    }

    @Override
    public void onActivation() {
        game.getBuilding(Buildings.GRANDMA).multiply(2);
        game.getBuilding(Buildings.GRANDMA).visualize(game.getInventory());
        active = true;
    }


}
