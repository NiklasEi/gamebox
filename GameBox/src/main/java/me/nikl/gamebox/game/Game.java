package me.nikl.gamebox.game;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxLanguage;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.Module;
import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.game.exceptions.GameLoadException;
import me.nikl.gamebox.game.manager.GameManager;
import me.nikl.gamebox.game.rules.GameRule;
import me.nikl.gamebox.game.rules.GameRuleMultiRewards;
import me.nikl.gamebox.game.rules.GameRuleRewards;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.inventory.GUIManager;
import me.nikl.gamebox.inventory.button.Button;
import me.nikl.gamebox.inventory.gui.game.GameGui;
import me.nikl.gamebox.inventory.gui.game.StartMultiplayerGamePage;
import me.nikl.gamebox.inventory.gui.game.TopListPage;
import me.nikl.gamebox.nms.NmsFactory;
import me.nikl.gamebox.nms.NmsUtility;
import me.nikl.gamebox.utility.ConfigManager;
import me.nikl.gamebox.utility.FileUtility;
import me.nikl.gamebox.utility.InventoryUtility;
import me.nikl.gamebox.utility.ItemStackUtility;
import me.nikl.gamebox.utility.Permission;
import me.nikl.gamebox.utility.Sound;
import me.nikl.gamebox.utility.StringUtility;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    protected GameManager gameManager;
    protected GameSettings gameSettings;
    protected GameLanguage gameLang;
    protected GameBoxLanguage gbLang;
    protected NmsUtility nms;
    private File dataFolder;
    private File configFile;

    protected Game(GameBox gameBox, String gameID) {
        this.module = gameBox.getGameRegistry().getModule(gameID);
        Validate.notNull(module, " You cannot initialize a game without registering it's module first!");
        this.gameBox = gameBox;
        this.gbLang = gameBox.lang;
        this.nms = NmsFactory.getNmsUtility();
        this.gameSettings = new GameSettings();
    }

    public abstract void onDisable();

    public void onEnable() throws GameLoadException {
        GameBox.debug(" enabling the game: " + module.getModuleID());
        loadConfig();
        loadSettings();
        loadLanguage();
        ConfigManager.registerModuleLanguage(module, gameLang);
        checkRequirements();
        // at this point the game can load any game specific stuff (e.g. from config)
        init();
        loadGameManager();
        hook();
    }

    private void checkRequirements() throws GameLoadException {
        checkGameBoxVersion();
    }

    private void checkGameBoxVersion() throws GameLoadException {
        String[] versionString = gameBox.getDescription().getVersion().replaceAll("[^0-9.]", "").split("\\.");
        String[] minVersionString = gameSettings.getGameBoxMinimumVersion().split("\\.");
        Integer[] version = new Integer[versionString.length];
        Integer[] minVersion = new Integer[minVersionString.length];
        for(int i = 0; i < minVersionString.length; i++){
            try {
                minVersion[i] = Integer.valueOf(minVersionString[i]);
                version[i] = Integer.valueOf(versionString[i]);
            } catch (NumberFormatException exception){
                warn(" Failed to check required GameBox version!");
                warn("     Lets hope it works...");
                return;
            }
        }
        if (minVersion.length != version.length) {
            warn(" Failed to check required GameBox version!");
            warn("     Lets hope it works...");
            return;
        }
        for(int i = 0; i < minVersion.length; i++){
            if(minVersion[i] > version[i]) {
                warn(" Your GameBox is outdated!");
                warn(" Get the latest version on Spigot.");
                warn(" https://www.spigotmc.org/resources/37273/");
                warn(" You need at least version " + gameSettings.getGameBoxMinimumVersion());
                warn(" for this game to work.");
                throw new GameLoadException("GameBox version is not compatible!");
            }
            if(minVersion[i] == version[i]) continue;
            return;
        }
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
     * Set all game settings
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
     * Initialize the GameManager
     *
     * Gets called on enable of the the game.
     * Initialize the GameManager and save it to its field.
     */
    public abstract void loadGameManager();

    public void loadConfig() throws GameLoadException {
        GameBox.debug(" load config... (" + module.getModuleID() + ")");
        configFile = new File(gameBox.getDataFolder()
                + File.separator + "games"
                + File.separator + getGameID()
                + File.separator + "config.yml");
        if (!configFile.exists()) {
            GameBox.debug(" default config missing in GB folder (" + module.getModuleID() + ")");
            configFile.getParentFile().mkdirs();
            if (module.getExternalPlugin() != null) {
                FileUtility.copyExternalResources(gameBox, module);
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
        try {
            this.config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(configFile), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
            throw new GameLoadException("Failed to load the configuration", e);
        }
        ConfigManager.registerModuleConfiguration(module, config);
    }

    private void hook() throws GameLoadException {
        GUIManager guiManager = gameBox.getPluginManager().getGuiManager();
        int gameGuiSlots = gameSettings.getGameGuiSize();
        GameGui gameGui = new GameGui(gameBox, this, gameGuiSlots);
        gameGui.setHelpButton(gameLang.GAME_HELP);
        if (config.isConfigurationSection("gameBox.gameButtons")) {
            ConfigurationSection gameButtons = config.getConfigurationSection("gameBox.gameButtons");
            ConfigurationSection buttonSec;
            String displayName;
            List<String> lore;
            for (String buttonID : gameButtons.getKeys(false)) {
                buttonSec = gameButtons.getConfigurationSection(buttonID);
                if (!buttonSec.isString("materialData")) {
                    Bukkit.getLogger().log(Level.WARNING, " missing material data under: gameBox.gameButtons." + buttonID + "        can not load the button");
                    continue;
                }
                ItemStack mat = ItemStackUtility.getItemStack(buttonSec.getString("materialData"));
                if (mat == null) {
                    Bukkit.getLogger().log(Level.WARNING, " error loading: gameBox.gameButtons." + buttonID);
                    Bukkit.getLogger().log(Level.WARNING, "     invalid material data");
                    continue;
                }
                Button button = new Button(mat);
                ItemMeta meta = button.getItemMeta();
                if (buttonSec.isString("displayName")) {
                    displayName = StringUtility.color(buttonSec.getString("displayName"));
                    meta.setDisplayName(displayName);
                }
                if (buttonSec.isList("lore")) {
                    lore = StringUtility.color(buttonSec.getStringList("lore"));
                    meta.setLore(lore);
                }
                switch (gameSettings.getGameType()) {
                    case SINGLE_PLAYER:
                        button.setAction(ClickAction.START_GAME);
                        break;
                    case TWO_PLAYER:
                        guiManager.registerGameGUI(new StartMultiplayerGamePage(gameBox, guiManager
                                , gameSettings.getGameGuiSize()
                                , getGameID(), buttonID, StringUtility.color(buttonSec
                                .getString("inviteGuiTitle", "&4title not set in config"))));
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
        if (config.isConfigurationSection("gameBox.mainButton")) {
            ConfigurationSection mainButtonSec = config.getConfigurationSection("gameBox.mainButton");
            if (!mainButtonSec.isString("materialData")) {
                warn(" Missing or invalid material data for main button ");
                throw new GameLoadException("Cannot load the main button from 'config.yml'");
            }
            ItemStack gameButton = ItemStackUtility.getItemStack(mainButtonSec.getString("materialData"));
            if (gameButton == null) {
                warn(" Invalid material data for main button");
                warn(" Using a default...");
                gameButton = (new ItemStack(Material.STAINED_CLAY));
            }
            ItemMeta meta = gameButton.getItemMeta();
            meta.setDisplayName(StringUtility.color(mainButtonSec.getString("displayName", gameLang.PLAIN_NAME)));
            if (mainButtonSec.isList("lore")) {
                meta.setLore(StringUtility.color(mainButtonSec.getStringList("lore")));
            }
            gameButton.setItemMeta(meta);
            guiManager.registerMainGameGUI(gameGui, gameButton);
        } else {
            warn(" Missing or wrong configured main button");
            throw new GameLoadException("Cannot load the main button from 'config.yml'");
        }
        Map<String, ? extends GameRule> gameRules = gameManager.getGameRules();
        if (gameRules == null || gameRules.isEmpty()) {
            throw new GameLoadException("Game manager failed to return any valid game rules.");
        }
        // get top list buttons
        if (config.isConfigurationSection("gameBox.topListButtons")) {
            ConfigurationSection topListButtons = config.getConfigurationSection("gameBox.topListButtons");
            ConfigurationSection buttonSec;
            List<String> lore;
            for (String buttonID : topListButtons.getKeys(false)) {
                buttonSec = topListButtons.getConfigurationSection(buttonID);
                if (!gameRules.keySet().contains(buttonID)) {
                    warn(" the top list button 'gameBox.topListButtons." + buttonID + "' does not have a corresponding game button");
                    continue;
                }
                if (!gameRules.get(buttonID).isSaveStats()) {
                    warn(" There is a configured top list for '" + buttonID + "', but statistics is turned off!");
                    warn(" With these settings there is no top list to display...");
                    warn(" Set 'gameBox.gameButtons." + buttonID + ".saveStats' to 'true', to enable this top list.");
                    continue;
                }
                if (!buttonSec.isString("materialData")) {
                    warn(" missing material data: 'gameBox.topListButtons." + buttonID + "'. Cannot load the button!");
                    continue;
                }
                ItemStack mat = ItemStackUtility.getItemStack(buttonSec.getString("materialData"));
                if (mat == null) {
                    warn(" error loading: gameBox.topListButtons." + buttonID);
                    warn("     invalid material data");
                    continue;
                }
                Button button = new Button(mat);
                ItemMeta meta = button.getItemMeta();
                if (buttonSec.isString("displayName")) {
                    meta.setDisplayName(StringUtility.color(buttonSec.getString("displayName")));
                }
                if (buttonSec.isList("lore")) {
                    lore = StringUtility.color(buttonSec.getStringList("lore"));
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
                    lore = StringUtility.color(buttonSec.getStringList("skullLore"));
                } else {
                    lore = new ArrayList<>(Arrays.asList("", "No lore specified in the config!"));
                }
                SaveType saveType = gameRules.get(buttonID).getSaveType();
                TopListPage topListPage = new TopListPage(gameBox, guiManager, getGameID(), buttonID + GUIManager.TOP_LIST_KEY_ADDON,
                        StringUtility.color(buttonSec.getString("inventoryTitle", "Title missing in config")), saveType, lore);
                guiManager.registerGameGUI(topListPage);
            }
        }
    }

    public FileConfiguration getConfig() {
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

    public GameBox getGameBox() {
        return this.gameBox;
    }

    public File getDataFolder() {
        return this.dataFolder;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public GameLanguage getGameLang() {
        return this.gameLang;
    }

    public void debug(String debugMessage) {
        if (debug) Bukkit.getLogger().info(gameLang.PREFIX + " " + debugMessage);
    }

    public void debug(ArrayList<String> debugMessages) {
        if (!debug) return;
        for (String message : debugMessages) {
            Bukkit.getLogger().info(gameLang.PREFIX + " " + message);
        }
    }

    private boolean pay(Player player, double cost) {
        return pay(player, cost, true);
    }

    /**
     * Withdraw the specified cost from the players balance,
     * if economy is enabled (for GameBox and the game) and the
     * player does not have a bypass permission.
     *
     * @param player that has to pay
     * @param cost to pay
     * @param withdraw whether to actually withdraw the money
     * @return whether player can enter the game now
     */
    public boolean payIfNecessary(Player player, double cost, boolean withdraw) {
        if (GameBoxSettings.econEnabled && gameSettings.isEconEnabled()
                && !Permission.BYPASS_GAME.hasPermission(player, getGameID())
                && cost > 0.0) {
            return pay(player, cost, withdraw);
        }
        return true;
    }

    public boolean payIfNecessary(Player player, double cost) {
        return payIfNecessary(player, cost, true);
    }

    public void payIfNecessary(Player[] players, double cost) {
        for (Player player : players) payIfNecessary(player, cost, true);
    }

    public boolean pay(Player player, double cost, boolean withdraw) {
        if (GameBox.econ.getBalance(player) >= cost) {
            if (withdraw) {
                GameBox.econ.withdrawPlayer(player, cost);
                player.sendMessage(StringUtility.color(gameLang.PREFIX
                        + gameLang.GAME_PAYED
                        .replaceAll("%cost%", String.valueOf(cost))));
            }
            return true;
        } else {
            player.sendMessage(StringUtility.color(gameLang.PREFIX
                    + gameLang.GAME_NOT_ENOUGH_MONEY
                    .replaceAll("%cost%", String.valueOf(cost))));
            return false;
        }
    }

    public void warn(String message) {
        gameBox.getLogger().warning(gameLang.PLAIN_PREFIX + StringUtility.color(message
                .replace("%config%", "GameBox/games/" + getGameID() + "/config.yml")));
    }

    public void info(String message) {
        gameBox.getLogger().info(gameLang.PLAIN_PREFIX + StringUtility.color(message
                .replace("%config%", "GameBox/games/" + getGameID() + "/config.yml")));
    }

    public Inventory createInventory(int size, String title) {
        return InventoryUtility.createInventory(gameManager, size, title);
    }

    public void playSound(Player player, Sound sound) {
        playSound(player, sound, 0.5f, 10f);
    }

    public void playSound(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound.bukkitSound(), volume, pitch);
    }

    public void onGameWon(Player player, GameRule rule, double score){
        if(rule.isSaveStats()) gameBox.getDataBase()
                .addStatistics(player.getUniqueId(), getGameID(), rule.getKey(), score, rule.getSaveType());
        if(hasBypassPermission(player)) return;
        if(rule instanceof GameRuleRewards) payOut(player, (GameRuleRewards) rule, score);
        if(rule instanceof GameRuleMultiRewards) payOut(player, (GameRuleMultiRewards) rule, score);
    }

    private void payOut(Player player, GameRuleRewards rule, double score) {
        if((rule.getSaveType().isHigherScore() && score < rule.getMinOrMaxScore())
                || (!rule.getSaveType().isHigherScore() && score > rule.getMinOrMaxScore())) return;
        if(GameBoxSettings.econEnabled && gameSettings.isEconEnabled()) {
            GameBox.econ.depositPlayer(player, rule.getMoneyToWin());
        }
        if(GameBoxSettings.tokensEnabled) gameBox.getApi().giveToken(player, rule.getTokenToWin());
    }

    private void payOut(Player player, GameRuleMultiRewards rule, double score) {
        double money = rule.getMoneyToWin(score);
        int token = rule.getTokenToWin(score);
        if(GameBoxSettings.econEnabled && gameSettings.isEconEnabled() && money > 0) {
            GameBox.econ.depositPlayer(player, money);
        }
        if(GameBoxSettings.tokensEnabled && token > 0) gameBox.getApi().giveToken(player, token);
    }

    private boolean hasBypassPermission(Player winner) {
        return Permission.BYPASS_GAME.hasPermission(winner, getGameID());
    }
}
