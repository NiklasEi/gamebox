package me.nikl.gamebox.games.tetris.elements;

import me.nikl.gamebox.games.tetris.Position;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;

/**
 * Created by niklas on 10/31/16.
 *
 */
public abstract class AElement {
	protected ItemStack item;
	protected int[] slots;
	protected Position position;
	
	public AElement(){
		position = Position.getRandom();
	}
}
