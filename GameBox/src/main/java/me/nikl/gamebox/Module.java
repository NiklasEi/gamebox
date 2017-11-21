package me.nikl.gamebox;

import org.apache.commons.lang.Validate;

/**
 * Created by nikl on 21.11.17.
 *
 */
public class Module {

    private String moduleID, classPath;

    private boolean isGame = false;

    public Module(GameBox gameBox, String moduleID, String classPath){
        Validate.isTrue(moduleID != null && !moduleID.isEmpty()
                , " moduleID cannot be null or empty!");
        if(classPath != null && !classPath.isEmpty()) {
            this.isGame = true;
        }
        this.classPath = classPath;
        this.moduleID = moduleID.toLowerCase();

        gameBox.getGameRegistry().registerModule(this);
    }

    public Module(GameBox gameBox, String moduleID){
        this.isGame = false;
        this.moduleID = moduleID.toLowerCase();

        gameBox.getGameRegistry().registerModule(this);
    }

    public String getModuleID() {
        return moduleID;
    }

    public String getClassPath() {
        return classPath;
    }

    public boolean isGame() {
        return isGame;
    }

    @Override
    public boolean equals(Object module){
        if(!(module instanceof Module)){
            return false;
        }

        return moduleID.equalsIgnoreCase(((Module) module).moduleID);
    }
}
