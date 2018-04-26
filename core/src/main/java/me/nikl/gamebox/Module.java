package me.nikl.gamebox;

import org.apache.commons.lang.Validate;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

/**
 * @author Niklas Eicker
 */
public class Module {
    private String moduleID, classPath;
    private boolean isGame = false;
    private JavaPlugin externalPlugin;
    private List<String> subCommands;

    public Module(GameBox gameBox, String moduleID, String classPath, JavaPlugin plugin, String... subCommands) {
        Validate.isTrue(moduleID != null && !moduleID.isEmpty()
                , " moduleID cannot be null or empty!");
        if (classPath != null && !classPath.isEmpty()) {
            this.isGame = true;
        }
        if (subCommands != null && !(subCommands.length < 1)) {
            this.subCommands = Arrays.asList(subCommands);
        }
        this.classPath = classPath;
        this.moduleID = moduleID.toLowerCase();
        this.externalPlugin = plugin;
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
    public boolean equals(Object module) {
        if (!(module instanceof Module)) {
            return false;
        }
        return moduleID.equalsIgnoreCase(((Module) module).moduleID);
    }

    public JavaPlugin getExternalPlugin() {
        return externalPlugin;
    }

    public List<String> getSubCommands() {
        return subCommands;
    }

    void setSubCommands(List<String> subCommands) {
        this.subCommands = subCommands;
    }
}
