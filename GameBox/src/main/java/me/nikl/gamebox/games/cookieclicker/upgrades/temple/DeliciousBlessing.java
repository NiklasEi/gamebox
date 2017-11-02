package me.nikl.gamebox.games.cookieclicker.upgrades.temple;

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
public class DeliciousBlessing extends Upgrade{

    public DeliciousBlessing(CookieClicker game) {
        super(game, 240);
        this.cost = 10000000000.;
        productionsRequirements.put(Buildings.TEMPLE, 25);

        icon = new MaterialData(Material.ENCHANTMENT_TABLE).toItemStack();
        icon.setAmount(1);

        loadLanguage(UpgradeType.CLASSIC, Buildings.TEMPLE);
    }

    @Override
    public void onActivation() {
        game.getBuilding(Buildings.TEMPLE).multiply(2);
        game.getBuilding(Buildings.TEMPLE).visualize(game.getInventory());
        active = true;
    }


}
