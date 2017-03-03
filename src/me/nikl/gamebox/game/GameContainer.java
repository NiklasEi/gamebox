package me.nikl.gamebox.game;

/**
 * Created by Niklas on 14.02.2017.
 *
 *
 */
public class GameContainer {
    private IGameManager gameManager;
    private boolean handleClicksOnHotbar;
    private String gameID;
    private String plainName, name;
    private int playerNum;


    public GameContainer(String gameID, IGameManager gameManager){
        this.gameManager = gameManager;
        this.gameID = gameID;
    }

    public String getPlainName() {
        return plainName;
    }

    public void setPlainName(String plainName) {
        this.plainName = plainName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGameID() {
        return gameID;
    }

    public boolean handleClicksOnHotbar() {
        return handleClicksOnHotbar;
    }

    public void setHandleClicksOnHotbar(boolean handleClicksOnHotbar) {
        this.handleClicksOnHotbar = handleClicksOnHotbar;
    }

    public IGameManager getGameManager() {
        return gameManager;
    }

    public int getPlayerNum() {
        return playerNum;
    }

    public void setPlayerNum(int playerNum) {
        this.playerNum = playerNum;
    }
}
