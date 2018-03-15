package me.nikl.gamebox;

import me.nikl.gamebox.utility.Sound;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Niklas Eicker
 *
 * class to store global settings
 */
public class GameBoxSettings {
    public static boolean exceptInvitesWithoutPlayPermission = false;
    public static boolean playSounds = true; //toggle for playing sounds
    public static Sound successfulClick, unsuccessfulClick;
    public static boolean checkInventoryLength = false;
    public static boolean useMysql = false;
    public static boolean version1_8 = false;
    public static int inviteInputDuration = 30; // time in seconds for inputs
    public static int inviteValidDuration = 60; // time in seconds for invitations
    public static boolean econEnabled = false;
    public static boolean tokensEnabled = false;
    public static boolean keepArmorWhileInGame = false;
    public static boolean sendInviteClickMessage = true;
    public static boolean bStatsMetrics = true;
    public static boolean hubModeEnabled = false;
    public static boolean closeInventoryOnDamage = true;
    public static boolean runLanguageChecksAutomatically = true;
    public static int autoSaveIntervalInMinutes = 10;
    public static int exitButtonSlot = 4;
    public static int toMainButtonSlot = 0;
    public static int toGameButtonSlot = 8;
    public static int emptyHotBarSlotToHold = 0;
    public static List<Integer> slotsToKeep = new ArrayList<>();
    // split aliases with pipes
    public static String mainCommand;
    public static String adminCommand;

    private static FileConfiguration configuration;

    public static void loadSettings(GameBox plugin) {
        configuration = plugin.getConfig();
        loadSpecialVersionSettings();
        loadCommands();
        useMysql = configuration.getBoolean("mysql.enabled", false);
        hubModeEnabled = configuration.getBoolean("hubMode.enabled", false);
        checkInventoryLength = checkInventoryTitleLength();
        guiSettings();
        generalSettings();
        invitationSettings();
        economySettings();
    }

    private static void loadSpecialVersionSettings() {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        switch (version) {
            case "v1_8_R1":
            case "v1_8_R2":
            case "v1_8_R3":
                version1_8 = true;
                return;
            default:
                return;
        }
    }

    private static void loadCommands() {
        mainCommand = configuration.getString("commands.main", "gamebox|games|gb");
        adminCommand = configuration.getString("commands.admin", "gameboxadmin|gamesadmin|gba");
    }

    private static void guiSettings() {
        playSounds = configuration.getBoolean("guiSettings.playSounds", true);
        loadSounds();
        loadHotBarSlots();
    }

    private static void generalSettings() {
        exceptInvitesWithoutPlayPermission = configuration.getBoolean("settings.exceptInvitesWithoutPlayPermission", false);
        bStatsMetrics = configuration.getBoolean("settings.bStats", true);
        closeInventoryOnDamage = configuration.getBoolean("settings.closeInventoryOnDamage", true);
        autoSaveIntervalInMinutes = configuration.getInt("settings.autoSaveIntervalInMinutes", 10);
        keepArmorWhileInGame = configuration.getBoolean("settings.keepArmor", false);
        runLanguageChecksAutomatically = configuration.getBoolean("settings.runLanguageChecksAutomatically", true);
    }

    private static void invitationSettings() {
        sendInviteClickMessage = configuration.getBoolean("settings.invitations.clickMessage.enabled", true);
        inviteValidDuration = configuration.getInt("settings.invitations.inviteValidDuration", 60);
        inviteInputDuration = configuration.getInt("settings.invitations.inviteInputDuration", 30);
    }

    private static void economySettings() {
        tokensEnabled = configuration.getBoolean("economy.tokens.enabled", false);
        econEnabled = configuration.getBoolean("economy.enabled", false);
    }

    private static void loadSounds() {
        try {
            successfulClick = Sound.valueOf(configuration.getString("guiSettings.standardSounds.successfulClick", "CLICK"));
        } catch (IllegalArgumentException exception) {
            successfulClick = Sound.CLICK;
        }
        try {
            unsuccessfulClick = Sound.valueOf(configuration.getString("guiSettings.standardSounds.unsuccessfulClick", "VILLAGER_NO"));
        } catch (IllegalArgumentException exception) {
            unsuccessfulClick = Sound.VILLAGER_NO;
        }
    }

    private static void loadHotBarSlots() {
        exitButtonSlot = configuration.getInt("guiSettings.hotBarNavigation.exitSlot", 4);
        toMainButtonSlot = configuration.getInt("guiSettings.hotBarNavigation.mainMenuSlot", 0);
        toGameButtonSlot = configuration.getInt("guiSettings.hotBarNavigation.gameMenuSlot", 8);
        // make sure the buttons are actually in the hot bar
        if (exitButtonSlot > 8) exitButtonSlot = 4;
        if (toMainButtonSlot > 8) toMainButtonSlot = 0;
        if (toGameButtonSlot > 8) toGameButtonSlot = 8;
        loadSlotsThatKeepTheirItems();
        findEmptyHotBarSlotToHold();
    }

    private static void findEmptyHotBarSlotToHold() {
        if (3 + slotsToKeep.size() < 9) {
            while (emptyHotBarSlotToHold == exitButtonSlot
                    || emptyHotBarSlotToHold == toMainButtonSlot
                    || emptyHotBarSlotToHold == toGameButtonSlot
                    || slotsToKeep.contains(emptyHotBarSlotToHold)) {
                emptyHotBarSlotToHold++;
            }
        } else {
            while (emptyHotBarSlotToHold == exitButtonSlot
                    || emptyHotBarSlotToHold == toMainButtonSlot
                    || emptyHotBarSlotToHold == toGameButtonSlot) {
                emptyHotBarSlotToHold++;
            }
        }
    }

    private static void loadSlotsThatKeepTheirItems() {
        if (configuration.isSet("guiSettings.keepItemsSlots")
                && configuration.isList("guiSettings.keepItemsSlots")) {
            slotsToKeep = configuration.getIntegerList("guiSettings.keepItemsSlots");
        }
        if (slotsToKeep == null) slotsToKeep = new ArrayList<>();
        Iterator<Integer> it = slotsToKeep.iterator();
        while (it.hasNext()) {
            int slot = it.next();
            if (slot == toMainButtonSlot
                    || slot == exitButtonSlot
                    || slot == toGameButtonSlot)
                it.remove();
            if (slot < 0 || slot > 8) it.remove();
        }
    }

    private static boolean checkInventoryTitleLength() {
        try {
            Bukkit.createInventory(null, 27, "This title is longer then 32 characters!");
        } catch (Exception e) {
            return true;
        }
        return false;
    }
}
