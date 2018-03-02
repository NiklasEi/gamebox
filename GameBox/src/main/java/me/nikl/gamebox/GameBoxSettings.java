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
    public static int autoSaveIntervalInMinutes = 10;
    public static int exitButtonSlot = 4;
    public static int toMainButtonSlot = 0;
    public static int toGameButtonSlot = 8;
    public static int emptyHotBarSlotToHold = 0;
    public static List<Integer> slotsToKeep = new ArrayList<>();

    public static void loadSettings(GameBox plugin) {
        FileConfiguration config = plugin.getConfig();
        useMysql = config.getBoolean("mysql.enabled", false);
        hubModeEnabled = config.getBoolean("hubMode.enabled", false);
        checkInventoryLength = checkInventoryTitleLength();
        guiSettings(config);
        generalSettings(config);
        invitationSettings(config);
        economySettings(config);
    }

    private static void guiSettings(FileConfiguration config) {
        playSounds = config.getBoolean("guiSettings.playSounds", true);
        loadSounds(config);
        loadHotBarSlots(config);
    }

    private static void generalSettings(FileConfiguration config) {
        exceptInvitesWithoutPlayPermission = config.getBoolean("settings.exceptInvitesWithoutPlayPermission", false);
        bStatsMetrics = config.getBoolean("settings.bStats", true);
        closeInventoryOnDamage = config.getBoolean("settings.closeInventoryOnDamage", true);
        autoSaveIntervalInMinutes = config.getInt("settings.autoSaveIntervalInMinutes", 10);
        keepArmorWhileInGame = config.getBoolean("settings.keepArmor", false);
    }

    private static void invitationSettings(FileConfiguration config) {
        sendInviteClickMessage = config.getBoolean("settings.invitations.clickMessage.enabled", true);
        inviteValidDuration = config.getInt("settings.invitations.inviteValidDuration", 60);
        inviteInputDuration = config.getInt("settings.invitations.inviteInputDuration", 30);
    }

    private static void economySettings(FileConfiguration config) {
        tokensEnabled = config.getBoolean("economy.tokens.enabled", false);
        econEnabled = config.getBoolean("economy.enabled", false);
    }

    private static void loadSounds(FileConfiguration config) {
        try {
            successfulClick = Sound.valueOf(config.getString("guiSettings.standardSounds.successfulClick", "CLICK"));
        } catch (IllegalArgumentException exception) {
            successfulClick = Sound.CLICK;
        }
        try {
            unsuccessfulClick = Sound.valueOf(config.getString("guiSettings.standardSounds.unsuccessfulClick", "VILLAGER_NO"));
        } catch (IllegalArgumentException exception) {
            unsuccessfulClick = Sound.VILLAGER_NO;
        }
    }

    private static void loadHotBarSlots(FileConfiguration config) {
        exitButtonSlot = config.getInt("guiSettings.hotBarNavigation.exitSlot", 4);
        toMainButtonSlot = config.getInt("guiSettings.hotBarNavigation.mainMenuSlot", 0);
        toGameButtonSlot = config.getInt("guiSettings.hotBarNavigation.gameMenuSlot", 8);
        // make sure the buttons are actually in the hot bar
        if (exitButtonSlot > 8) exitButtonSlot = 4;
        if (toMainButtonSlot > 8) toMainButtonSlot = 0;
        if (toGameButtonSlot > 8) toGameButtonSlot = 8;
        loadSlotsThatKeepTheirItems(config);
        findEmptyHotBarSlotToHold(config);
    }

    private static void findEmptyHotBarSlotToHold(FileConfiguration config) {
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

    private static void loadSlotsThatKeepTheirItems(FileConfiguration config) {
        if (config.isSet("guiSettings.keepItemsSlots")
                && config.isList("guiSettings.keepItemsSlots")) {
            slotsToKeep = config.getIntegerList("guiSettings.keepItemsSlots");
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
