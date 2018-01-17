package me.nikl.gamebox;

import me.nikl.gamebox.util.Sound;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Created by Niklas on 07.05.2017.
 *
 * class to store global settings
 */
public class GameBoxSettings {

    public static boolean exceptInvitesWithoutPlayPermission = false;

    //toggle for playing sounds
    public static boolean playSounds = true;

    public static Sound successfulClick, unsuccessfulClick;

    public static boolean checkInventoryLength = false;

    public static boolean useMysql = false;

    // is changed in main class
    public static boolean version1_8 = false;

    // time in seconds for inputs and invitations
    public static int inviteInputDuration = 30;
    public static int inviteValidDuration = 60;

    public static boolean econEnabled = false;
    public static boolean tokensEnabled = false;

    public static boolean keepArmor = false;

    public static boolean sendInviteClickMessage = true;

    public static boolean bStats = true;

    public static boolean hubMode = false;

    // what to do on player damage
    public static boolean closeInventoryOnDamage = true;

    public static int autoSaveInterval = 10;


    public static void loadSettings(GameBox plugin){
        FileConfiguration config = plugin.getConfig();

        sendInviteClickMessage = config.getBoolean("settings.invitations.clickMessage.enabled", true);
        tokensEnabled = config.getBoolean("economy.tokens.enabled", false);
        econEnabled = config.getBoolean("economy.enabled", false);
        playSounds = config.getBoolean("guiSettings.playSounds", true);
        inviteValidDuration = config.getInt("settings.invitations.inviteValidDuration", 60);
        inviteInputDuration = config.getInt("settings.invitations.inviteInputDuration", 30);
        useMysql = config.getBoolean("mysql.enabled", false);
        exceptInvitesWithoutPlayPermission = config.getBoolean("settings.exceptInvitesWithoutPlayPermission", false);
        bStats = config.getBoolean("settings.bStats", true);
        closeInventoryOnDamage = config.getBoolean("settings.closeInventoryOnDamage", true);

        autoSaveInterval = config.getInt("settings.autoSaveInterval", 10);

        keepArmor = config.getBoolean("settings.keepArmor", false);

        try{
            successfulClick = Sound.valueOf(config.getString("guiSettings.standardSounds.successfulClick", "CLICK"));
        } catch (IllegalArgumentException exception) {
            successfulClick = Sound.CLICK;
        }
        try{
            unsuccessfulClick = Sound.valueOf(config.getString("guiSettings.standardSounds.unsuccessfulClick", "VILLAGER_NO"));
        } catch (IllegalArgumentException exception) {
            unsuccessfulClick = Sound.VILLAGER_NO;
        }

        checkInventoryLength = checkInventoryTitleLength();

        hubMode = config.getBoolean("hubMode.enabled", false);
    }

    private static boolean checkInventoryTitleLength() {
        try {
            Bukkit.createInventory(null, 27, "This title is longer then 32 characters!");
        } catch (Exception e){
            return true;
        }
        return false;
    }
}
