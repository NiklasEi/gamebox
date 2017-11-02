package me.nikl.gamebox.games;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxLanguage;
import me.nikl.gamebox.data.SaveType;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import me.nikl.gamebox.guis.gui.game.GameGui;
import me.nikl.gamebox.guis.gui.game.TopListPage;
import me.nikl.gamebox.nms.NMSUtil;
import me.nikl.gamebox.util.ClickAction;
import me.nikl.gamebox.util.ItemStackUtil;
import me.nikl.gamebox.util.Module;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Created by nikl on 26.10.17.
 *
 * Abstract Game class to be extended by every GB game
 */
public abstract class Game {

    protected static boolean debug = false;

    protected GameBox gameBox;

    protected FileConfiguration config;

    protected Module module;

    protected File dataFolder;

    protected GameManager gameManager;

    protected GameSettings gameSettings;

    protected GameLanguage gameLang;

    protected GameBoxLanguage gbLang;

    protected NMSUtil nms;

    protected String[] subCommands;

    public Game(GameBox gameBox, Module module, String[] subCommands){
        this.module = module;
        this.subCommands = subCommands;

        this.gameBox = gameBox;
        this.gbLang = gameBox.lang;
        this.nms = gameBox.getNMS();

        this.gameSettings = new GameSettings();
    }

    public abstract void onDisable();

    public void onEnable(){
        loadConfig();
        loadSettings();

        loadGameManager();

        hook();
    }

    /**
     * This method will be called on enable,
     * after the configuration file is loaded.
     *
     * Load all game settings
     */
    public abstract void loadSettings();

    /**
     * Get a new GameManager
     *
     * Gets called on enable of the the game.
     * Initialize the GameManager and save it.
     */
    public abstract void loadGameManager();

    public boolean loadConfig(){
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

    private void hook() {
        GUIManager guiManager = gameBox.getPluginManager().getGuiManager();

        gameBox.getPluginManager().registerGame(this);

        int gameGuiSlots = gameSettings.getGameGuiSize();
        GameGui gameGui = new GameGui(gameBox, this, gameGuiSlots);
        gameGui.setHelpButton(gameLang.GAME_HELP);


        if (config.isConfigurationSection("gameBox.gameButtons")) {
            ConfigurationSection gameButtons = config.getConfigurationSection("gameBox.gameButtons");
            ConfigurationSection buttonSec;

            String displayName;
            ArrayList<String> lore;

            for (String buttonID : gameButtons.getKeys(false)) {
                buttonSec = gameButtons.getConfigurationSection(buttonID);


                if (!buttonSec.isString("materialData")) {
                    Bukkit.getLogger().log(Level.WARNING, " missing material data under: gameBox.gameButtons." + buttonID + "        can not load the button");
                    continue;
                }

                ItemStack mat = ItemStackUtil.getItemStack(buttonSec.getString("materialData"));
                if (mat == null) {
                    Bukkit.getLogger().log(Level.WARNING, " error loading: gameBox.gameButtons." + buttonID);
                    Bukkit.getLogger().log(Level.WARNING, "     invalid material data");
                    continue;
                }


                AButton button = new AButton(mat.getData(), 1);
                ItemMeta meta = button.getItemMeta();

                if (buttonSec.isString("displayName")) {
                    displayName = GameBox.chatColor(buttonSec.getString("displayName"));
                    meta.setDisplayName(displayName);
                }

                if (buttonSec.isList("lore")) {
                    lore = GameBox.chatColor(buttonSec.getStringList("lore"));
                    meta.setLore(lore);
                }

                // Todo: two player game case
                button.setAction(ClickAction.START_GAME);

                button.setItemMeta(meta);
                button.setArgs(module.moduleID(), buttonID);

                // from here it is game specific info
                gameManager.loadGameRules(buttonSec, buttonID);

                if (buttonSec.isInt("slot")) {
                    int slot = buttonSec.getInt("slot");
                    if (slot < 0 || slot >= gameGuiSlots) {
                        Bukkit.getLogger().log(Level.WARNING, "the slot of gameBox.gameButtons." + buttonID
                                + " is out of the inventory range (0 - " + gameGuiSlots + ")");
                        gameGui.setButton(button);
                    } else {
                        gameGui.setButton(button, slot);
                    }
                } else {
                    gameGui.setButton(button);
                }
            }
        }

        getMainButton:
        if (config.isConfigurationSection("gameBox.mainButton")) {
            ConfigurationSection mainButtonSec = config.getConfigurationSection("gameBox.mainButton");
            if (!mainButtonSec.isString("materialData")){
                break getMainButton;
            }

            ItemStack gameButton = ItemStackUtil.getItemStack(mainButtonSec.getString("materialData"));
            if (gameButton == null) {
                gameButton = (new ItemStack(Material.STAINED_CLAY));
            }

            ItemMeta meta = gameButton.getItemMeta();

            meta.setDisplayName(GameBox.chatColor(mainButtonSec.getString("displayName", gameLang.PLAIN_NAME)));

            if (mainButtonSec.isList("lore")) {
                meta.setLore(GameBox.chatColor(mainButtonSec.getStringList("lore")));
            }

            gameButton.setItemMeta(meta);

            guiManager.registerMainGameGUI(gameGui, gameButton, this.subCommands);
        } else {
            gameBox.getLogger().log(Level.WARNING, " Missing or wrong configured main button for " + gameLang.PLAIN_NAME + "!");
        }

        HashMap<String, GameRule> gameRules = gameManager.getGameRules();

        // get top list buttons
        if (config.isConfigurationSection("gameBox.topListButtons")) {
            ConfigurationSection topListButtons = config.getConfigurationSection("gameBox.topListButtons");
            ConfigurationSection buttonSec;

            ArrayList<String> lore;


            for (String buttonID : topListButtons.getKeys(false)) {
                buttonSec = topListButtons.getConfigurationSection(buttonID);

                if (!gameRules.keySet().contains(buttonID)) {
                    gameBox.getLogger().log(Level.WARNING, " the top list button 'gameBox.topListButtons." + buttonID + "' does not have a corresponding game button");
                    continue;
                }


                if (!gameRules.get(buttonID).isSaveStats()) {
                    gameBox.getLogger().log(Level.WARNING, " the top list buttons 'gameBox.topListButtons." + buttonID + "' corresponding game button has statistics turned off!");
                    gameBox.getLogger().log(Level.WARNING, " With these settings there is no toplist to display");
                    continue;
                }

                if (!buttonSec.isString("materialData")) {
                    gameBox.getLogger().log(Level.WARNING, " missing material data under: gameBox.topListButtons." + buttonID + "        can not load the button");
                    continue;
                }

                ItemStack mat = ItemStackUtil.getItemStack(buttonSec.getString("materialData"));
                if (mat == null) {
                    gameBox.getLogger().log(Level.WARNING, " error loading: gameBox.topListButtons." + buttonID);
                    gameBox.getLogger().log(Level.WARNING, "     invalid material data");
                    continue;
                }


                AButton button = new AButton(mat.getData(), 1);
                ItemMeta meta = button.getItemMeta();

                if (buttonSec.isString("displayName")) {
                    meta.setDisplayName(GameBox.chatColor(buttonSec.getString("displayName")));
                }


                if (buttonSec.isList("lore")) {
                    lore = GameBox.chatColor(buttonSec.getStringList("lore"));
                    meta.setLore(lore);
                }

                button.setItemMeta(meta);
                button.setAction(ClickAction.SHOW_TOP_LIST);
                button.setArgs(module.moduleID(), buttonID + GUIManager.TOP_LIST_KEY_ADDON);


                setTheButton:
                if (buttonSec.isInt("slot")) {
                    int slot = buttonSec.getInt("slot");
                    if (slot < 0 || slot >= gameGuiSlots) {
                        Bukkit.getLogger().log(Level.WARNING, "the slot of gameBox.topListButtons." + buttonID + " is out of the inventory range (0 - 53)");
                        gameGui.setButton(button);
                        break setTheButton;
                    }
                    gameGui.setButton(button, slot);
                } else {
                    gameGui.setButton(button);
                }

                // get skull lore and pass on to the top list page
                if (buttonSec.isList("skullLore")) {
                    lore = GameBox.chatColor(buttonSec.getStringList("skullLore"));
                } else {
                    lore = new ArrayList<>(Arrays.asList("", "No lore specified in the config!"));
                }

                SaveType saveType = gameRules.get(buttonID).getSaveTypes().iterator().next();

                TopListPage topListPage = new TopListPage(gameBox, guiManager, 54, module.moduleID(), buttonID + GUIManager.TOP_LIST_KEY_ADDON,
                        GameBox.chatColor(buttonSec.getString("inventoryTitle", "Title missing in config")), saveType, lore);

                guiManager.registerGameGUI(topListPage);
            }
        }
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

    public GameManager getGameManager() {
        return gameManager;
    }

    public GameLanguage getGameLang(){
        return this.gameLang;
    }

    public void debug(String debugMessage){
        if(debug) Bukkit.getLogger().info(gameLang.PREFIX + " " + debugMessage);
    }

    public void debug(ArrayList<String> debugMessages){
        if(!debug) return;
        for(String message : debugMessages){
            Bukkit.getLogger().info(gameLang.PREFIX + " " + message);
        }
    }
}
