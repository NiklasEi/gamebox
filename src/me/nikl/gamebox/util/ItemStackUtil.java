package me.nikl.gamebox.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Niklas on 13.04.2017.
 *
 * Utility class for ItemStacks
 */
public class ItemStackUtil {

    public static ItemStack getItemStack(String matDataString){
        Material mat; short data;
        if(matDataString == null) return null;
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
