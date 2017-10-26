package me.nikl.gamebox.games;

import org.bukkit.plugin.Plugin;

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
    private Plugin gamePlugin;


    public GameContainer(Plugin plugin, String gameID, IGameManager gameManager){
        this.gameManager = gameManager;
        this.gameID = gameID;
        this.gamePlugin = plugin;
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

    public Plugin getGamePlugin() {
        return gamePlugin;
    }

    public void setGamePlugin(Plugin gamePlugin) {
        this.gamePlugin = gamePlugin;
    }
}
