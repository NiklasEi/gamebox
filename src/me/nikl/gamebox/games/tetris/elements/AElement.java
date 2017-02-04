package me.nikl.gamebox.games.tetris.elements;

import me.nikl.gamebox.games.tetris.Position;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

/**
 * Created by niklas on 10/31/16.
 *
 */
public abstract class AElement implements IElement{
	protected ItemStack item;
	protected int[] slots;
	protected Position position;
	protected int center;
	
	public AElement(int durability){
		this(durability, (new Random()).nextInt(9) - 18);
	}
	
	public AElement(int durability, int center){
		position = Position.getRandom();
		
		this.item = new ItemStack(Material.STAINED_CLAY, 1);
		this.item.setDurability((short) durability);
		
		ItemMeta meta = this.item.getItemMeta();
		meta.setDisplayName("a");
		this.item.setItemMeta(meta);
		
		this.center = center;
		
		this.slots = new int[4];
		
		this.setCenter(center);
	}
	
	@Override
	public void setSlotsAroundCenter(int a, int b, int c, int d){
		this.slots[0] = center + a;
		this.slots[1] = center + b;
		this.slots[2] = center + c;
		this.slots[3] = center + d;
	}
	
	@Override
	public int[] getSlots(){
		return this.slots;
	}
	
	@Override
	public int getCenter(){
		return this.center;
	}
	
	@Override
	public ItemStack getItem(){
		return this.item;
	}
	
	@Override
	public void setPosition(Position position){
		this.position = position;
		setCenter(center);
	}
	
	@Override
	public Position getPosition(){
		return this.position;
	}
}
