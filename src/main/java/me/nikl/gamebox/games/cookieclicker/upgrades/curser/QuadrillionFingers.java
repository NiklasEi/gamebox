package me.nikl.gamebox.games.cookieclicker.upgrades.curser;

import me.nikl.gamebox.games.cookieclicker.CCGame;
import me.nikl.gamebox.games.cookieclicker.buildings.Buildings;
import me.nikl.gamebox.games.cookieclicker.upgrades.Upgrade;
import me.nikl.gamebox.games.cookieclicker.upgrades.UpgradeType;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

/**
 * @author Niklas Eicker
 */
public class QuadrillionFingers extends Upgrade {

    public QuadrillionFingers(CCGame game) {
        super(game, 43);
        this.cost = 10000000000.;
        productionsRequirements.put(Buildings.CURSOR, 200);

        icon = new MaterialData(Material.ARROW).toItemStack();
        icon.setAmount(1);

        gain = "+500";
        loadLanguage(UpgradeType.GAIN_MOUSE_AND_OTHER, Buildings.CURSOR);
    }

    @Override
    public void onActivation() {
        for (Buildings buildings : Buildings.values()) {
            if (buildings == Buildings.CURSOR) continue;
            game.addBuildingBonus(Buildings.CURSOR, buildings, 500);
            game.addClickBonus(buildings, 500);
        }
        active = true;
    }


}
