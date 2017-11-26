package me.nikl.gamebox;

import me.nikl.gamebox.games.Game;
import me.nikl.gamebox.util.FileUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Created by nikl on 21.11.17.
 *
 */
public class GameRegistry {

    private GameBox gameBox;

    private HashMap<String, Module> modules = new HashMap<>();

    public GameRegistry(GameBox plugin){
        this.gameBox = plugin;
    }

    public boolean registerModule(Module module){
        if(isRegistered(module.getModuleID())) {
            gameBox.getLogger().log(Level.WARNING, " A Module tried registering with an already in use ID!");
            return false;
        }

        modules.put(module.getModuleID(), module);

        if(module.getExternalPlugin() != null){
            if(!FileUtil.copyExternalResources(gameBox, module)){
                gameBox.info(" Failed to completely load the external module '" + module.getModuleID() + "'");
                return false;
            }
        }

        if(module.isGame())
            loadGame(module);
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
            loadGame(module);
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

}
