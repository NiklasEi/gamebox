package me.nikl.gamebox.utility;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nikl on 15.11.17.
 *
 * String related utility functions
 */
public class StringUtility {

    public static List<String> color(List<String> list) {
        ArrayList<String> toReturn = new ArrayList(list);
        for (int i = 0; i < list.size(); i++) {
            toReturn.set(i, color(toReturn.get(i)));
        }
        return toReturn;
    }

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Shorten a passed String to the given length.
     * The last three characters of the new String will be dots.
     *
     * @param string
     * @param length
     * @return shortened String
     */
    public static String shorten(String string, int length) {
        if (string != null && string.length() > length) {
            return string.substring(0, length - 3) + "...";
        } else {
            return string;
        }
    }

    public static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int sec = seconds % 60;
        return (minutes < 10 ? "0" + String.valueOf(minutes) : String.valueOf(minutes))
                + ":" + (sec < 10 ? "0" + String.valueOf(sec) : String.valueOf(sec));
    }

    public static String center(String toCenter, int letterCount) {
        int addToFront = (letterCount - toCenter.length()) / 2;
        StringBuilder builder = new StringBuilder();
        for (int count = 0; count < addToFront; count++) builder.append(" ");
        builder.append(toCenter);
        return builder.toString();
    }
}
