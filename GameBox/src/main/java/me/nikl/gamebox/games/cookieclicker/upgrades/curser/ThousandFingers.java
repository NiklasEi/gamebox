package me.nikl.gamebox.games.cookieclicker.upgrades.curser;

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
public class ThousandFingers extends Upgrade{

    public ThousandFingers(Game game) {
        super(game, 3);
        this.cost = 100000;
        productionsRequirements.put(Buildings.CURSOR, 25);

        icon = new MaterialData(Material.ARROW).toItemStack();
        icon.setAmount(1);

        gain = "+0.1";
        loadLanguage(UpgradeType.GAIN_MOUSE_AND_OTHER, Buildings.CURSOR);
    }

    @Override
    public void onActivation() {
        for(Buildings buildings : Buildings.values()){
            if (buildings == Buildings.CURSOR) continue;
            game.addBuildingBonus(Buildings.CURSOR, buildings, 0.1);
            game.addClickBonus(buildings, 0.1);
        }
        active = true;
    }


}
