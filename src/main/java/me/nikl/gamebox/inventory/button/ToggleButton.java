package me.nikl.gamebox.inventory.button;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Niklas Eicker
 */
public class ToggleButton extends Button {
  private ItemStack toggle;
  private String toggleDisplayName = "missing name";
  private List<String> toggleLore = new ArrayList<>(Collections.singletonList("missing lore"));


  public ToggleButton(ItemStack item, ItemStack toggle) {
    super(item);
    this.toggle = toggle;
  }

  public ToggleButton toggle() {
    ItemStack newData = this.toggle.clone();
    ItemMeta meta = getItemMeta();
    String displayName = toggleDisplayName;
    ArrayList<String> lore = new ArrayList<>(toggleLore);

    this.toggle.setAmount(getAmount());
    this.toggle.setType(getType());
    toggleDisplayName = meta.getDisplayName();
    toggleLore = new ArrayList<>(meta.getLore());

    setAmount(newData.getAmount());
    setType(newData.getType());
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
    ToggleButton clone = new ToggleButton(this, toggle);
    clone.setActionAndArgs(this.action, this.args);
    clone.setToggleDisplayName(this.toggleDisplayName);
    clone.setToggleLore(this.toggleLore);
    return clone;
  }
}
