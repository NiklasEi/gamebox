package me.nikl.gamebox.games.tetris;

import java.util.Random;

/**
 * Created by niklas on 10/31/16.
 *
 * possible translational positions of the elements
 */
public enum Position {
	ONE, TWO, THREE, FOUR;
	
	public static Position getRandom() {
		int random = (new Random()).nextInt(4);
		if(random == 0) return ONE;
		else if(random == 1) return TWO;
		else if(random == 2) return THREE;
		else return FOUR;
	}
	
	public static Position getNextPosition(Position position){
		switch (position){
			case ONE:
				return TWO;
			case TWO:
				return THREE;
			case THREE:
				return FOUR;
			case FOUR:
				return ONE;
			default:
				return null;
		}
	}
	
	public static Position getLastPosition(Position position){
		switch (position){
			case ONE:
				return FOUR;
			case TWO:
				return ONE;
			case THREE:
				return TWO;
			case FOUR:
				return THREE;
			default:
				return null;
		}
	}
}
