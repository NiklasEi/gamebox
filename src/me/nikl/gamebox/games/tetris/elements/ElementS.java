package me.nikl.gamebox.games.tetris.elements;

/**
 * Created by niklas on 11/5/16.
 */
public class ElementS extends AElement{
	
	public ElementS(){
		super(5);
	}
	
	@Override
	public void setCenter(int centerSlot) {
		this.center = centerSlot;
		switch (this.position){
			case ONE:
			case THREE:
				setSlotsAroundCenter(-9, -8, -1, 0);
				break;
			case TWO:
			case FOUR:
				setSlotsAroundCenter(-9, 0, 1, 10);
				break;
			default:
				break;
		}
	}
}
