package me.nikl.gamebox.games.cookieclicker;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.data.SaveType;
import me.nikl.gamebox.nms.NMSUtil;
import me.nikl.gamebox.util.Module;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Level;

/**
 * Created by Niklas
 *
 * Main class of the GameBox game Cookie Clicker
 */
public class CCGame extends me.nikl.gamebox.games.Game {

    public static boolean debug = false;
    public static String gameID = "cookieclicker";

    public static Economy econ = null;
    private boolean econEnabled;

    public CCLanguage lang;

    private final SaveType topListSaveType = SaveType.HIGH_NUMBER_SCORE;
    private final int playerNum = 1;

    private boolean disabled, playSounds;
    private NMSUtil nms;
    private CCGameManager gameManager;

    public CCGame(GameBox gameBox, Module module) {
        super(gameBox, module, new String[]{"cookies", "cc"});
    }

    public void reload() {
        if (!con.exists()) {
            this.saveResource("config.yml", false);
        }
        reloadConfig();

        this.lang = new CCLanguage(this);


        playSounds = config.getBoolean("rules.playSounds", true);


        this.econEnabled = false;
        if (getConfig().getBoolean("economy.enabled")) {
            this.econEnabled = true;
            if (!setupEconomy()) {
                Bukkit.getConsoleSender().sendMessage(lang.PREFIX + ChatColor.RED + " No economy found!");
                getServer().getPluginManager().disablePlugin(this);
                disabled = true;
                return;
            }
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }


    public void debug(String message) {
        if (debug) Bukkit.getLogger().log(Level.INFO, message);
    }

    public NMSUtil getNms() {
        return this.nms;
    }

    public CCGameManager getGameManager() {
        return gameManager;
    }

    public boolean getPlaySounds() {
        return playSounds;
    }

    public boolean isEconEnabled(){
        return this.econEnabled;
    }
}
