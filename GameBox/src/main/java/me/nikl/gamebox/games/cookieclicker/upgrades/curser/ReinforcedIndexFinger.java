package me.nikl.gamebox.games.cookieclicker.upgrades.curser;

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
public class ReinforcedIndexFinger extends Upgrade{

    public ReinforcedIndexFinger(Game game) {
        super(game, 0);
        this.cost = 100;
        productionsRequirements.put(Buildings.CURSOR, 1);

        icon = new MaterialData(Material.ARROW).toItemStack();
        icon.setAmount(1);

        loadLanguage(UpgradeType.CLASSIC_MOUSE, Buildings.CURSOR);
    }

    @Override
    public void onActivation() {
        game.baseCookiesPerClick = game.baseCookiesPerClick * 2;
        game.getBuilding(Buildings.CURSOR).multiply(2);
        game.getBuilding(Buildings.CURSOR).visualize(game.getInventory());
        active = true;
    }


}
