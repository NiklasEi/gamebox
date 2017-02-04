package me.nikl.gamebox.games.tetris.elements;

/**
 * Created by niklas on 11/5/16.
 */
public class ElementZ extends AElement {
	
	public ElementZ(){
		super(14);
	}
	
	@Override
	public void setCenter(int centerSlot) {
		this.center = centerSlot;
		switch (this.position){
			case ONE: case THREE:
				setSlotsAroundCenter(-10, -9, 0, 1);
				break;
			case TWO: case FOUR:
				setSlotsAroundCenter(-8, 0, 1, 9);
				break;
			default:
				break;
		}
	}
}
