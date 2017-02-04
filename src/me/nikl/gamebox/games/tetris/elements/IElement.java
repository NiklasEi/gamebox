package me.nikl.gamebox.games.tetris.elements;

import me.nikl.gamebox.games.tetris.Position;
import org.bukkit.inventory.ItemStack;

/**
 * Created by niklas on 11/5/16.
 *
 */
public interface IElement {
	
	void setCenter(int centerSlot);
	
	void setSlotsAroundCenter(int a, int b, int c, int d);
	
	Position getPosition();
	
	int[] getSlots();
	
	int getCenter();
	
	ItemStack getItem();
	
	void setPosition(Position newPosition);
}
