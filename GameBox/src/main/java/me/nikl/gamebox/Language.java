package me.nikl.gamebox;

import me.nikl.gamebox.util.LanguageUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

/**
 * Created by nikl on 24.10.17.
 */
public abstract class Language {

    protected GameBox plugin;
    protected LanguageUtil.Namespace namespace;

    public String PREFIX = "["+ChatColor.DARK_AQUA+"GameBox"+ChatColor.RESET+"]";
    public String NAME = ChatColor.DARK_AQUA+"GameBox"+ChatColor.RESET;
    public String PLAIN_PREFIX = ChatColor.stripColor(PREFIX);
    public String PLAIN_NAME = ChatColor.stripColor(NAME);

    public Language(GameBox plugin, LanguageUtil.Namespace namespace){
        this.plugin = plugin;
        this.namespace = namespace;
    }

    /**
     * Load all messages from the language file
     */
    public abstract void loadMessages();

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
    private void getLangFile(FileConfiguration config)
            throws FileNotFoundException, UnsupportedEncodingException,
            IllegalArgumentException {

        FileConfiguration langFile = null;

        String fileName = config.getString("langFile");

        if(fileName != null && (fileName.equalsIgnoreCase("default") || fileName.equalsIgnoreCase("default.yml"))) {
            LanguageUtil.registerLanguageNamespace(namespace, null);
            return;
        }

        if(fileName == null || !fileName.endsWith(".yml")){
            String path = namespace == LanguageUtil.Namespace.GAMEBOX ? "'config.yml'" : "'games" + "/" + namespace.namespace() + "/config.yml'";
            throw new IllegalArgumentException("Filename is not specified or not valid.\n" +
                    "Should be set in " + path + " as 'langFile:'");
        }

        File languageFile = namespace == LanguageUtil.Namespace.GAMEBOX ?
                new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + config.getString("langFile"))
                :
                new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + namespace.namespace() + File.separatorChar + config.getString("langFile"));

        if(languageFile.exists()){
            langFile = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(languageFile), "UTF-8"));
        }
        LanguageUtil.registerLanguageNamespace(namespace, langFile);
        return;
    }
}
