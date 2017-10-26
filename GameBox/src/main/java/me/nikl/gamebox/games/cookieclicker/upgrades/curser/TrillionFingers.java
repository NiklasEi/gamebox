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
public class TrillionFingers extends Upgrade{

    public TrillionFingers(Game game) {
        super(game, 6);
        this.cost = 1000000000;
        productionsRequirements.put(Buildings.CURSOR, 150);

        icon = new MaterialData(Material.ARROW).toItemStack();
        icon.setAmount(1);

        gain = "+50";
        loadLanguage(UpgradeType.GAIN_MOUSE_AND_OTHER, Buildings.CURSOR);
    }

    @Override
    public void onActivation() {
        for(Buildings buildings : Buildings.values()){
            if (buildings == Buildings.CURSOR) continue;
            game.addBuildingBonus(Buildings.CURSOR, buildings, 50);
            game.addClickBonus(buildings, 50);
        }
        active = true;
    }


}
