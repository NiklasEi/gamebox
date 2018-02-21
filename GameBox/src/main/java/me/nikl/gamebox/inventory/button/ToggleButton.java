package me.nikl.gamebox.inventory.button;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Niklas Eicker
 */
public class ToggleButton extends Button {
    private boolean toggled = false;
    private MaterialData toggleData;
    private String toggleDisplayName = "missing name";
    private List<String> toggleLore = new ArrayList<>(Arrays.asList("missing lore"));

    // ToDo: get rid of MaterialData / Data usage for mc 1.13

    @Deprecated
    public ToggleButton(ItemStack item, MaterialData mat2) {
        super(item);
        this.toggleData = mat2;
    }

    @SuppressWarnings("deprecation")
    public ToggleButton toggle() {
        toggled = !toggled;
        MaterialData mat = toggleData;
        ItemMeta meta = getItemMeta();
        String displayName = toggleDisplayName;
        ArrayList<String> lore = new ArrayList<>(toggleLore);

        toggleData = getData();
        toggleDisplayName = meta.getDisplayName();
        toggleLore = new ArrayList<>(meta.getLore());

        setData(mat);
        setType(mat.getItemType());
        setDurability(mat.getData());
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.values());
        setItemMeta(meta);
        return this;
    }

    public void setToggleDisplayName(String toggleDisplayName) {
        this.toggleDisplayName = toggleDisplayName;
    }

    public void setToggleLore(List<String> toggleLore) {
        this.toggleLore = toggleLore;
    }

    @Override
    public Button clone() {
        ToggleButton clone = new ToggleButton(this, toggled?getData():toggleData);
        clone.setActionAndArgs(this.action, this.args);
        clone.setToggleDisplayName(toggled?getItemMeta().getDisplayName():toggleDisplayName);
        clone.setToggleLore(toggled?getItemMeta().getLore():toggleLore);
        return clone;
    }
}
