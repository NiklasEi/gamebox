package me.nikl.gamebox.inventory.button;

import me.nikl.gamebox.inventory.ClickAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niklas Eicker
 */
public class DisplayButton extends AButton {
    protected Map<String, Object> displays = new HashMap<>();
    private List<String> defaultLore;
    private String displayName;
    public DisplayButton(ItemStack item, String displayName, List<String> defaultLore) {
        super(item);
        setAction(ClickAction.NOTHING);
        this.defaultLore = defaultLore;
        this.displayName = displayName;
    }

    public void addDisplay(String replace, Object display) {
        displays.put(replace, display);
    }

    public void update(){
        List<String > updatedLore = new ArrayList<>(defaultLore);
        String updatedName = displayName;
        for(String toReplace : displays.keySet()){
            for(int i = 0; i < defaultLore.size(); i++) {
                updatedLore.set(i, updatedLore.get(i).replace(toReplace, String.valueOf(displays.get(toReplace))));
            }
            updatedName = updatedName.replace(toReplace, String.valueOf(displays.get(toReplace)));
        }
        ItemMeta meta = getItemMeta();
        meta.setLore(updatedLore);
        meta.setDisplayName(updatedName);
        setItemMeta(meta);
    }

    public DisplayButton clone(){
        DisplayButton toReturn = new DisplayButton(this, displayName, defaultLore);
        toReturn.displays = new HashMap<>(this.displays);
        toReturn.update();
        return toReturn;
    }
}
