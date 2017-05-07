package me.nikl.gamebox;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Created by Niklas on 07.05.2017.
 *
 * class to store settings
 */
public class GameBoxSettings {
    public static boolean exceptInvitesWithoutPlayPermission = false;



    public static void loadSettings(GameBox plugin){
        FileConfiguration config = plugin.getConfig();

        exceptInvitesWithoutPlayPermission = config.getBoolean("settings.exceptInvitesWithoutPlayPermission", false);
    }
}
