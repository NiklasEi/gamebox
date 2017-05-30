package me.nikl.connectfour;

import me.nikl.gamebox.ClickAction;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.data.SaveType;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import me.nikl.gamebox.guis.gui.game.GameGui;
import me.nikl.gamebox.guis.gui.game.StartMultiplayerGamePage;
import me.nikl.gamebox.guis.gui.game.TopListPage;
import me.nikl.gamebox.nms.NMSUtil;
import me.nikl.gamebox.util.ItemStackUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Created by Niklas on 14.04.2017.
 *
 * Main class for the GameBox game 2048
 */
public class Main extends JavaPlugin {
    public static boolean debug = false;
    public static String gameID = "connect4";

    public static Economy econ = null;
    public boolean econEnabled;

    public Language lang;

    private final String[][] depends = new String[][]{
            new String[]{"Vault", "1.5"},
            new String[]{"GameBox", "1.5.0"}
    };

    private final String[] subCommands = new String[]{"connect4", "c4"};
    private final SaveType topListSaveType = SaveType.WINS;
    private final int playerNum = 2;

    GameBox gameBox;
    private FileConfiguration config;
    private File con;
    private boolean disabled, playSounds;
    private GameManager gameManager;

    @Override
    public void onEnable() {

        this.con = new File(this.getDataFolder().toString() + File.separatorChar + "config.yml");

        reload();
        if (disabled) return;

        hook();
        if (disabled) return;
    }


    @Override
    public void onDisable(){
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = (Economy) rsp.getProvider();
        return econ != null;
    }

    private void hook() {
        if (Bukkit.getPluginManager().getPlugin("GameBox") == null || !Bukkit.getPluginManager().getPlugin("GameBox").isEnabled()) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes( '&', " &cGameBox not found!"));
            Bukkit.getLogger().log(Level.SEVERE, "   Get the newest version here:");
            Bukkit.getLogger().log(Level.SEVERE, "   https://www.spigotmc.org/resources/37273/");
            Bukkit.getPluginManager().disablePlugin(this);
            disabled = true;
            return;
        }


        gameBox = (GameBox) Bukkit.getPluginManager().getPlugin("GameBox");

        // Version check
        String[] versionString = gameBox.getDescription().getVersion().replaceAll("[^0-9.]", "").split("\\.");
        String[] minVersionString = depends[1][1].split("\\.");
        Integer[] version = new Integer[versionString.length];
        Integer[] minVersion = new Integer[minVersionString.length];

        for (int i = 0; i < minVersionString.length; i++) {
            try {
                minVersion[i] = Integer.valueOf(minVersionString[i]);
                version[i] = Integer.valueOf(versionString[i]);
            } catch (NumberFormatException exception) {
                exception.printStackTrace();
            }
        }

        for (int i = 0; i < minVersion.length; i++) {
            if (minVersion[i] < version[i]) break;
            if (minVersion[i].equals(version[i])) continue;

            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes( '&', " &cYour GameBox is outdated!"));
            Bukkit.getLogger().log(Level.WARNING, " Get the latest version here: ");
            Bukkit.getLogger().log(Level.WARNING, "   https://www.spigotmc.org/resources/37273/");
            Bukkit.getLogger().log(Level.WARNING, " You need at least version " + depends[1][1]);
            Bukkit.getPluginManager().disablePlugin(this);
            disabled = true;
            return;
        }// version is fine


        // disable economy/sounds if it is disabled for either one of the plugins
        this.econEnabled = this.econEnabled && GameBoxSettings.econEnabled;
        playSounds = playSounds && GameBoxSettings.playSounds;

        GUIManager guiManager = gameBox.getPluginManager().getGuiManager();

        this.gameManager = new GameManager(this);

        gameBox.getPluginManager().registerGame(gameManager, gameID, lang.NAME, playerNum);

        int gameGuiSlots = 54;
        GameGui gameGui = new GameGui(gameBox, guiManager, gameGuiSlots, gameID);
        gameGui.setHelpButton(lang.GAME_HELP);


        Map<String, GameRules> gameTypes = new HashMap<>();

        // Get the buttons and load the GameRules
        if (config.isConfigurationSection("gameBox.gameButtons")) {
            ConfigurationSection gameButtons = config.getConfigurationSection("gameBox.gameButtons");
            ConfigurationSection buttonSec;
            int tokens;
            double cost, reward;
            boolean saveStats;

            String displayName;
            ArrayList<String> lore;

            GameRules rules;

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
                    lore = new ArrayList<>(buttonSec.getStringList("lore"));
                    for (int i = 0; i < lore.size(); i++) {
                        lore.set(i, GameBox.chatColor(lore.get(i)));
                    }
                    meta.setLore(lore);
                }



                guiManager.registerGameGUI(new StartMultiplayerGamePage(gameBox, guiManager, 54,
                        gameID, buttonID, GameBox.chatColor(buttonSec.getString("inviteGuiTitle","&4title not set in config"))));


                button.setItemMeta(meta);
                button.setAction(ClickAction.OPEN_GAME_GUI);
                button.setArgs(gameID, buttonID);


                cost = buttonSec.getDouble("cost", 0.);
                reward = buttonSec.getDouble("reward", 0.);
                tokens = buttonSec.getInt("tokens", 0);
                saveStats = buttonSec.getBoolean("saveStats", false);


                rules = new GameRules(buttonID, cost, reward, tokens, saveStats);

                setTheButton:
                if (buttonSec.isInt("slot")) {
                    int slot = buttonSec.getInt("slot");
                    if (slot < 0 || slot >= gameGuiSlots) {
                        Bukkit.getLogger().log(Level.WARNING, "the slot of gameBox.gameButtons." + buttonID + " is out of the inventory range (0 - 53)");
                        gameGui.setButton(button);
                        break setTheButton;
                    }
                    gameGui.setButton(button, slot);
                } else {
                    gameGui.setButton(button);
                }

                gameTypes.put(buttonID, rules);
            }
        }

        // register all the GameRules with he game manager
        this.gameManager.setGameTypes(gameTypes);


        // load main button
        getMainButton:
        if (config.isConfigurationSection("gameBox.mainButton")) {
            ConfigurationSection mainButtonSec = config.getConfigurationSection("gameBox.mainButton");
            if (!mainButtonSec.isString("materialData")) break getMainButton;

            ItemStack gameButton = ItemStackUtil.getItemStack(mainButtonSec.getString("materialData"));
            if (gameButton == null) {
                gameButton = (new ItemStack(Material.STAINED_CLAY));
            }
            ItemMeta meta = gameButton.getItemMeta();
            meta.setDisplayName(GameBox.chatColor(mainButtonSec.getString("displayName", lang.NAME)));
            if (mainButtonSec.isList("lore")) {
                ArrayList<String> lore = new ArrayList<>(mainButtonSec.getStringList("lore"));
                for (int i = 0; i < lore.size(); i++) {
                    lore.set(i, GameBox.chatColor(lore.get(i)));
                }
                meta.setLore(lore);
            }
            gameButton.setItemMeta(meta);
            guiManager.registerMainGameGUI(gameGui, gameButton, this.subCommands);
        } else {
            Bukkit.getLogger().log(Level.WARNING, " Missing or wrong configured main button in the configuration file!");
        }


        // get top list buttons
        if (config.isConfigurationSection("gameBox.topListButtons")) {
            ConfigurationSection topListButtons = config.getConfigurationSection("gameBox.topListButtons");
            ConfigurationSection buttonSec;

            ArrayList<String> lore;


            for (String buttonID : topListButtons.getKeys(false)) {
                buttonSec = topListButtons.getConfigurationSection(buttonID);

                if (!gameTypes.keySet().contains(buttonID)) {
                    Bukkit.getLogger().log(Level.WARNING, " the top list button 'gameBox.topListButtons." + buttonID + "' does not have a corresponding game button");
                    continue;
                }


                if (!gameTypes.get(buttonID).isSaveStats()) {
                    Bukkit.getLogger().log(Level.WARNING, " the top list buttons 'gameBox.topListButtons." + buttonID + "' corresponding game button has statistics turned off!");
                    Bukkit.getLogger().log(Level.WARNING, " With these settings there is no toplist to display");
                    continue;
                }

                if (!buttonSec.isString("materialData")) {
                    Bukkit.getLogger().log(Level.WARNING, " missing material data under: gameBox.topListButtons." + buttonID + "        can not load the button");
                    continue;
                }

                ItemStack mat = ItemStackUtil.getItemStack(buttonSec.getString("materialData"));
                if (mat == null) {
                    Bukkit.getLogger().log(Level.WARNING, " error loading: gameBox.topListButtons." + buttonID);
                    Bukkit.getLogger().log(Level.WARNING, "     invalid material data");
                    continue;
                }


                AButton button = new AButton(mat.getData(), 1);
                ItemMeta meta = button.getItemMeta();

                if (buttonSec.isString("displayName")) {
                    meta.setDisplayName(GameBox.chatColor(buttonSec.getString("displayName")));
                }


                if (buttonSec.isList("lore")) {
                    lore = new ArrayList<>(buttonSec.getStringList("lore"));
                    for (int i = 0; i < lore.size(); i++) {
                        lore.set(i, GameBox.chatColor(lore.get(i)));
                    }
                    meta.setLore(lore);
                }

                button.setItemMeta(meta);
                button.setAction(ClickAction.SHOW_TOP_LIST);
                button.setArgs(gameID, buttonID + GUIManager.TOP_LIST_KEY_ADDON);


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
                    lore = new ArrayList<>(buttonSec.getStringList("skullLore"));
                    for (int i = 0; i < lore.size(); i++) {
                        lore.set(i, GameBox.chatColor(lore.get(i)));
                    }
                } else {
                    lore = new ArrayList<>(Arrays.asList("", "No lore specified in the config!"));
                }

                TopListPage topListPage = new TopListPage(gameBox, guiManager, 54, gameID
                        , buttonID + GUIManager.TOP_LIST_KEY_ADDON
                        , GameBox.chatColor(buttonSec.getString("inventoryTitle", "Title missing in config"))
                        , this.topListSaveType, lore);

                guiManager.registerGameGUI(topListPage);
            }
        }
    }


    public void reloadConfig() {
        try {
            this.config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(this.con), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            disabled = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            disabled = true;
        }
    }

    public void reload() {
        if (!con.exists()) {
            this.saveResource("config.yml", false);
        }
        reloadConfig();

        this.lang = new Language(this);


        if (config.isBoolean("rules.playSounds")) {
            playSounds = config.getBoolean("rules.playSounds");
        }

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
        return gameBox.getNMS();
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public boolean getPlaySounds() {
        return playSounds;
    }

    public boolean isEconEnabled(){
        return this.econEnabled;
    }
}
