package me.nikl.gamebox.games;

import me.nikl.gamebox.GameBoxSettings;

/**
 * Created by nikl on 26.10.17.
 */
public class GameSettings {
    private boolean handleClicksOnHotbar;
    private boolean playSounds;
    private boolean econEnabled;
    private GameType gameType;
    private int gameGuiSize;

    public GameSettings(){
        // set default values
        this.handleClicksOnHotbar = false;
        this.gameType = GameType.SINGLE_PLAYER;
        this.gameGuiSize = 54;
        this.playSounds = GameBoxSettings.playSounds;

        setEconEnabled(GameBoxSettings.econEnabled);
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

    public boolean isPlaySounds() {
        return playSounds;
    }

    public void setPlaySounds(boolean playSounds) {
        this.playSounds = playSounds;
    }

    public boolean isEconEnabled() {
        return econEnabled;
    }

    public void setEconEnabled(boolean econEnabled) {
        this.econEnabled = econEnabled;
    }

    public enum GameType{
        SINGLE_PLAYER(1), TWO_PLAYER(2);

        int playerNumber;

        GameType(int playerNumber){
            this.playerNumber = playerNumber;
        }

        public int getPlayerNumber(){
            return this.playerNumber;
        }
    }
}
