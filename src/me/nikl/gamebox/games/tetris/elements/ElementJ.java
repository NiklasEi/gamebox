package me.nikl.gamebox.games.tetris.elements;

/**
 * Created by niklas on 11/5/16.
 */
public class ElementJ extends AElement{
	
	public ElementJ(){
		super(11);
	}
	
	@Override
	public void setCenter(int centerSlot) {
		this.center = centerSlot;
		switch (this.position){
			case ONE:
				setSlotsAroundCenter(-1, 0, 1, 10);
				break;
			case TWO:
				setSlotsAroundCenter(-9, 0, 8, 9);
				break;
			case THREE:
				setSlotsAroundCenter(-10, -1, 0, 1);
				break;
			case FOUR:
				setSlotsAroundCenter(-9, 8, 0, 9);
				break;
			default:
				break;
		}
	}
}
