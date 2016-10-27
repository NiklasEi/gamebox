package me.nikl.gamebox.guis;

import org.bukkit.inventory.ItemStack;

/**
 * Created by niklas on 10/27/16.
 *
 * abstract button class implementing IButton
 */
public abstract class AButton implements IButton {
	
	public ItemStack item;
	
	@Override
	public ItemStack getItem(){
		return this.item;
	}
}
