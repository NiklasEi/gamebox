package me.nikl.gamebox.games.cookieclicker.upgrades.mine;

import me.nikl.gamebox.games.cookieclicker.Game;
import me.nikl.gamebox.games.cookieclicker.buildings.Buildings;
import me.nikl.gamebox.games.cookieclicker.upgrades.Upgrade;
import me.nikl.gamebox.games.cookieclicker.upgrades.UpgradeType;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

/**
 * Created by Niklas on 09.07.2017.
 *
 */
public class CanolaOilWells extends Upgrade{

    public CanolaOilWells(Game game) {
        super(game, 309);
        this.cost = 6000000000000000000.;
        productionsRequirements.put(Buildings.MINE, 250);

        icon = new MaterialData(Material.DIAMOND_PICKAXE).toItemStack();
        icon.setAmount(1);

        loadLanguage(UpgradeType.CLASSIC, Buildings.MINE);
    }

    @Override
    public void onActivation() {
        game.getBuilding(Buildings.MINE).multiply(2);
        game.getBuilding(Buildings.MINE).visualize(game.getInventory());
        active = true;
    }


}
