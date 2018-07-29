package me.nikl.gamebox.utility;

import me.nikl.gamebox.GameBox;
import me.nikl.nmsutilities.NmsFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niklas Eicker
 *
 * Utility class for ItemStacks
 */
public class ItemStackUtility {
    public static final String MATERIAL = "materialData";
    public static final String LORE = "lore";
    public static final String NAME = "displayName";
    public static final String GLOW = "glow";
    private static final Map<String, ItemStack> cachedPlayerHeads = new HashMap<>();
    private static final Inventory dummy = Bukkit.createInventory(null, 54, "dummy inv.");

    public static ItemStack getItemStack(String matDataString) {
        Material mat;
        short data;
        if (matDataString == null) return null;
        String[] obj = matDataString.split(":");
        try {
            mat = Material.matchMaterial(obj[0]);
        } catch (Exception e) {
            GameBox.debug(matDataString + " cannot be matched to a material");
            return null;
        }
        if (mat == null) {
            GameBox.debug("matched " + matDataString + " to null");
            return null;
        }
        if (obj.length == 2) {
            try {
                data = Short.valueOf(obj[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return null; // data not a number
            }
            ItemStack stack = new ItemStack(mat, 1);
            stack.setDurability(data);
            return stack;
        } else {
            return new ItemStack(mat, 1);
        }
    }

    public static ItemStack createBookWithText(List<String> text) {
        return createItemWithText(text, new MaterialData(Material.BOOK_AND_QUILL));
    }

    public static ItemStack createItemWithText(List<String> text, MaterialData materialData) {
        ItemStack helpItem = materialData.toItemStack(1);
        ItemMeta meta = helpItem.getItemMeta();
        if (text != null) {
            if (text.size() > 0) meta.setDisplayName(text.get(0));
            if (text.size() > 1) {
                text.remove(0);
                meta.setLore(text);
            }
        }
        helpItem.setItemMeta(meta);
        return helpItem;
    }

    public static ItemStack loadItem(ConfigurationSection section) {
        ItemStack toReturn = getItemStack(section.getString(MATERIAL));
        if (toReturn == null) return null;
        ItemMeta meta = toReturn.getItemMeta();
        if (section.isString(NAME)) {
            meta.setDisplayName(StringUtility.color(section.getString(NAME)));
        }
        if (section.isList(LORE)) {
            meta.setLore(StringUtility.color(section.getStringList(LORE)));
        }
        if (section.getBoolean(GLOW, false)) {
            toReturn = NmsFactory.getNmsUtility().addGlow(toReturn);
        }
        toReturn.setItemMeta(meta);
        return toReturn;
    }

    public static ItemStack getPlayerHead(String name) {
        GameBox.debug("Grabbing head for " + name);
        ItemStack skull = cachedPlayerHeads.get(name);
        if (skull != null) return skull.clone();
        GameBox.debug("Not cached yet...");
        skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwner(name);
        skull.setItemMeta(skullMeta);
        // force profile lookup
        dummy.setItem(0, skull);
        cachedPlayerHeads.put(name, skull);
        GameBox.debug(name + "'s head is cached now");
        return skull.clone();
    }
}
