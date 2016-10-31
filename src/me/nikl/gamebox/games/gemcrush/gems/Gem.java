package me.nikl.gamebox.games.gemcrush.gems;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Created by niklas on 10/3/16.
 *
 * Abstract class for all Gems
 */
public abstract class Gem {
	ItemStack item;
	ArrayList<String> lore;
	String name;
	int pointsOnBreak;
	
	Gem() {
	}
	
	
	public abstract void onBreak();
	
	Gem(Material material, String name){
		this.item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
		item.setAmount(1);
		this.name = name;
		
		this.pointsOnBreak = 10;
		
		shine(false);
	}
	
	
	Gem(Material material, String name, short durability){
		this(material, name);
		this.item.setDurability(durability);
		this.name = name;
		
		this.pointsOnBreak = 10;
	}
	
	public Gem(Gem copyFrom){
		this.item = copyFrom.item;
		this.name = copyFrom.name;
		this.lore = copyFrom.lore;
		this.pointsOnBreak = copyFrom.pointsOnBreak;
		this.shine(false);
	}
	
	public void setLore(ArrayList lore){
		if(lore != null && !lore.isEmpty()) {
			ItemMeta meta = this.item.getItemMeta();
			meta.setLore(lore);
			this.item.setItemMeta(meta);
		}
	}
	
	public ItemStack getItem(){
		return this.item;
	}
	
	
	
	private void shine(boolean shine){
		if(shine){
			this.item.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 0);
		} else {
			if(!this.item.getItemMeta().hasEnchants()) return;
			for(Enchantment en : this.item.getEnchantments().keySet()){
				this.item.removeEnchantment(en);
			}
		}
	}
	
	public String getName(){
		return this.name;
	}
	
	public int getPointsOnBreak(){
		return this.pointsOnBreak;
	}
	
	public void setPointsOnBreak(int pointsOnBreak){
		this.pointsOnBreak = pointsOnBreak;
	}
	
	public void setItem(ItemStack item){
		this.item = item;
	}
}
