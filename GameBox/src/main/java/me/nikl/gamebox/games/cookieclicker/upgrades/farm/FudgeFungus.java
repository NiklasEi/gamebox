package me.nikl.gamebox.games.cookieclicker.upgrades.farm;

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
public class FudgeFungus extends Upgrade{

    public FudgeFungus(CookieClicker game) {
        super(game, 295);
        this.cost = 550000000000000.;
        productionsRequirements.put(Buildings.FARM, 200);

        icon = new MaterialData(Material.DIRT).toItemStack();
        icon.setAmount(1);

        loadLanguage(UpgradeType.CLASSIC, Buildings.FARM);
    }

    @Override
    public void onActivation() {
        game.getBuilding(Buildings.FARM).multiply(2);
        game.getBuilding(Buildings.FARM).visualize(game.getInventory());
        active = true;
    }


}
