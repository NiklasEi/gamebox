package me.nikl.gamebox;

import me.nikl.gamebox.util.Module;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nikl on 24.10.17.
 */
public abstract class Language {

    protected GameBox plugin;
    protected Module module;

    protected FileConfiguration defaultLanguage;
    protected FileConfiguration language;

    public String PREFIX = "["+ChatColor.DARK_AQUA+"GameBox"+ChatColor.RESET+"]";
    public String NAME = ChatColor.DARK_AQUA+"GameBox"+ChatColor.RESET;
    public String PLAIN_PREFIX = ChatColor.stripColor(PREFIX);
    public String PLAIN_NAME = ChatColor.stripColor(NAME);

    public Language(GameBox plugin, Module module){
        this.plugin = plugin;
        this.module = module;
    }

    /**
     * Load all messages from the language file
     */
    protected abstract void loadMessages();

    /**
     * Try loading the language file specified in the
     * passed file configuration.
     *
     * The required set option is 'langFile'. Possible options
     * are:
     * 'default'/'default.yml': loads the english language file from inside the jar
     * 'lang_xx.yml': will try to load the given file inside the namespaces language folder
     * @param config
     * @throws FileNotFoundException if specified language file is not found
     * @throws UnsupportedEncodingException
     * @throws IllegalArgumentException if file name is not set or invalid
     */
    protected void getLangFile(FileConfiguration config)
            throws FileNotFoundException, UnsupportedEncodingException,
            IllegalArgumentException {

        // load default language
        try {
            String defaultLangName = module == Module.GAMEBOX ? "language/lang_en.yml" : "language/" + module.moduleID() + "/lang_en.yml";
            defaultLanguage = YamlConfiguration.loadConfiguration(new InputStreamReader(GameBox.class.getResourceAsStream(defaultLangName), "UTF-8"));
        } catch (UnsupportedEncodingException e2) {
            Bukkit.getLogger().warning("Failed to load default language file for namespace: " + module.moduleID());
            e2.printStackTrace();
        }

        String fileName = config.getString("langFile");

        if(fileName != null && (fileName.equalsIgnoreCase("default") || fileName.equalsIgnoreCase("default.yml"))) {
            language = defaultLanguage;
            return;
        }

        if(fileName == null || !fileName.endsWith(".yml")){
            String path = module == Module.GAMEBOX ? "'config.yml'" : "'games" + "/" + module.moduleID() + "/config.yml'";
            Bukkit.getLogger().warning("Language file for " + module.moduleID() + " is not specified or not valid.");
            Bukkit.getLogger().warning("Should be set in " + path + " as 'langFile:'");
            Bukkit.getLogger().warning("Falling back to the default file...");
            language = defaultLanguage;
            return;
        }

        File languageFile = module == Module.GAMEBOX ?
                new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar
                        + fileName)
                :
                new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar
                        + module.moduleID() + File.separatorChar + fileName);

        if(!languageFile.exists()){
            String path = module == Module.GAMEBOX ? "'config.yml'"
                    : "'games" + "/" + module.moduleID() + "/config.yml'";
            Bukkit.getLogger().warning("The in '" + path + "' as 'langFile' configured file does not exist!");
            Bukkit.getLogger().warning("Falling back to the default file...");
            language = defaultLanguage;
            return;
        }

        language = YamlConfiguration.loadConfiguration
                (new InputStreamReader(new FileInputStream(languageFile), "UTF-8"));

        return;
    }

    /**
     * Find all messages that are missing in the language file.
     *
     * This method compares all message keys from the default english file
     * with all set keys in the used language file. All missing keys are
     * collected and returned.
     *
     * @return list of all missing keys
     */
    public List<String> findMissingMessages(){

        List<String> toReturn = new ArrayList<>();

        if(defaultLanguage.equals(language)) return toReturn;

        for(String key : defaultLanguage.getKeys(true)){
            if(defaultLanguage.isString(key)){
                if(!language.isString(key)){
                    // there is a message missing
                    toReturn.add(key);
                }
            } else if (defaultLanguage.isList(key)){
                if(!language.isList(key)){
                    // there is a list missing
                    toReturn.add(key + "    (List)");
                }
            }
        }
        return toReturn;
    }


    /**
     * Load list messages from the language file
     *
     * If the requested path is not valid for the chosen
     * language file the corresponding list from the default
     * file is returned.
     * ChatColor can be translated here.
     * @param path path to the message
     * @param color if set, color the loaded message
     * @return message
     */
    protected List<String> getStringList(String path, boolean color) {
        List<String> toReturn;

        // load from default file if path is not valid
        if(!language.isList(path)){
            toReturn = defaultLanguage.getStringList(path);
            if(color && toReturn != null){
                for(int i = 0; i<toReturn.size(); i++){
                    toReturn.set(i, ChatColor.translateAlternateColorCodes('&',toReturn.get(i)));
                }
            }
            return toReturn;
        }

        // load from language file
        toReturn = language.getStringList(path);
        if(color && toReturn != null) {
            for (int i = 0; i < toReturn.size(); i++) {
                toReturn.set(i, ChatColor.translateAlternateColorCodes('&', toReturn.get(i)));
            }
        }
        return toReturn;
    }

    protected List<String> getStringList(String path){
        return getStringList(path, true);
    }

    /**
     * Get a message from the language file
     *
     * If the requested path is not valid for the
     * configured language file the corresponding
     * message from the default file is returned.
     * ChatColor is translated when reading the message.
     * @param path path to the message
     * @param color if set, color the loaded message
     * @return message
     */
    protected String getString(String path, boolean color) {
        if(!language.isString(path)){
            String toReturn = defaultLanguage.getString(path);
            if(color && toReturn != null){
                return ChatColor.translateAlternateColorCodes('&', defaultLanguage.getString(path));
            }
            return defaultLanguage.getString(path);
        }
        if(!color) return language.getString(path);
        return ChatColor.translateAlternateColorCodes('&',language.getString(path));
    }

    protected String getString(String path){
        return getString(path, true);
    }
}
