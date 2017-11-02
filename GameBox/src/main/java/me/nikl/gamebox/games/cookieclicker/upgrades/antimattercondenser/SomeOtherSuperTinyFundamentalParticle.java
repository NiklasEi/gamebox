package me.nikl.gamebox.games.cookieclicker.upgrades.antimattercondenser;

import me.nikl.gamebox.games.cookieclicker.CookieClicker;
import me.nikl.gamebox.games.cookieclicker.buildings.Buildings;
import me.nikl.gamebox.games.cookieclicker.upgrades.Upgrade;
import me.nikl.gamebox.games.cookieclicker.upgrades.UpgradeType;

/**
 * Created by Niklas on 09.07.2017.
 *
 */
public class SomeOtherSuperTinyFundamentalParticle extends Upgrade{

    public SomeOtherSuperTinyFundamentalParticle(CookieClicker game) {
        super(game, 318);
        this.cost = 85000000000000000000000000000.;
        productionsRequirements.put(Buildings.ANTIMATTER_CONDENSER, 250);

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
