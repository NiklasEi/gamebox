package me.nikl.gamebox.games.cookieclicker.upgrades.temple;

import me.nikl.gamebox.games.cookieclicker.CCGame;
import me.nikl.gamebox.games.cookieclicker.buildings.Buildings;
import me.nikl.gamebox.games.cookieclicker.upgrades.Upgrade;
import me.nikl.gamebox.games.cookieclicker.upgrades.UpgradeType;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

/**
 * @author Niklas Eicker
 */
public class GoldenIdols extends Upgrade {

    public GoldenIdols(CCGame game) {
        super(game, 238);
        this.cost = 200000000.;
        productionsRequirements.put(Buildings.TEMPLE, 1);

        icon = new MaterialData(Material.ENCHANTMENT_TABLE).toItemStack();
        icon.setAmount(1);

        loadLanguage(UpgradeType.CLASSIC, Buildings.TEMPLE);
    }

    @Override
    public void onActivation() {
        game.getBuilding(Buildings.TEMPLE).multiply(2);
        game.getBuilding(Buildings.TEMPLE).visualize(game.getInventory());
        active = true;
    }


}
