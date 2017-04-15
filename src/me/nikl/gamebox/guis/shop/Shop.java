package me.nikl.gamebox.guis.shop;

import me.nikl.gamebox.ClickAction;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.PluginManager;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import me.nikl.gamebox.guis.gui.AGui;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;


/**
 * Created by Niklas on 13.04.2017.
 *
 *
 */
public class Shop extends AGui{
    FileConfiguration shop;

    ShopManager shopManager;

    public Shop(GameBox plugin, GUIManager guiManager, int slots, ShopManager shopManager) {
        super(plugin, guiManager, slots);
        this.shopManager = shopManager;
        this.shop = shopManager.getShop();

        Map<Integer, ItemStack> hotBarButtons = plugin.getPluginManager().getHotBarButtons();


        // set lower grid
        if (hotBarButtons.get(PluginManager.exit) != null) {
            AButton exit = new AButton(hotBarButtons.get(PluginManager.exit).getData(), 1);
            ItemMeta meta = hotBarButtons.get(PluginManager.exit).getItemMeta();
            exit.setItemMeta(meta);
            exit.setAction(ClickAction.CLOSE);
            setLowerButton(exit, PluginManager.exit);
        }


        if (hotBarButtons.get(PluginManager.toMain) != null) {
            AButton main = new AButton(hotBarButtons.get(PluginManager.toMain).getData(), 1);
            ItemMeta meta = hotBarButtons.get(PluginManager.toMain).getItemMeta();
            main.setItemMeta(meta);
            main.setAction(ClickAction.OPEN_MAIN_GUI);
            setLowerButton(main, PluginManager.toMain);
        }


    }



    protected ItemStack getItemStack(String matDataString){
        Material mat; short data;
        String[] obj = matDataString.split(":");

        if (obj.length == 2) {
            try {
                mat = Material.matchMaterial(obj[0]);
            } catch (Exception e) {
                return null; // material name doesn't exist
            }

            try {
                data = Short.valueOf(obj[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return null; // data not a number
            }

            //noinspection deprecation
            if(mat == null) return null;
            ItemStack stack = new ItemStack(mat, 1);
            stack.setDurability(data);
            return stack;
        } else {
            try {
                mat = Material.matchMaterial(obj[0]);
            } catch (Exception e) {
                return null; // material name doesn't exist
            }
            //noinspection deprecation
            return (mat == null ? null : new ItemStack(mat, 1));
        }
    }
}
