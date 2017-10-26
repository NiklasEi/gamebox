package me.nikl.gamebox.games;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.util.LanguageUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

/**
 * Created by nikl on 26.10.17.
 */
public abstract class Game {
    private GameBox gameBox;
    private FileConfiguration config;
    private String gameID;
    private String name;
    private LanguageUtil.Namespace namespace;
    private File dataFolder;

    private GameSettings gameSettings;

    public Game(GameBox gameBox, String gameID, String name){
        this.gameID = gameID;
        this.name = name;
        this.gameBox = gameBox;

        this.gameSettings = new GameSettings();
    }


    public boolean onEnable(){
        File configFile = new File(gameBox.getDataFolder()
                + File.separator + "games"
                + File.separator + name
                + File.separator + "config.yml");
        if(!configFile.exists()){
            configFile.mkdirs();
            gameBox.saveResource("games"
                    + File.separator + name
                    + File.separator + "config.yml", false);
        }

        this.dataFolder = new File(gameBox.getDataFolder()
                + File.separator + "games"
                + File.separator + name
                + File.separator);

        // reload config
        try {
            this.config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(configFile), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        this.namespace = LanguageUtil.Namespace.valueOf(name.toUpperCase());

        return true;
    }

    public FileConfiguration getConfig(){
        return config;
    }

    public LanguageUtil.Namespace getNamespace() {
        return namespace;
    }

    public String getGameID() {
        return gameID;
    }

    public String getName(){
        return name;
    }

    public GameSettings getGameSettings() {
        return gameSettings;
    }

    public GameBox getGameBox(){
        return this.gameBox;
    }

    public File getDataFolder(){
        return this.dataFolder;
    }
}
