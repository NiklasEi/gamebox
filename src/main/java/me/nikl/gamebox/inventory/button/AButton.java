package me.nikl.gamebox.inventory.button;

import me.nikl.gamebox.inventory.ClickAction;
import org.bukkit.inventory.ItemStack;

/**
 * @author Niklas Eicker
 */
public abstract class AButton extends ItemStack {
    protected ClickAction action;
    protected String[] args;


    public AButton(ItemStack item) {
        super(item);
        if (getAmount() < 1) setAmount(1);
    }

    public AButton setActionAndArgs(ClickAction action, String... args) {
        this.action = action;
        this.args = args;
        return this;
    }

    public ClickAction getAction() {
        return this.action;
    }

    public void setAction(ClickAction action) {
        this.action = action;
    }

    public String[] getArgs() {
        return this.args;
    }

    public void setArgs(String... args) {
        this.args = args;
    }

    public abstract AButton clone();
}
