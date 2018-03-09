package me.nikl.gamebox.commands.admin;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Language;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author Niklas Eicker
 */
@CommandAlias("%adminCommand")
public class LanguageCommands extends GameBoxBaseCommand {
    private HashMap<String, HashMap<String, List<String>>> missingLanguageKeys = new HashMap<>();

    public LanguageCommands(GameBox gameBox) {
        super(gameBox);
        new BukkitRunnable() {
            @Override
            public void run() {
                GameBox.debug(" running late check in Admin command");
                checkLanguageFiles();
            }
        }.runTask(gameBox);
    }

    @Subcommand("language")
    public void onLanguageCommand(CommandSender sender) {
        if (sender instanceof Player) {
            sender.sendMessage(gameBox.lang.PREFIX + " Only from the console!");
            return;
        }
        if (missingLanguageKeys.isEmpty()) {
            gameBox.info(ChatColor.GREEN + " You have no missing messages in your language files :)");
        } else {
            printIncompleteLangFilesInfo();
        }
    }

    @Subcommand("language all")
    public void onLanguageAllCommand(CommandSender sender) {
        if (sender instanceof Player) {
            sender.sendMessage(gameBox.lang.PREFIX + " Only from the console!");
            return;
        }
        printMissingKeys();
    }

    @Subcommand("language")
    public void onLanguageCommand(CommandSender sender, @Single String moduleID) {
        if (sender instanceof Player) {
            sender.sendMessage(gameBox.lang.PREFIX + " Only from the console!");
            return;
        }
        if (missingLanguageKeys.get(moduleID.toLowerCase()) == null) {
            gameBox.info(" Module '" + moduleID.toLowerCase() + "' does not exist or has no missing keys.");
            gameBox.info(" Valid options: " + String.join(", ", missingLanguageKeys.keySet()));
            return;
        }
        gameBox.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - +");
        printMissingModuleKeys(moduleID.toLowerCase());
        gameBox.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - +");
    }

    private void printMissingKeys() {
        if (missingLanguageKeys.isEmpty()) return;
        gameBox.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - +");
        Iterator<String> iterator = missingLanguageKeys.keySet().iterator();
        String moduleID;
        while (iterator.hasNext()) {
            moduleID = iterator.next();
            printMissingModuleKeys(moduleID);
            if (iterator.hasNext()) {
                gameBox.info(" ");
                gameBox.info(" ");
            } else {
                gameBox.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - +");
            }
        }
    }

    private void printMissingModuleKeys(String moduleID) {
        HashMap<String, List<String>> currentKeys = missingLanguageKeys.get(moduleID);
        List<String> keys;
        gameBox.info(" Missing from " + ChatColor.BLUE + gameBox.getLanguage(moduleID).DEFAULT_PLAIN_NAME
                + ChatColor.RESET + " language file:");
        if (currentKeys.keySet().contains("string")) {
            gameBox.info(" ");
            gameBox.info(ChatColor.BOLD + "   Strings:");
            keys = currentKeys.get("string");
            for (String key : keys) {
                gameBox.info(ChatColor.RED + "   -> " + ChatColor.RESET + key);
            }
        }
        if (currentKeys.keySet().contains("list")) {
            gameBox.info(" ");
            gameBox.info(ChatColor.BOLD + "   Lists:");
            keys = currentKeys.get("list");
            for (String key : keys) {
                gameBox.info(ChatColor.RED + "   -> " + ChatColor.RESET + key);
            }
        }
    }

    private void checkLanguageFiles() {
        HashMap<String, List<String>> currentKeys;
        for (String moduleID : gameBox.getGameRegistry().getModuleIDs()) {
            currentKeys = collectMissingKeys(moduleID);
            if (!currentKeys.isEmpty()) {
                missingLanguageKeys.put(moduleID, currentKeys);
            }
        }
    }

    private HashMap<String, List<String>> collectMissingKeys(String moduleID) {
        Language language = gameBox.getLanguage(moduleID);
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

    public void printIncompleteLangFilesInfo() {
        if (missingLanguageKeys == null) checkLanguageFiles();
        if (missingLanguageKeys.isEmpty()) return;
        gameBox.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - +");
        gameBox.info(ChatColor.BOLD + " There are missing keys in the following module(s):");
        gameBox.info("");
        Iterator<String> iterator = missingLanguageKeys.keySet().iterator();
        String moduleID;
        while (iterator.hasNext()) {
            moduleID = iterator.next();
            gameBox.info(ChatColor.RED + "   -> " + ChatColor.RESET + gameBox.getLanguage(moduleID).DEFAULT_PLAIN_NAME);
        }
        gameBox.info("");
        gameBox.info(" To get the specific missing keys of one module run ");
        gameBox.info("      " + ChatColor.BLUE + "/gba language <module name>");
        gameBox.info(" To get the specific missing keys of all files run ");
        gameBox.info("      " + ChatColor.BLUE + "/gba language all");
        gameBox.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - +");
    }
}
