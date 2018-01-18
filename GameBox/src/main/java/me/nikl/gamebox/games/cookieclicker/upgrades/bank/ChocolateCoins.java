package me.nikl.gamebox.games.cookieclicker.upgrades.bank;

import me.nikl.gamebox.games.cookieclicker.CCGame;
import me.nikl.gamebox.games.cookieclicker.buildings.Buildings;
import me.nikl.gamebox.games.cookieclicker.upgrades.Upgrade;
import me.nikl.gamebox.games.cookieclicker.upgrades.UpgradeType;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

/**
 * @author Niklas Eicker
 *
 */
public class ChocolateCoins extends Upgrade{

    public ChocolateCoins(CCGame game) {
        super(game, 235);
        this.cost = 70000000000.;
        productionsRequirements.put(Buildings.BANK, 50);

        icon = new MaterialData(Material.GOLD_NUGGET).toItemStack();
        icon.setAmount(1);

        loadLanguage(UpgradeType.CLASSIC, Buildings.BANK);
    }

    @Override
    public void onActivation() {
        game.getBuilding(Buildings.BANK).multiply(2);
        game.getBuilding(Buildings.BANK).visualize(game.getInventory());
        active = true;
    }


}
