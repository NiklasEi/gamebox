package me.nikl.gamebox.guis.button;

import me.nikl.gamebox.ClickAction;
import me.nikl.gamebox.guis.gui.AGui;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

/**
 * Created by niklas on 2/5/17.
 *
 */
public class AButton extends ItemStack{
	private ClickAction action;
	private AGui gui;
	private String[] args;
	
	
	public AButton(MaterialData mat, int count){
		super(mat.getItemType());
		this.setData(mat);
		this.setAmount(count);
	}
	
	public void setAction(ClickAction action){
		this.action = action;
	}
	
	public boolean onClick(InventoryClickEvent event){
		return gui.action(event, action, args);
	}
	
	public void setArgs(String... args){
		this.args = args;
	}
	
}
