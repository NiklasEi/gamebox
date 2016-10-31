package me.nikl.gamebox.games.tetris;

import java.util.Random;

/**
 * Created by niklas on 10/31/16.
 */
public enum Position {
	ONE(0), TWO(1), THREE(2), FOUR(3);
	
	private int rotate;
	
	Position(int rotate){
		this.rotate = rotate;
	}
	
	public static Position getRandom() {
		int random = (new Random()).nextInt(4);
		if(random == 0) return ONE;
		else if(random == 1) return TWO;
		else if(random == 2) return THREE;
		else return FOUR;
	}
}
