package me.nikl.gamebox.games.cookieclicker.upgrades.alchemylab;

import me.nikl.gamebox.games.cookieclicker.CCGame;
import me.nikl.gamebox.games.cookieclicker.buildings.Buildings;
import me.nikl.gamebox.games.cookieclicker.upgrades.Upgrade;
import me.nikl.gamebox.games.cookieclicker.upgrades.UpgradeType;

/**
 * @author Niklas Eicker
 */
public class BeigeGoo extends Upgrade {

    public BeigeGoo(CCGame game) {
        super(game, 315);
        this.cost = 37500000000000000000000000.;
        productionsRequirements.put(Buildings.ALCHEMY_LAB, 250);

        loadLanguage(UpgradeType.CLASSIC, Buildings.ALCHEMY_LAB);
    }

    @Override
    public void onActivation() {
        game.getBuilding(Buildings.ALCHEMY_LAB).multiply(2);
        game.getBuilding(Buildings.ALCHEMY_LAB).visualize(game.getInventory());
        active = true;
    }


}
