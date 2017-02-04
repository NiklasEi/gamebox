package me.nikl.gamebox.games.tetris.elements;

/**
 * Created by niklas on 11/5/16.
 */
public class ElementO extends AElement{
	
	public ElementO(){
		super(4);
	}
	
	@Override
	public void setCenter(int centerSlot) {
		this.center = centerSlot;
		setSlotsAroundCenter(-10, -9, -1, 0);
		return;
		/*switch (this.position){
			case ONE:
				setSlotsAroundCenter(-10, -9, -1, 0);
				break;
			case TWO:
				setSlotsAroundCenter(-9, -8, 0, 1);
				break;
			case THREE:
				setSlotsAroundCenter(0, 1, 9, 10);
				break;
			case FOUR:
				setSlotsAroundCenter(-1, 0, 8, 9);
				break;
			default:
				break;
		}*/
	}
}
