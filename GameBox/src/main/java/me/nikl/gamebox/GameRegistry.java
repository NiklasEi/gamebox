package me.nikl.gamebox;

import me.nikl.gamebox.games.Game;
import me.nikl.gamebox.utility.FileUtility;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by nikl on 21.11.17.
 *
 */
public class GameRegistry {

    private GameBox gameBox;

    private Map<String, Module> modules = new HashMap<>();

    private Map<String, Module> subCommands = new HashMap<>();
    private Map<Module, Set<String>> bundledSubCommands  = new HashMap<>();

    private final Set<String> forbiddenIDs =
            new HashSet<>(Arrays.asList("all, game, games"));

    private final Set<String> forbiddenSubCommands =
            new HashSet<>(Arrays.asList("info"));

    public GameRegistry(GameBox plugin){
        this.gameBox = plugin;
    }


    public boolean registerModule(Module module){
        if(isRegistered(module.getModuleID())) {
            gameBox.getLogger().log(Level.WARNING, " A Module tried registering with an already in use ID!");
            return false;
        }

        if(forbiddenIDs.contains(module.getModuleID())){
            gameBox.getLogger().log(Level.WARNING, " A Module tried registering with a forbidden ID (" + module.getModuleID() + ")");
            return false;
        }

        modules.put(module.getModuleID(), module);

        if(module.getExternalPlugin() != null){
            if(!FileUtility.copyExternalResources(gameBox, module)){
                gameBox.info(" Failed to completely load the external module '" + module.getModuleID() + "'");
                modules.remove(module.getModuleID());
                return false;
            }
        }

        if(module.isGame()) {
            loadGame(module);
            registerSubCommands(module);
        }
        return true;
    }

    public boolean isRegistered(Module module){
        return isRegistered(module.getModuleID());
    }

    public boolean isRegistered(String moduleID){
        return modules.containsKey(moduleID.toLowerCase());
    }

    public Module getModule(String moduleID){
        return modules.get(moduleID);
    }

    /**
     * Go through all modules and try getting game instances through their classes
     */
    public void loadGames() {
        for(Module module : modules.values()){
            if(module.isGame()) {
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
        if(clazz == null) return;

        try {
            Constructor<Game> ctor = ((Class<Game>) clazz).getConstructor(GameBox.class);
            Game game = ctor.newInstance(gameBox);
            gameBox.getPluginManager().addGame(game);
            game.onEnable();
        } catch (NoSuchMethodException | IllegalAccessException
                | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            gameBox.info(" The game class needs a public constructor taking only a GameBox obj!");
        }
    }

    public Set<String> getModuleIDs() {
        return Collections.unmodifiableSet(modules.keySet());
    }

    public Set<String> getModuleSubCommands(Module module) {
        return Collections.unmodifiableSet(bundledSubCommands.get(module));
    }

    private void registerSubCommands(Module module){
        if(module.getSubCommands() == null || module.getSubCommands().isEmpty()){
            return;
        }

        List<String> subCommands = module.getSubCommands();

        for(int i = 0; i < subCommands.size(); i++){
            subCommands.set(i, subCommands.get(i).toLowerCase());
        }

        // ensure that sub commands are unique and valid
        for(int i = 0; i < subCommands.size(); i++) {
            if(forbiddenSubCommands.contains(subCommands.get(i)))
                continue;
            if(this.subCommands.keySet().contains(subCommands.get(i)))
                continue;

            this.subCommands.put(subCommands.get(i), module);
            addSubCommandToBundle(module, subCommands.get(i));
        }
    }

    private void addSubCommandToBundle(Module module, String subCommand){
        bundledSubCommands.putIfAbsent(module, new HashSet<>());
        bundledSubCommands.get(module).add(subCommand);
    }

    public Module getModuleBySubCommand(String subCommand) {
        return subCommands.get(subCommand);
    }
}
