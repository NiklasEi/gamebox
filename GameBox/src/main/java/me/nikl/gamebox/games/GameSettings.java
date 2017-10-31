package me.nikl.gamebox.games;

/**
 * Created by nikl on 26.10.17.
 */
public class GameSettings {
    private boolean handleClicksOnHotbar;
    private GameType gameType;
    private int gameGuiSize;

    public GameSettings(){
        // set default values
        this.handleClicksOnHotbar = false;
        this.gameType = GameType.SINGLE_PLAYER;
        this.gameGuiSize = 54;
    }

    public boolean isHandleClicksOnHotbar() {
        return handleClicksOnHotbar;
    }

    public void setHandleClicksOnHotbar(boolean handleClicksOnHotbar) {
        this.handleClicksOnHotbar = handleClicksOnHotbar;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public int getGameGuiSize() {
        return gameGuiSize;
    }

    public void setGameGuiSize(int gameGuiSize) {
        this.gameGuiSize = gameGuiSize;
    }

    public enum GameType{
        SINGLE_PLAYER, TWO_PLAYER
    }
}
