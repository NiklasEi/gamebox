package me.nikl.gamebox.util;

import me.nikl.gamebox.GameBox;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by nikl on 23.10.17.
 *
 * Utility class for language related stuff
 */
public class FileUtil {

    /**
     * Copy all default language files to the plugin folder
     *
     * This method checks for every .yml in the language folder
     * whether it is already present in the plugins language folder.
     * If not it is copied.
     */
    public static void copyDefaultLanguageFiles(){
        URL main = GameBox.class.getResource("GameBox.class");

        try {
            JarURLConnection connection = (JarURLConnection) main.openConnection();

            JarFile jar = new JarFile(connection.getJarFileURL().getFile());
            Plugin gameBox = Bukkit.getPluginManager().getPlugin("GameBox");
            for (Enumeration list = jar.entries(); list.hasMoreElements(); ) {
                JarEntry entry = (JarEntry) list.nextElement();
                if(entry.getName().split(File.separator)[0].equals("language")) {

                    String[] pathParts = entry.getName().split(File.separator);

                    if (pathParts.length < 2 || !entry.getName().endsWith(".yml")){
                        continue;
                    }

                    File file = new File(gameBox.getDataFolder().toString() + File.separatorChar + entry.getName());
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        gameBox.saveResource(entry.getName(), false);
                    }
                }
            }
            jar.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
