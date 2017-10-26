package me.nikl.gamebox.games.cookieclicker.upgrades.wizardtower;

import me.nikl.cookieclicker.Game;
import me.nikl.cookieclicker.buildings.Buildings;
import me.nikl.cookieclicker.upgrades.Upgrade;
import me.nikl.cookieclicker.upgrades.UpgradeType;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

/**
 * Created by Niklas on 09.07.2017.
 *
 */
public class DarkFormulas extends Upgrade{

    public DarkFormulas(Game game) {
        super(game, 249);
        this.cost = 165000000000000000.;
        productionsRequirements.put(Buildings.WIZARD_TOWER, 150);

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
