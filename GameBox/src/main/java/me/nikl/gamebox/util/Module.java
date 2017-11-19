package me.nikl.gamebox.util;


/**
 * Created by nikl on 27.10.17.
 */
public enum Module {
    GAMEBOX("gamebox"),
    COOKIECLICKER("cookieclicker", "me.nikl.gamebox.games.cookieclicker.CookieClicker"),
    CONNECTFOUR("connectfour", "me.nikl.gamebox.games.connectfour.ConnectFour");

    private String moduleID, gameClass;

    Module(String moduleID){
        this(moduleID, null);
    }

    Module(String moduleID, String gameClass){
        this.moduleID = moduleID;
        this.gameClass = gameClass;
    }

    public String moduleID(){
        return this.moduleID;
    }

    public Class getGameClass(){
        if (gameClass == null) return null;
        try {
            Class clazz = Class.forName(gameClass);
            return clazz;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
