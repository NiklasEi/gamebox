package me.nikl.gamebox.games.tetris.elements;

import me.nikl.gamebox.Main;
import org.bukkit.Bukkit;

/**
 * Created by niklas on 11/5/16.
 *
 */
public class ElementI extends AElement{
	
	public ElementI(){
		super(3);
	}
	
	@Override
	public void setCenter(int centerSlot) {
		if(Main.debug)
			Bukkit.getConsoleSender().sendMessage("setting new center");
		this.center = centerSlot;
		switch (this.position){
			case ONE:
			case THREE:
				setSlotsAroundCenter(-2, -1, 0, 1);
				break;
			case TWO:
			case FOUR:
				setSlotsAroundCenter(-18, -9, 0, 9);
				break;
			default:
				break;
		}
	}
}
