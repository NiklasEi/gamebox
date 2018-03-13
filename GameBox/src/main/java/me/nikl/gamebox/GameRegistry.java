package me.nikl.gamebox;

import me.nikl.gamebox.game.Game;
import me.nikl.gamebox.game.exceptions.GameLoadException;
import me.nikl.gamebox.utility.ConfigManager;
import me.nikl.gamebox.utility.FileUtility;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author Niklas Eicker
 */
public class GameRegistry {
    private final Set<String> forbiddenIDs =
            new HashSet<>(Arrays.asList("all, game, games, info, token, t"));
    private final Set<String> forbiddenSubCommands =
            new HashSet<>(Arrays.asList("all, game, games, info, token, t"));
    private final Set<String> disabledModules = new HashSet<>();
    private GameBox gameBox;
    private Map<String, Module> modules = new HashMap<>();
    private Map<String, Module> subCommands = new HashMap<>();
    private Map<Module, Set<String>> bundledSubCommands = new HashMap<>();
    private boolean enableNewGamesByDefault;
    private FileConfiguration gamesConfiguration;
    private File gamesFile;

    public GameRegistry(GameBox plugin) {
        this.gameBox = plugin;
    }

    private void loadDisabledModules() {
        disabledModules.clear();
        enableNewGamesByDefault = gamesConfiguration.getBoolean("enableNewGamesByDefault", true);
        ConfigurationSection gamesSection = gamesConfiguration.getConfigurationSection("games");
        if (gamesSection == null ) return;
        for (String moduleID : gamesSection.getKeys(false)) {
            if (!gamesSection.getBoolean(moduleID + ".enabled", true)) {
                GameBox.debug("Set " + moduleID + " as disabled");
                disabledModules.add(moduleID);
            }
        }
    }

    public void reloadGamesConfiguration() {
        gamesFile = new File(gameBox.getDataFolder().toString() + File.separatorChar + "games.yml");
        if (!gamesFile.exists()) {
            gameBox.saveResource("games.yml", false);
        }
        try {
            gamesConfiguration = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(gamesFile), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean registerModule(Module module) {
        if (isRegistered(module.getModuleID())) {
            gameBox.getLogger().log(Level.WARNING, "A Module tried registering with an already in use ID!");
            return false;
        }
        if (forbiddenIDs.contains(module.getModuleID())) {
            gameBox.getLogger().log(Level.WARNING, "A Module tried registering with a forbidden ID (" + module.getModuleID() + ")");
            return false;
        }
        if (disabledModules.contains(module.getModuleID())) {
            gameBox.warning("The game " + module.getModuleID() + " is disabled in 'games.yml'");
            return false;
        }
        if (!module.getModuleID().equals(GameBox.MODULE_GAMEBOX))
            registerModuleInSettingsFile(module.getModuleID());
        modules.put(module.getModuleID(), module);
        if (module.getExternalPlugin() != null) {
            if (!FileUtility.copyExternalResources(gameBox, module)) {
                gameBox.info(" Failed to register the external module '" + module.getModuleID() + "'");
                modules.remove(module.getModuleID());
                return false;
            }
        }
        if (module.isGame()) {
            loadGame(module);
            registerSubCommands(module);
        }
        return true;
    }

    private void registerModuleInSettingsFile(String moduleID) {
        if (!gamesConfiguration.isSet("games." + moduleID)) {
            gamesConfiguration.set("games." + moduleID + ".enabled", enableNewGamesByDefault);
            saveGameSettings();
        }
    }

    public boolean isRegistered(Module module) {
        return isRegistered(module.getModuleID());
    }

    public boolean isRegistered(String moduleID) {
        return modules.containsKey(moduleID.toLowerCase());
    }

    public Module getModule(String moduleID) {
        return modules.get(moduleID);
    }

    /**
     * Reload the settings. Then go through all modules and
     * try getting game instances through their class paths
     */
    public void reload() {
        reloadGamesConfiguration();
        loadDisabledModules();
        for (Module module : modules.values()) {
            if (disabledModules.contains(module.getModuleID())) {
                gameBox.warning("The game " + module.getModuleID() + " is disabled in 'games.yml'");
                continue;
            }
            if (module.isGame()) {
                loadGame(module);
                registerSubCommands(module);
            }
        }
    }

    private void loadGame(Module module) {
        Class clazz = null;
        try {
            clazz = Class.forName(module.getClassPath());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (clazz == null) return;
        try {
            Constructor<Game> ctor = ((Class<Game>) clazz).getConstructor(GameBox.class);
            Game game = ctor.newInstance(gameBox);
            gameBox.getPluginManager().registerGame(game);
            game.onEnable();
        } catch (NoSuchMethodException | IllegalAccessException
                | InstantiationException | InvocationTargetException e) {
            gameBox.warning(" The game class needs a public constructor taking only a GameBox object!");
            e.printStackTrace();
            gameBox.getPluginManager().unregisterGame(module.getModuleID());
        } catch (GameLoadException e) {
            e.printStackTrace();
            gameBox.getPluginManager().unregisterGame(module.getModuleID());
        }
    }

    public Set<String> getModuleIDs() {
        return Collections.unmodifiableSet(modules.keySet());
    }

    public Set<String> getModuleSubCommands(Module module) {
        return Collections.unmodifiableSet(bundledSubCommands.get(module));
    }

    private void registerSubCommands(Module module) {
        if (module.getSubCommands() == null || module.getSubCommands().isEmpty()) {
            bundledSubCommands.putIfAbsent(module, new HashSet<>());
            return;
        }
        List<String> subCommands = module.getSubCommands();
        for (int i = 0; i < subCommands.size(); i++) {
            subCommands.set(i, subCommands.get(i).toLowerCase());
        }
        // ensure that sub commands are unique and valid
        for (int i = 0; i < subCommands.size(); i++) {
            if (forbiddenSubCommands.contains(subCommands.get(i)))
                throw new IllegalArgumentException("Forbidden sub command: " + subCommands.get(i));
            if (this.subCommands.keySet().contains(subCommands.get(i)))
                continue;
            this.subCommands.put(subCommands.get(i), module);
            addSubCommandToBundle(module, subCommands.get(i));
        }
    }

    private void addSubCommandToBundle(Module module, String subCommand) {
        bundledSubCommands.putIfAbsent(module, new HashSet<>());
        bundledSubCommands.get(module).add(subCommand);
    }

    public Module getModuleBySubCommand(String subCommand) {
        GameBox.debug("grab module of " + subCommand);
        return subCommands.get(subCommand);
    }

    public void unregisterGame(String gameID) {
        Module module = modules.get(gameID);
        if (module == null) return;
        Set<String> subCommands = bundledSubCommands.get(module);
        modules.remove(gameID);
        if (subCommands == null || subCommands.isEmpty()) return;
        for (String subCommand : subCommands) {
            GameBox.debug("   remove " + subCommand);
            this.subCommands.remove(subCommand);
        }
        bundledSubCommands.remove(gameID);
    }

    public void disableGame(String gameID) {
        disabledModules.add(gameID);
        gamesConfiguration.set("games." + gameID + ".enabled", false);
        saveGameSettings();
    }

    public void enableGame(String gameID) {
        disabledModules.remove(gameID);
        gamesConfiguration.set("games." + gameID + ".enabled", true);
        saveGameSettings();
    }

    private void saveGameSettings() {
        try {
            gamesConfiguration.save(gamesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isDisabledModule(String moduleID) {
        return disabledModules.contains(moduleID);
    }
}
