package me.nikl.gamebox.inventory.shop;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niklas Eicker
 */
public class ShopItem {
    private List<String> commands = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();
    private List<String> noPermissions = new ArrayList<>();

    private boolean manipulatesInventory = false;

    private ItemStack itemStack;


    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public List<String> getNoPermissions() {
        return noPermissions;
    }

    public void setNoPermissions(List<String> noPermissions) {
        this.noPermissions = noPermissions;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public boolean manipulatesInventory() {
        return manipulatesInventory;
    }

    public void setManipulatesInventory(boolean manipulatesInventory) {
        this.manipulatesInventory = manipulatesInventory;
    }
}
