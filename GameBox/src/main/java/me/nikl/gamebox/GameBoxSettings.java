package me.nikl.gamebox;

import me.nikl.gamebox.utility.Sound;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author Niklas Eicker
 *
 *         class to store global settings
 */
public class GameBoxSettings {
    public static boolean exceptInvitesWithoutPlayPermission = false;
    public static boolean playSounds = true; //toggle for playing sounds
    public static Sound successfulClick, unsuccessfulClick;
    public static boolean checkInventoryLength = false;
    public static boolean useMysql = false;
    public static boolean version1_8 = false; // is changed in main class
    public static int inviteInputDuration = 30; // time in seconds for inputs
    public static int inviteValidDuration = 60; // time in seconds for invitations
    public static boolean econEnabled = false;
    public static boolean tokensEnabled = false;
    public static boolean keepArmorWhileInGame = false;
    public static boolean sendInviteClickMessage = true;
    public static boolean bStatsMetrics = true;
    public static boolean hubModeEnabled = false;
    public static boolean closeInventoryOnDamage = true; // what to do on player damage
    public static int autoSaveInterval = 10;

    public static void loadSettings(GameBox plugin) {
        FileConfiguration config = plugin.getConfig();

        sendInviteClickMessage = config.getBoolean("settings.invitations.clickMessage.enabled", true);
        tokensEnabled = config.getBoolean("economy.tokens.enabled", false);
        econEnabled = config.getBoolean("economy.enabled", false);
        playSounds = config.getBoolean("guiSettings.playSounds", true);
        inviteValidDuration = config.getInt("settings.invitations.inviteValidDuration", 60);
        inviteInputDuration = config.getInt("settings.invitations.inviteInputDuration", 30);
        useMysql = config.getBoolean("mysql.enabled", false);
        exceptInvitesWithoutPlayPermission = config.getBoolean("settings.exceptInvitesWithoutPlayPermission", false);
        bStatsMetrics = config.getBoolean("settings.bStats", true);
        closeInventoryOnDamage = config.getBoolean("settings.closeInventoryOnDamage", true);
        autoSaveInterval = config.getInt("settings.autoSaveInterval", 10);
        keepArmorWhileInGame = config.getBoolean("settings.keepArmor", false);
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
        checkInventoryLength = checkInventoryTitleLength();
        hubModeEnabled = config.getBoolean("hubMode.enabled", false);
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
