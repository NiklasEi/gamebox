package me.nikl.gamebox.games.cookieclicker.upgrades.wizardtower;

import me.nikl.gamebox.games.cookieclicker.CookieClicker;
import me.nikl.gamebox.games.cookieclicker.buildings.Buildings;
import me.nikl.gamebox.games.cookieclicker.upgrades.Upgrade;
import me.nikl.gamebox.games.cookieclicker.upgrades.UpgradeType;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

/**
 * Created by Niklas on 09.07.2017.
 *
 */
public class SchoolOfSorcery extends Upgrade{

    public SchoolOfSorcery(CookieClicker game) {
        super(game, 248);
        this.cost = 1650000000000000.;
        productionsRequirements.put(Buildings.WIZARD_TOWER, 100);

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
