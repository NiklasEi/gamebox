package me.nikl.gamebox.guis.button;

import me.nikl.gamebox.util.ClickAction;
import me.nikl.gamebox.guis.gui.AGui;
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
	
	
	@SuppressWarnings("deprecation")
	public AButton(MaterialData mat, int count){
		super(mat.getItemType());
		this.setData(mat);
		this.setDurability(mat.getData());
		this.setAmount(count);
	}

	public AButton(ItemStack item){
		super(item);
	}
	
	public void setAction(ClickAction action){
		this.action = action;
	}

	public AButton setActionAndArgs(ClickAction action, String... args){
		this.action = action;
		this.args = args;
		return this;
	}
	
	public void setArgs(String... args){
		this.args = args;
	}

	public ClickAction getAction(){
		return this.action;
	}

	public String[] getArgs(){
		return this.args;
	}
	
}
