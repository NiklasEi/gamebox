package me.nikl.gamebox.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nikl on 15.11.17.
 *
 * String related utility functions
 */
public class StringUtil {

    public static ArrayList<String> color(List<String> list){
        ArrayList<String> toReturn = new ArrayList(list);
        for(int i = 0; i < list.size(); i++){
            toReturn.set(i, color(toReturn.get(i)));
        }
        return toReturn;
    }

    public static String color(String message){
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
