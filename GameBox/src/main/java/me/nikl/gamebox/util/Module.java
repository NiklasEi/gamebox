package me.nikl.gamebox.util;

/**
 * Created by nikl on 27.10.17.
 */
public enum Module {
    GAMEBOX("gamebox"),
    COOKIECLICKER("cookieclicker");

    private String moduleID;

    Module(String moduleID){
        this.moduleID = moduleID;
    }

    public String moduleID(){
        return this.moduleID;
    }
}
