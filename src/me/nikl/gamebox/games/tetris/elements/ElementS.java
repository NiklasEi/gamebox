package me.nikl.gamebox.games.tetris.elements;

/**
 * Created by Niklas on 13.02.2017.
 */
public class ElementS extends AElement {

    public ElementS(){
        super(1);
    }

    @Override
    public void setCenter(int centerSlot) {
        this.center = centerSlot;
        switch (this.position){
            case ONE:
                setSlotsAroundCenter(-1, 0, 1, 8);
                break;
            case TWO:
                setSlotsAroundCenter(-10, -9, 0, 9);
                break;
            case THREE:
                setSlotsAroundCenter(-8, -1, 0, 1);
                break;
            case FOUR:
                setSlotsAroundCenter(-9, 0, 9, 10);
                break;
            default:
                break;
        }
    }
}
