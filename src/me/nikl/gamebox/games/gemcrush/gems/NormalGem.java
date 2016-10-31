package me.nikl.gamebox.games.gemcrush.gems;

import org.bukkit.Material;

/**
 * Created by niklas on 10/3/16.
 *
 * NormalGem class
 */
public class NormalGem extends Gem{
	
	double possibility = 1.;
	
	
	public NormalGem(Material material, String name){
		super(material, name);
	}
	
	public NormalGem(Material material, String name, short dur){
		super(material, name, dur);
	}
	
	public NormalGem(NormalGem copyFrom){
		this.item = copyFrom.item;
		this.name = copyFrom.name;
		this.lore = copyFrom.lore;
		
		this.possibility = copyFrom.possibility;
		
		this.pointsOnBreak = copyFrom.pointsOnBreak;
	}
	
	
	@Override
	public void onBreak() {
		
	}
	
	public void setPossibility(double possibility){
		this.possibility = possibility;
	}
	
	public double getPossibility(){
		return this.possibility;
	}
}
