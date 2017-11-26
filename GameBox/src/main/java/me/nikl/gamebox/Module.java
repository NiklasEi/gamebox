package me.nikl.gamebox;

import org.apache.commons.lang.Validate;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by nikl on 21.11.17.
 *
 */
public class Module {

    private String moduleID, classPath;

    private boolean isGame = false;

    private JavaPlugin externalPlugin;

    public Module(GameBox gameBox, String moduleID, String classPath, JavaPlugin plugin){
        Validate.isTrue(moduleID != null && !moduleID.isEmpty()
                , " moduleID cannot be null or empty!");
        if(classPath != null && !classPath.isEmpty()) {
            this.isGame = true;
        }
        this.classPath = classPath;
        this.moduleID = moduleID.toLowerCase();
        this.externalPlugin = plugin;

        gameBox.getGameRegistry().registerModule(this);
    }

    /**
     * For internal game module
     *
     * For external use ExternalModule
     * @param moduleID
     * @param classPath
     */
    public Module(GameBox gameBox, String moduleID, String classPath){
        this(gameBox, moduleID, classPath, null);
    }

    /**
     * For non-game module
     * @param moduleID
     */
    public Module(GameBox gameBox, String moduleID){
        this(gameBox, moduleID, null, null);
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

    public JavaPlugin getExternalPlugin() {
        return externalPlugin;
    }
}
