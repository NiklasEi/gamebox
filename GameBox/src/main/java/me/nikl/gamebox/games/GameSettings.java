package me.nikl.gamebox.games;

/**
 * Created by nikl on 26.10.17.
 */
public class GameSettings {
    private boolean handleClicksOnHotbar;

    public GameSettings(){
        // set default values
        handleClicksOnHotbar = false;
    }

    public boolean isHandleClicksOnHotbar() {
        return handleClicksOnHotbar;
    }

    public void setHandleClicksOnHotbar(boolean handleClicksOnHotbar) {
        this.handleClicksOnHotbar = handleClicksOnHotbar;
    }
}
