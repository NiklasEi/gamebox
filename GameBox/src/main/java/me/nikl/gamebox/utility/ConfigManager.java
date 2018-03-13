package me.nikl.gamebox.utility;

import me.nikl.gamebox.Language;
import me.nikl.gamebox.Module;
import me.nikl.gamebox.game.GameLanguage;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Niklas Eicker
 */
public class ConfigManager {
    private static final Map<String, FileConfiguration> moduleFileConfigurations = new HashMap<>();
    private static final Map<String, Language> moduleLanguages = new HashMap<>();

    public static void registerModuleConfiguration(Module module, FileConfiguration configuration) {
        moduleFileConfigurations.put(module.getModuleID(), configuration);
    }

    public static <T extends Language> void  registerModuleLanguage(Module module, T language) {
        moduleLanguages.put(module.getModuleID(), language);
    }

    public static Language getLanguage(Module module) {
        return getLanguage(module.getModuleID());
    }

    public static Language getLanguage(String moduleID) {
        return moduleLanguages.get(moduleID);
    }

    public static GameLanguage getGameLanguage(Module module) {
        return getGameLanguage(module.getModuleID());
    }

    public static GameLanguage getGameLanguage(String moduleID) {
        Language language = moduleLanguages.get(moduleID);
        if (language == null || !(language instanceof GameLanguage))
            throw new IllegalArgumentException("Requested game language for '" + moduleID + "' cannot be found");
        return (GameLanguage) language;
    }

    public static FileConfiguration getConfig(Module module) {
        return getConfig(module.getModuleID());
    }

    public static FileConfiguration getConfig(String moduleID) {
        if (!moduleFileConfigurations.containsKey(moduleID))
            throw new IllegalArgumentException("Configuration for '" + moduleID + "' cannot be found");
        return moduleFileConfigurations.get(moduleID);
    }
}
