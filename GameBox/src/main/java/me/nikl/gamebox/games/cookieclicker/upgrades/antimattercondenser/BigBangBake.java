package me.nikl.gamebox.games.cookieclicker.upgrades.antimattercondenser;

import me.nikl.gamebox.games.cookieclicker.CCGame;
import me.nikl.gamebox.games.cookieclicker.buildings.Buildings;
import me.nikl.gamebox.games.cookieclicker.upgrades.Upgrade;
import me.nikl.gamebox.games.cookieclicker.upgrades.UpgradeType;

/**
 * @author Niklas Eicker
 *
 */
public class BigBangBake extends Upgrade{

    public BigBangBake(CCGame game) {
        super(game, 102);
        this.cost = 8500000000000000000.;
        productionsRequirements.put(Buildings.ANTIMATTER_CONDENSER, 50);

        // for the standard upgrade type the building icon is used
        loadLanguage(UpgradeType.CLASSIC, Buildings.ANTIMATTER_CONDENSER);
    }

    @Override
    public void onActivation() {
        game.getBuilding(Buildings.ANTIMATTER_CONDENSER).multiply(2);
        game.getBuilding(Buildings.ANTIMATTER_CONDENSER).visualize(game.getInventory());
        active = true;
    }
}
