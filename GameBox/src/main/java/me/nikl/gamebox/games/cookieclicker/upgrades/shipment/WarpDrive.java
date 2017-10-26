package me.nikl.gamebox.games.cookieclicker.upgrades.shipment;

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
public class WarpDrive extends Upgrade{

    public WarpDrive(Game game) {
        super(game, 48);
        this.cost = 255000000000000.;
        productionsRequirements.put(Buildings.SHIPMENT, 50);

        icon = new MaterialData(Material.FIREWORK).toItemStack();
        icon.setAmount(1);

        loadLanguage(UpgradeType.CLASSIC, Buildings.SHIPMENT);
    }

    @Override
    public void onActivation() {
        game.getBuilding(Buildings.SHIPMENT).multiply(2);
        game.getBuilding(Buildings.SHIPMENT).visualize(game.getInventory());
        active = true;
    }
}
