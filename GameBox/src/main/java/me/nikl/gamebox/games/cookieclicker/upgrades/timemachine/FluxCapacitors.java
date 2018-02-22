package me.nikl.gamebox.games.cookieclicker.upgrades.timemachine;

import me.nikl.gamebox.games.cookieclicker.CCGame;
import me.nikl.gamebox.games.cookieclicker.buildings.Buildings;
import me.nikl.gamebox.games.cookieclicker.upgrades.Upgrade;
import me.nikl.gamebox.games.cookieclicker.upgrades.UpgradeType;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

/**
 * @author Niklas Eicker
 */
public class FluxCapacitors extends Upgrade {

    public FluxCapacitors(CCGame game) {
        super(game, 28);
        this.cost = 140000000000000.;
        productionsRequirements.put(Buildings.TIME_MACHINE, 1);

        icon = new MaterialData(Material.WATCH).toItemStack();
        icon.setAmount(1);

        loadLanguage(UpgradeType.CLASSIC, Buildings.TIME_MACHINE);
    }

    @Override
    public void onActivation() {
        game.getBuilding(Buildings.TIME_MACHINE).multiply(2);
        game.getBuilding(Buildings.TIME_MACHINE).visualize(game.getInventory());
        active = true;
    }
}
