package me.nikl.gamebox.games.cookieclicker.upgrades.wizardtower;

import me.nikl.gamebox.games.cookieclicker.CCGame;
import me.nikl.gamebox.games.cookieclicker.buildings.Buildings;
import me.nikl.gamebox.games.cookieclicker.upgrades.Upgrade;
import me.nikl.gamebox.games.cookieclicker.upgrades.UpgradeType;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

/**
 * @author Niklas Eicker
 */
public class PointierHats extends Upgrade {

    public PointierHats(CCGame game) {
        super(game, 244);
        this.cost = 3300000000.;
        productionsRequirements.put(Buildings.WIZARD_TOWER, 1);

        icon = new MaterialData(Material.BLAZE_ROD).toItemStack();
        icon.setAmount(1);

        loadLanguage(UpgradeType.CLASSIC, Buildings.WIZARD_TOWER);
    }

    @Override
    public void onActivation() {
        game.getBuilding(Buildings.WIZARD_TOWER).multiply(2);
        game.getBuilding(Buildings.WIZARD_TOWER).visualize(game.getInventory());
        active = true;
    }


}
