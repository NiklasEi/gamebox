package me.nikl.gamebox.guis.gui;

import me.nikl.gamebox.Main;
import me.nikl.gamebox.guis.GUIManager;

/**
 * Created by Niklas on 13.02.2017.
 */
public class GameGui extends AGui{
    String gameID, key;
    /**
     * Constructor for a gamegui
     *
     * will register automatically to the guimanager
     *
     * @param plugin     plugin instance
     * @param guiManager GUIManager instance
     * @param slots      number of slots in the inventory
     */
    public GameGui(Main plugin, GUIManager guiManager, int slots, String gameID, String key) {
        super(plugin, guiManager, slots);
        this.gameID = gameID;
        if(!key.equalsIgnoreCase(MAIN)) Main.debug("GameGui has not the key 'main'");
        this.key = key;

        //guiManager.registerGameGUI(gameID, key, this);
    }

    public String getGameID(){
        return this.gameID;
    }

    public String getKey(){
        return this.key;
    }
}
