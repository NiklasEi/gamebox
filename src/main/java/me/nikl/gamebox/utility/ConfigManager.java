package me.nikl.gamebox.utility;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Language;
import me.nikl.gamebox.module.GameBoxGame;
import me.nikl.gamebox.game.GameLanguage;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Niklas Eicker
 */
public class ConfigManager {
  private static final Map<String, FileConfiguration> gameFileConfigurations = new HashMap<>();
  private static final Map<String, Language> gameLanguages = new HashMap<>();
  private static HashMap<String, HashMap<String, List<String>>> missingLanguageKeys;

  public static void registerGameConfiguration(GameBoxGame game, FileConfiguration configuration) {
    registerGameConfiguration(game.getGameId(), configuration);
  }

  public static void registerGameConfiguration(String gameId, FileConfiguration configuration) {
    gameFileConfigurations.put(gameId, configuration);
  }

  public static <T extends Language> void registerGameLanguage(GameBoxGame game, T language) {
    registerGameLanguage(game.getGameId(), language);
  }

  public static <T extends Language> void registerGameLanguage(String gameId, T language) {
    gameLanguages.put(gameId, language);
  }

  public static Language getLanguage(GameBoxGame module) {
    return getLanguage(module.getGameId());
  }

  public static Language getLanguage(String moduleID) {
    return gameLanguages.get(moduleID);
  }

  public static GameLanguage getGameLanguage(GameBoxGame module) {
    return getGameLanguage(module.getGameId());
  }

  public static GameLanguage getGameLanguage(String moduleID) {
    Language language = gameLanguages.get(moduleID);
    if (!(language instanceof GameLanguage))
      throw new IllegalArgumentException("Requested game language for '" + moduleID + "' cannot be found");
    return (GameLanguage) language;
  }

  public static FileConfiguration getConfig(GameBoxGame module) {
    return getConfig(module.getGameId());
  }

  public static FileConfiguration getConfig(String moduleID) {
    if (!gameFileConfigurations.containsKey(moduleID))
      throw new IllegalArgumentException("Configuration for '" + moduleID + "' cannot be found");
    return gameFileConfigurations.get(moduleID);
  }

  public static void clear() {
    gameFileConfigurations.clear();
    gameLanguages.clear();
    missingLanguageKeys = null;
  }

  public static void checkLanguageFiles() {
    missingLanguageKeys = new HashMap<>();
    HashMap<String, List<String>> currentKeys;
    for (String moduleID : gameLanguages.keySet()) {
      currentKeys = collectMissingKeys(moduleID);
      if (!currentKeys.isEmpty()) {
        missingLanguageKeys.put(moduleID, currentKeys);
      }
    }
  }

  private static HashMap<String, List<String>> collectMissingKeys(String moduleID) {
    Language language = gameLanguages.get(moduleID);
    HashMap<String, List<String>> toReturn = new HashMap<>();
    if (language == null) return toReturn;
    List<String> missingStringKeys = language.findMissingStringMessages();
    List<String> missingListKeys = language.findMissingListMessages();
    if (!missingListKeys.isEmpty()) {
      toReturn.put("list", missingListKeys);
    }
    if (!missingStringKeys.isEmpty()) {
      toReturn.put("string", missingStringKeys);
    }
    return toReturn;
  }

  public static void printMissingKeys(GameBox gameBox) {
    if (missingLanguageKeys == null) checkLanguageFiles();
    if (missingLanguageKeys.isEmpty()) return;
    gameBox.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - +");
    gameBox.info(ChatColor.BOLD + "For the following keys GameBox is using default messages:");
    gameBox.info("");
    Iterator<String> iterator = missingLanguageKeys.keySet().iterator();
    String moduleID;
    while (iterator.hasNext()) {
      moduleID = iterator.next();
      printMissingGameKeys(gameBox, moduleID);
      if (iterator.hasNext()) {
        gameBox.info(" ");
        gameBox.info(" ");
      } else {
        gameBox.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - +");
      }
    }
  }

  public static void printMissingGameKeys(GameBox gameBox, String moduleID) {
    if (missingLanguageKeys == null) checkLanguageFiles();
    HashMap<String, List<String>> currentKeys = missingLanguageKeys.get(moduleID);
    List<String> keys;
    gameBox.info(" Missing from " + ChatColor.BLUE + ConfigManager.getLanguage(moduleID).DEFAULT_PLAIN_NAME
            + ChatColor.RESET + " language file:");
    if (currentKeys.containsKey("string")) {
      gameBox.info(" ");
      gameBox.info(ChatColor.BOLD + "   Strings:");
      keys = currentKeys.get("string");
      for (String key : keys) {
        gameBox.info(ChatColor.RED + "   -> " + ChatColor.RESET + key);
      }
    }
    if (currentKeys.containsKey("list")) {
      gameBox.info(" ");
      gameBox.info(ChatColor.BOLD + "   Lists:");
      keys = currentKeys.get("list");
      for (String key : keys) {
        gameBox.info(ChatColor.RED + "   -> " + ChatColor.RESET + key);
      }
    }
  }

  public static void printIncompleteLangFilesInfo(GameBox gameBox) {
    if (missingLanguageKeys == null) checkLanguageFiles();
    if (missingLanguageKeys.isEmpty()) {
      return;
    }
    gameBox.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - +");
    gameBox.info(ChatColor.BOLD + " Missing messages in the following module(s):");
    gameBox.info("");
    gameBox.info("      Name                module ID");
    Iterator<String> iterator = missingLanguageKeys.keySet().iterator();
    String moduleID;
    String twentySpaces = "                    ";
    while (iterator.hasNext()) {
      moduleID = iterator.next();
      String defaultName = ConfigManager.getLanguage(moduleID).DEFAULT_PLAIN_NAME;
      gameBox.info(ChatColor.RED + "   -> " + ChatColor.RESET + defaultName + twentySpaces.substring(defaultName.length()) + "(" + moduleID + ")");
    }
    gameBox.info("");
    gameBox.info(ChatColor.BOLD + " GameBox uses default messages for missing messages.");
    gameBox.info("");
    gameBox.info(" To get the specific missing keys of one module run ");
    gameBox.info("      " + ChatColor.BLUE + "/gba language <module ID>");
    gameBox.info(" To get the specific missing keys of all files run ");
    gameBox.info("      " + ChatColor.BLUE + "/gba language all");
    gameBox.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - +");
  }

  public static Set<String> getGameIdsWithMissingKeys() {
    if (missingLanguageKeys == null) checkLanguageFiles();
    return missingLanguageKeys.keySet();
  }
}
