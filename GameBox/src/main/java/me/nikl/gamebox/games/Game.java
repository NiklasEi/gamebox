package me.nikl.gamebox.games;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxLanguage;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.Module;
import me.nikl.gamebox.data.SaveType;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import me.nikl.gamebox.guis.gui.game.GameGui;
import me.nikl.gamebox.guis.gui.game.StartMultiplayerGamePage;
import me.nikl.gamebox.guis.gui.game.TopListPage;
import me.nikl.gamebox.nms.NMSUtil;
import me.nikl.gamebox.util.*;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
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
    protected File configFile;

    protected GameManager gameManager;

    protected GameSettings gameSettings;

    protected GameLanguage gameLang;

    protected GameBoxLanguage gbLang;

    protected NMSUtil nms;

    protected String[] subCommands;

    protected Game(GameBox gameBox, String gameID, String[] subCommands){
        this.module = gameBox.getGameRegistry().getModule(gameID);
        Validate.notNull(module, " You cannot initialize a game without registering it's module first!");

        this.subCommands = subCommands;
        this.gameBox = gameBox;
        this.gbLang = gameBox.lang;
        this.nms = gameBox.getNMS();

        this.gameSettings = new GameSettings();
    }

    public abstract void onDisable();

    public void onEnable(){
        GameBox.debug(" enabling the game: " + module.getModuleID());
        loadConfig();

        // abstract
        loadSettings();
        loadLanguage();

        // at this point the game can load any game specific stuff (e.g. from config)
        init();

        loadGameManager();

        hook();
    }

    /**
     * Initialize the game
     *
     * At this point the settings and the language are set,
     * but not the manager.
     */
    public abstract void init();

    /**
     * This method will be called on enable,
     * after the configuration file is loaded.
     *
     * Load all game settings
     */
    public abstract void loadSettings();

    /**
     * This method will be called on enable,
     * after the configuration file is loaded.
     *
     * Load the game language
     */
    public abstract void loadLanguage();

    /**
     * Get a new GameManager
     *
     * Gets called on enable of the the game.
     * Initialize the GameManager and save it.
     */
    public abstract void loadGameManager();

    public boolean loadConfig(){
        GameBox.debug(" load config... (" + module.getModuleID() + ")");
        configFile = new File(gameBox.getDataFolder()
                + File.separator + "games"
                + File.separator + getGameID()
                + File.separator + "config.yml");

        if(!configFile.exists()){
            GameBox.debug(" default config missing in GB folder (" + module.getModuleID() + ")");
            configFile.getParentFile().mkdirs();
            if(module.getExternalPlugin() != null){
                FileUtil.copyExternalResources(gameBox, module);
            } else {
                gameBox.saveResource("games"
                        + File.separator + getGameID()
                        + File.separator + "config.yml", false);
            }
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
                    displayName = StringUtil.color(buttonSec.getString("displayName"));
                    meta.setDisplayName(displayName);
                }

                if (buttonSec.isList("lore")) {
                    lore = StringUtil.color(buttonSec.getStringList("lore"));
                    meta.setLore(lore);
                }

                switch (gameSettings.getGameType()){
                    case SINGLE_PLAYER:
                        button.setAction(ClickAction.START_GAME);
                        break;

                    case TWO_PLAYER:
                        guiManager.registerGameGUI(new StartMultiplayerGamePage(gameBox, guiManager
                                , gameSettings.getGameGuiSize()
                                , getGameID(), buttonID, StringUtil.color(buttonSec
                                .getString("inviteGuiTitle","&4title not set in config"))));

                        button.setAction(ClickAction.OPEN_GAME_GUI);
                        break;

                    default:
                        gameBox.getLogger().warning("Unhandled game type!");
                        break;
                }


                button.setItemMeta(meta);
                button.setArgs(getGameID(), buttonID);

                // from here it is game specific info
                gameManager.loadGameRules(buttonSec, buttonID);

                if (buttonSec.isInt("slot")) {
                    int slot = buttonSec.getInt("slot");
                    if (slot < 0 || slot >= gameGuiSlots) {
                        Bukkit.getLogger().log(Level.WARNING, "the slot of gameBox.gameButtons." + buttonID
                                + " is out of the inventory range (0 - " + (gameGuiSlots - 1) + ")");
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

            meta.setDisplayName(StringUtil.color(mainButtonSec.getString("displayName", gameLang.PLAIN_NAME)));

            if (mainButtonSec.isList("lore")) {
                meta.setLore(StringUtil.color(mainButtonSec.getStringList("lore")));
            }

            gameButton.setItemMeta(meta);

            guiManager.registerMainGameGUI(gameGui, gameButton, this.subCommands);
        } else {
            gameBox.getLogger().log(Level.WARNING, " Missing or wrong configured main button for " + gameLang.PLAIN_NAME + "!");
        }

        Map<String, ? extends GameRule> gameRules = gameManager.getGameRules();
        if(gameRules == null || gameRules.isEmpty()){
            gameBox.getLogger().log(Level.WARNING, " While loading " + gameLang.DEFAULT_PLAIN_NAME
                    + " the game manager failed to return any valid game rules!");
            return;
            // Todo: system to unregister from guis...
        }

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
                    gameBox.getLogger().log(Level.WARNING, " There is a configured top list for '" + buttonID + "', but statistics is turned off!");
                    gameBox.getLogger().log(Level.WARNING, " With these settings there is no top list to display...");
                    gameBox.getLogger().log(Level.WARNING, " Set 'gameBox.gameButtons." + buttonID + ".saveStats' to 'true', to enable this top list.");
                    continue;
                }

                if (!buttonSec.isString("materialData")) {
                    gameBox.getLogger().log(Level.WARNING, " missing material data: 'gameBox.topListButtons." + buttonID + "'. Cannot load the button!");
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
                    meta.setDisplayName(StringUtil.color(buttonSec.getString("displayName")));
                }

                if (buttonSec.isList("lore")) {
                    lore = StringUtil.color(buttonSec.getStringList("lore"));
                    meta.setLore(lore);
                }

                button.setItemMeta(meta);
                button.setAction(ClickAction.SHOW_TOP_LIST);
                button.setArgs(getGameID(), buttonID + GUIManager.TOP_LIST_KEY_ADDON);


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
                    lore = StringUtil.color(buttonSec.getStringList("skullLore"));
                } else {
                    lore = new ArrayList<>(Arrays.asList("", "No lore specified in the config!"));
                }

                SaveType saveType = gameRules.get(buttonID).getSaveTypes().iterator().next();

                TopListPage topListPage = new TopListPage(gameBox, guiManager, 54, getGameID(), buttonID + GUIManager.TOP_LIST_KEY_ADDON,
                        StringUtil.color(buttonSec.getString("inventoryTitle", "Title missing in config")), saveType, lore);

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
        return module.getModuleID();
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

    public boolean pay(Player player, double cost){
        return pay(player, cost, true);
    }

    public boolean pay(Player player, double cost, boolean withdraw) {
        if (GameBoxSettings.econEnabled
                && !player.hasPermission(Permission.BYPASS_ALL.getPermission())
                && !player.hasPermission(Permission.BYPASS_GAME.getPermission(getGameID()))
                && cost > 0.0) {
            if (GameBox.econ.getBalance(player) >= cost) {
                if(withdraw) {
                    GameBox.econ.withdrawPlayer(player, cost);
                    player.sendMessage(StringUtil.color(gameLang.PREFIX
                            + gameLang.GAME_PAYED
                            .replaceAll("%cost%", String.valueOf(cost))));
                }
                return true;
            } else {
                player.sendMessage(StringUtil.color(gameLang.PREFIX
                        + gameLang.GAME_NOT_ENOUGH_MONEY
                        .replaceAll("%cost%", String.valueOf(cost))));
                return false;
            }
        } else {
            return true;
        }
    }

    public void warn(String message){
        gameBox.getLogger().warning(" " + gameLang.PLAIN_PREFIX + message
                .replace("%config%", "GameBox/games/" + getGameID() + "/config.yml"));
    }

    public void info(String message){
        gameBox.getLogger().info(" " + gameLang.PLAIN_PREFIX + message
                .replace("%config%", "GameBox/games/" + getGameID() + "/config.yml"));
    }
}
