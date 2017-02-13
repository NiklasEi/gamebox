package me.nikl.gamebox.games.tetris.elements;

import java.util.Random;

/**
 * Created by niklas on 11/5/16.
 *
 */
public enum AllElements {
	I, J, L, O, S, T, Z;
	
	public static AElement getNewRandom(){
		int rand = (new Random()).nextInt(AllElements.values().length);
		if(rand == 0) return new ElementI();
		if(rand == 1) return new ElementJ();
		if(rand == 2) return new ElementL();
		if(rand == 3) return new ElementO();
		if(rand == 4) return new ElementS();
		if(rand == 5) return new ElementT();
		if(rand == 6) return new ElementZ();
		else return null;
	}
}
