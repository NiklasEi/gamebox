package me.nikl.gamebox.games.cookieclicker.upgrades.shipment;

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
public class FrequentFlyer extends Upgrade{

    public FrequentFlyer(CookieClicker game) {
        super(game, 21);
        this.cost = 2550000000000.;
        productionsRequirements.put(Buildings.SHIPMENT, 25);

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
