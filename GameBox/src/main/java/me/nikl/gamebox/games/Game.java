package me.nikl.gamebox.games;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.util.FileUtil;
import me.nikl.gamebox.util.Module;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

/**
 * Created by nikl on 26.10.17.
 *
 * Abstract Game class to be extended by every GB game
 */
public abstract class Game {

    protected GameBox gameBox;

    protected FileConfiguration config;

    protected Module module;

    protected File dataFolder;

    protected IGameManager gameManager;

    protected GameSettings gameSettings;

    public Game(GameBox gameBox, Module module){
        this.module = module;

        this.gameBox = gameBox;

        this.gameSettings = new GameSettings();
    }


    public boolean onEnable(){
        File configFile = new File(gameBox.getDataFolder()
                + File.separator + "games"
                + File.separator + getGameID()
                + File.separator + "config.yml");
        if(!configFile.exists()){
            configFile.mkdirs();
            gameBox.saveResource("games"
                    + File.separator + getGameID()
                    + File.separator + "config.yml", false);
        }

        this.dataFolder = new File(gameBox.getDataFolder()
                + File.separator + "games"
                + File.separator + getGameID()
                + File.separator);

        // reload config
        try {
            this.config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(configFile), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public FileConfiguration getConfig(){
        return config;
    }

    public Module getModule() {
        return module;
    }

    public String getGameID() {
        return module.moduleID();
    }

    public GameSettings getSettings() {
        return gameSettings;
    }

    public GameBox getGameBox(){
        return this.gameBox;
    }

    public File getDataFolder(){
        return this.dataFolder;
    }

    public IGameManager getGameManager() {
        return gameManager;
    }
}
