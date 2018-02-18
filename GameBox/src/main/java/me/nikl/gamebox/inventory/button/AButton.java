package me.nikl.gamebox.inventory.button;

import me.nikl.gamebox.inventory.ClickAction;
import org.bukkit.inventory.ItemStack;

/**
 * Created by niklas on 2/5/17.
 */
public class AButton extends ItemStack {
    private ClickAction action;
    private String[] args;


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

    @Override
    public AButton clone() {
        AButton clone = new AButton(this);
        clone.setActionAndArgs(this.action, this.args);
        return clone;
    }
}
