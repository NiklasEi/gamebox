package me.nikl.connectfour;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.List;


public class Language {
	private Main plugin;
	private FileConfiguration langFile;
	
	public String PREFIX = "[Connect4]", NAME = "&1Connect4&r";
	public List<String> GAME_HELP;
	private YamlConfiguration defaultLang;
	
	public Language(Main plugin){
		this.plugin = plugin;
		getLangFile();
		PREFIX = getString("prefix");
		NAME = getString("name");


		this.GAME_HELP = getStringList("gameHelp");
	}



	private List<String> getStringList(String path) {
		List<String> toReturn;
		if(!langFile.isList(path)){
			toReturn = defaultLang.getStringList(path);
			for(int i = 0; i<toReturn.size(); i++){
				toReturn.set(i, ChatColor.translateAlternateColorCodes('&',toReturn.get(i)));
			}
			return toReturn;
		}
		toReturn = langFile.getStringList(path);
		for(int i = 0; i<toReturn.size(); i++){
			toReturn.set(i, ChatColor.translateAlternateColorCodes('&',toReturn.get(i)));
		}
		return toReturn;
	}

	private String getString(String path) {
		if(!langFile.isString(path)){
			return ChatColor.translateAlternateColorCodes('&',defaultLang.getString(path));
		}
		return ChatColor.translateAlternateColorCodes('&',langFile.getString(path));
	}

	private void getLangFile() {
		try {
			String fileName = "language/lang_en.yml";
			this.defaultLang =  YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(fileName), "UTF-8"));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
		
		File defaultEn = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "lang_en.yml");
		if(!defaultEn.exists()){
			plugin.saveResource("language" + File.separatorChar + "lang_en.yml", false);
		}
		File defaultDe = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "lang_de.yml");
		if(!defaultDe.exists()){
			plugin.saveResource("language" + File.separatorChar + "lang_de.yml", false);
		}
		if(!plugin.getConfig().isString("langFile")){
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Language file is missing in the config!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Add the following to your config:"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " langFile: 'lang_en.yml'"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Using default language file"));
			this.langFile = defaultLang;
			return;
		}
		String fileName = plugin.getConfig().getString("langFile");
		if(fileName.equalsIgnoreCase("default") || fileName.equalsIgnoreCase("default.yml")){
			this.langFile = defaultLang;
			return;
		}
		File languageFile = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + plugin.getConfig().getString("langFile"));
		if(!languageFile.exists()){
			languageFile.mkdir();
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Language file not found!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Using default language file"));
			this.langFile = defaultLang;
			return;
		}
		try { 
			this.langFile = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(languageFile), "UTF-8"));
		} catch (UnsupportedEncodingException e) { 
			e.printStackTrace(); 
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Error in language file!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Using default language file"));
			this.langFile = defaultLang;
			return;
		} catch (FileNotFoundException e) { 
			e.printStackTrace(); 
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Error in language file!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Using default language file"));
			this.langFile = defaultLang;
			return;
		} 
		int count = 0;
		for(String key : defaultLang.getKeys(true)){
			if(defaultLang.isString(key)){
				if(!this.langFile.isString(key)){// there is a message missing
					if(count == 0){
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*"));
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Missing message(s) in your language file!"));
					}
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " " + key));
					count++;
				}
			} else if (defaultLang.isList(key)){
				if(!this.langFile.isList(key)){// there is a message missing
					if(count == 0){
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*"));
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Missing message(s) in your language file!"));
					}
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " " + key + "     (StringList!)"));
					count++;
				}
			}
		}
		if(count > 0){
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + ""));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Game will use default messages for these paths"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + ""));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Please get an updated language file"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Or add the listed paths by hand"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*"));
		}
		return;
		
	}
	
}

