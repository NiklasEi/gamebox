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
public class QuintillionFingers extends Upgrade{

    public QuintillionFingers(Game game) {
        super(game, 82);
        this.cost = 10000000000000.;
        productionsRequirements.put(Buildings.CURSOR, 250);

        icon = new MaterialData(Material.ARROW).toItemStack();
        icon.setAmount(1);

        gain = "+5000";
        loadLanguage(UpgradeType.GAIN_MOUSE_AND_OTHER, Buildings.CURSOR);
    }

    @Override
    public void onActivation() {
        for(Buildings buildings : Buildings.values()){
            if (buildings == Buildings.CURSOR) continue;
            game.addBuildingBonus(Buildings.CURSOR, buildings, 5000);
            game.addClickBonus(buildings, 5000);
        }
        active = true;
    }


}
