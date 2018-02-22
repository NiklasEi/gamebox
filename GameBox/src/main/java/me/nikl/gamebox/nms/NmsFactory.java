package me.nikl.gamebox.nms;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import org.bukkit.Bukkit;

/**
 * @author Niklas Eicker
 */
public class NmsFactory {
    private final static String VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private static NmsUtility nmsUtility;

    public static NmsUtility getNmsUtility() {
        if (nmsUtility != null) return nmsUtility;
        GameBox.debug("Your server is running version " + VERSION);
        switch (VERSION) {
            case "v1_8_R1":
                GameBoxSettings.version1_8 = true;
                return nmsUtility = new NmsUtility_1_8_R1();
            case "v1_8_R2":
                GameBoxSettings.version1_8 = true;
                return nmsUtility = new NmsUtility_1_8_R2();
            case "v1_8_R3":
                GameBoxSettings.version1_8 = true;
                return nmsUtility = new NmsUtility_1_8_R3();
            case "v1_9_R1":
                return nmsUtility = new NmsUtility_1_9_R1();
            case "v1_9_R2":
                return nmsUtility = new NmsUtility_1_9_R2();
            case "v1_10_R1":
                return nmsUtility = new NmsUtility_1_10_R1();
            case "v1_11_R1":
                return nmsUtility = new NmsUtility_1_11_R1();
            case "v1_12_R1":
                return nmsUtility = new NmsUtility_1_12_R1();
            default:
                return null;
        }
    }
}
