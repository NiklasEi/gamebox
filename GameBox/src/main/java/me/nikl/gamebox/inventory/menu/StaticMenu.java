package me.nikl.gamebox.inventory.menu;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.inventory.button.AButton;

/**
 * Created by nikl on 21.02.18.
 */
public abstract class StaticMenu extends GameBoxMenu {
    protected AButton[] upperGrid;
    protected AButton[] lowerGrid = new Button[36];

    public StaticMenu(GameBox gameBox) {
        super(gameBox);
    }

    public void setButton(AButton button, int slot) {
        upperGrid[slot] = button;
    }

    public void setButton(AButton button) {
        int i = 0;
        while (upperGrid[i] != null) {
            i++;
        }
        setButton(button, i);
    }

    public void setLowerButton(AButton button, int slot) {
        lowerGrid[slot] = button;
    }

    public void setLowerButton(AButton button) {
        int i = 0;
        while (lowerGrid[i] != null) {
            i++;
        }
        setLowerButton(button, i);
    }
}
