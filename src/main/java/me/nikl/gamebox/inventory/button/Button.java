package me.nikl.gamebox.inventory.button;

import org.bukkit.inventory.ItemStack;

/**
 * @author Niklas Eicker
 */
public class Button extends AButton {
    public Button(ItemStack item) {
        super(item);
    }

    @Override
    public AButton clone() {
        Button clone = new Button(this);
        clone.setActionAndArgs(this.action, this.args);
        return clone;
    }
}
