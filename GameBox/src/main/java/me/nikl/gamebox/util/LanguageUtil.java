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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by nikl on 23.10.17.
 *
 * Utility class for language related stuff
 */
public class LanguageUtil {

    private static Map<String, FileConfiguration> languageFiles = new HashMap<>();

    private static Map<String, FileConfiguration> defaultLanguageFiles = new HashMap<>();

    /**
     * Register a language file with a namespace.
     *
     * Each namespace needs a default language file
     * at 'language/namespace/lang_en.yml' which will
     * be loaded from inside the jar.
     * @param namespace
     * @param language
     */
    public static void registerLanguageNamespace(String namespace, FileConfiguration language){
        try {
            String fileName = namespace.equals("gamebox") ? "language/lang_en.yml" : "language/" + namespace + "/lang_en.yml";
            defaultLanguageFiles.put(namespace, YamlConfiguration.loadConfiguration(new InputStreamReader(GameBox.class.getResourceAsStream(fileName), "UTF-8")));
        } catch (UnsupportedEncodingException e2) {
            Bukkit.getLogger().warning("Failed to load default language file for namespace: " + namespace);
            e2.printStackTrace();
        }
        if(language != null) {
            languageFiles.put(namespace, language);
        } else {
            languageFiles.put(namespace, defaultLanguageFiles.get(namespace));
        }
    }

    public static void registerLanguageNamespace(Namespace namespace, FileConfiguration language){
        registerLanguageNamespace(namespace.namespace(), language);
    }

    /**
     * Load list messages from the language file
     *
     * If the requested path is not valid for the chosen
     * language file the corresponding list from the default
     * file is returned.
     * ChatColor can be translated here.
     * @param namespace namespace of the message to load
     * @param path path to the message
     * @param color if set, color the loaded message
     * @return message
     */
    public static List<String> getStringList(String namespace, String path, boolean color) {
        List<String> toReturn;

        // load from default file if path is not valid
        if(!languageFiles.get(namespace).isList(path)){
            toReturn = defaultLanguageFiles.get(namespace).getStringList(path);
            if(color){
                for(int i = 0; i<toReturn.size(); i++){
                    toReturn.set(i, ChatColor.translateAlternateColorCodes('&',toReturn.get(i)));
                }
            }
            return toReturn;
        }

        // load from language file
        toReturn = languageFiles.get(namespace).getStringList(path);
        if(color) {
            for (int i = 0; i < toReturn.size(); i++) {
                toReturn.set(i, ChatColor.translateAlternateColorCodes('&', toReturn.get(i)));
            }
        }
        return toReturn;
    }

    public static List<String> getStringList(String namespace, String path){
        return getStringList(namespace, path, true);
    }

    public static List<String> getStringList(String namespacePath, boolean color){
        String[] split = namespacePath.split(":");
        if(split.length != 2) return null;

        return getStringList(split[0], split[1], color);
    }

    public static List<String> getStringList(String namespacePath){
        return getStringList(namespacePath, true);
    }

    /**
     * Get a message from the language file
     *
     * If the requested path is not valid for the
     * configured language file the corresponding
     * message from the default file is returned.
     * ChatColor is translated when reading the message.
     * @param namespace namespace of the message to load
     * @param path path to the message
     * @param color if set, color the loaded message
     * @return message
     */
    public static String getString(String namespace, String path, boolean color) {
        if(!languageFiles.get(namespace).isString(path)){
            if(!color) return defaultLanguageFiles.get(namespace).getString(path);
            return ChatColor.translateAlternateColorCodes('&',defaultLanguageFiles.get(namespace).getString(path));
        }
        if(!color) return languageFiles.get(namespace).getString(path);
        return ChatColor.translateAlternateColorCodes('&',languageFiles.get(namespace).getString(path));
    }

    public static String getString(String namespace, String path){
        return getString(namespace, path, true);
    }

    public static String getString(String namespacePath, boolean color){
        String[] split = namespacePath.split(":");
        if(split.length != 2) return null;

        return getString(split[0], split[1], color);
    }

    public static String getString(String namespacePath){
        return getString(namespacePath, true);
    }

    /**
     * Copy all default language files to the plugin folder
     *
     * This method checks for every .yml in the language folder
     * whether it is already present in the plugins language folder.
     * If not it is copied.
     */
    public static void copyDefaultFiles(){
        URL main = GameBox.class.getResource("GameBox.class");
        try {
            JarURLConnection connection = (JarURLConnection) main.openConnection();

            JarFile jar = new JarFile(connection.getJarFileURL().getFile());
            for (Enumeration list = jar.entries(); list.hasMoreElements(); ) {
                JarEntry entry = (JarEntry) list.nextElement();
                if(!entry.getName().split(File.separator)[0].equals("language")){
                    continue;
                }

                String[] pathParts = entry.getName().split(File.separator);

                if(pathParts.length < 2) continue;
                if(pathParts.length == 2) {
                    if(!pathParts[1].endsWith(".yml")) continue;
                } else if(pathParts.length == 3){
                    if(!pathParts[2].endsWith(".yml")) continue;
                } else {
                    continue;
                }

                Plugin gameBox = Bukkit.getPluginManager().getPlugin("gamebox");
                File file = new File(gameBox.getDataFolder().toString() + File.separatorChar + entry.getName());
                if(!file.exists()){
                    file.getParentFile().mkdirs();
                    gameBox.saveResource(entry.getName(), false);
                }
            }
            jar.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FileConfiguration getLanguage(Namespace namespace){
        return languageFiles.get(namespace.namespace());
    }

    public static FileConfiguration getDefaultLanguage(Namespace namespace){
        return defaultLanguageFiles.get(namespace.namespace());
    }

    public enum Namespace{
        GAMEBOX("gamebox");

        private String namespace;

        Namespace(String namespace){
            this.namespace = namespace;
        }

        public String namespace(){
            return this.namespace;
        }
    }
}
