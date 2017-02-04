package me.nikl.gamebox.games.tetris.elements;

/**
 * Created by niklas on 11/5/16.
 */
public class ElementT extends AElement {
	
	public ElementT(){
		super(10);
	}
	
	@Override
	public void setCenter(int centerSlot) {
		this.center = centerSlot;
		switch (this.position){
			case ONE:
				setSlotsAroundCenter(-1, 0, 1, 9);
				break;
			case TWO:
				setSlotsAroundCenter(-9, -1, 0, 9);
				break;
			case THREE:
				setSlotsAroundCenter(-9, -1, 0, 1);
				break;
			case FOUR:
				setSlotsAroundCenter(-9, 0, 1, 9);
				break;
			default:
				break;
		}
	}
}
