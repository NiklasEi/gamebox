package me.nikl.gamebox.games.cookieclicker.upgrades.prism;

import me.nikl.gamebox.games.cookieclicker.CCGame;
import me.nikl.gamebox.games.cookieclicker.buildings.Buildings;
import me.nikl.gamebox.games.cookieclicker.upgrades.Upgrade;
import me.nikl.gamebox.games.cookieclicker.upgrades.UpgradeType;

/**
 * @author Niklas Eicker
 */
public class GlowInTheDark extends Upgrade {

    public GlowInTheDark(CCGame game) {
        super(game, 201);
        this.cost = 1050000000000000000000000.;
        productionsRequirements.put(Buildings.PRISM, 150);

        // for the standard upgrade type the building icon is used
        loadLanguage(UpgradeType.CLASSIC, Buildings.PRISM);
    }

    @Override
    public void onActivation() {
        game.getBuilding(Buildings.PRISM).multiply(2);
        game.getBuilding(Buildings.PRISM).visualize(game.getInventory());
        active = true;
    }
}
