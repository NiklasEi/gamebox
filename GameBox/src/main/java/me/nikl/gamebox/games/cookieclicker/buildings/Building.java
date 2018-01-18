package me.nikl.gamebox.games.cookieclicker.buildings;

import me.nikl.gamebox.games.cookieclicker.CCLanguage;
import me.nikl.gamebox.games.cookieclicker.CookieClicker;
import me.nikl.gamebox.utility.NumberUtil;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niklas Eicker
 */
public class Building {
    protected int count = 0;
    protected double baseCost = 0.;
    protected int slot;
    protected ItemStack icon;
    protected double productionPerSecond = 0.;
    protected double multiplier = 1.;
    protected double otherBuildingsBonus = 0.;
    protected List<String > lore;

    protected String name;

    protected CCLanguage lang;

    public Building(CookieClicker plugin, int slot, Buildings building){
        this.lang = (CCLanguage) plugin.getGameLang();
        this.name = lang.buildingName.get(building);

        lore = new ArrayList<>();
        for(String line : lang.buildingLore.get(building)){
            lore.add(line.replace("%name%", name));
        }

        if(slot < 0 || slot > 53){
            slot = 0;
        }

        this.slot = slot;
    }

    public double getAllInAllProductionPerSecond(){
        return  getProductionPerSecondPerItem() * count;
    }

    public void addProductions(int amount){
        this.count += amount;
    }

    /***
     * Add a multiplier
     *
     * 0.5 => 50%
     * 1 => 100%
     * @param toAdd production to add to multiplier
     */
    public void addMultiplier(double toAdd) {
        this.multiplier += toAdd;
    }

    /**
     * Multiply the current production
     *
     * The current multiplier is multiplied by the
     * given factor
     * @param multiplier
     */
    public void multiply(double multiplier){
        this.multiplier = this.multiplier * multiplier;
    }

    public int getCount() {
        return count;
    }

    /**
     * Calculate and return the cost for the next building
     * @return
     */
    public double getCost() {
        return (baseCost * Math.pow(1.15, count));
    }

    public double getProductionPerSecondPerItem() {
        return productionPerSecond * multiplier + otherBuildingsBonus;
    }

    public void visualize(Inventory inventory){
        if(icon == null || lore == null) return;
        List<String> newLore = new ArrayList<>();
        for (String line : lore) {
            newLore.add(line.replace("%count%", String.valueOf(count))
                    .replace("%cost%", NumberUtil.convertHugeNumber(getCost()))
                    .replace("%cookies_per_second_per_item%", NumberUtil.convertHugeNumber(getProductionPerSecondPerItem()))
                    .replace("%cookies_per_second%", NumberUtil.convertHugeNumber(getAllInAllProductionPerSecond()))
                    .replace("%cost_long%", NumberUtil.convertHugeNumber(getCost(), false))
                    .replace("%cookies_per_second_per_item_long%", NumberUtil.convertHugeNumber(getProductionPerSecondPerItem(), false))
                    .replace("%cookies_per_second_long%", NumberUtil.convertHugeNumber(getAllInAllProductionPerSecond(), false)));
        }
        ItemMeta meta = icon.getItemMeta();
        meta.setLore(newLore);
        icon.setItemMeta(meta);

        inventory.setItem(slot, icon);
    }

    public void setOtherBuildingsBonus(double bonus){
        this.otherBuildingsBonus = bonus;
    }

    public ItemStack getIcon(){
        return icon.clone();
    }
}
