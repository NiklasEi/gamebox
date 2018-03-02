package me.nikl.gamebox.commands;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxLanguage;
import me.nikl.gamebox.Language;
import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.data.database.DataBase;
import me.nikl.gamebox.data.database.MysqlDB;
import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.utility.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;


/**
 * @author Niklas Eicker
 *
 *         Commands for server administrators
 */
public class AdminCommand implements CommandExecutor {
    private GameBox plugin;
    private GameBoxLanguage lang;
    private HashMap<String, HashMap<String, List<String>>> missingLanguageKeys;

    public AdminCommand(GameBox plugin) {
        this.plugin = plugin;
        this.lang = plugin.lang;
        missingLanguageKeys = new HashMap<>();
        new BukkitRunnable() {
            @Override
            public void run() {
                GameBox.debug(" running late check in Admin command");
                checkLanguageFiles();
            }
        }.runTask(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!Permission.ADMIN.hasPermission(sender)) {
            sender.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);
            return true;
        }
        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
            sendHelpMessages(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("givetoken") || args[0].equalsIgnoreCase("taketoken") || args[0].equalsIgnoreCase("settoken")) {
            return manipulateTokenCommand(sender, args);
        } else if (args[0].equalsIgnoreCase("token")) {
            return getTokenCountCommand(sender, args);
        } else if (args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(lang.PREFIX + ChatColor.GREEN + " Reloading...");
            if (plugin.reload()) {
                sender.sendMessage(lang.PREFIX + lang.RELOAD_SUCCESS);
                return true;
            } else {
                sender.sendMessage(lang.PREFIX + lang.RELOAD_FAIL);
                Bukkit.getPluginManager().disablePlugin(plugin);
                return true;
            }
        } else if (args[0].equalsIgnoreCase("language")) {
            return languageCommand(sender, args);
        } else if (args[0].equalsIgnoreCase("debug")) {
            if (sender instanceof Player) {
                return true;
            }
            GameBox.debug = !GameBox.debug;
            sender.sendMessage(lang.PREFIX + " Set debug mode to: " + GameBox.debug);
            return true;
        } else if (args[0].equalsIgnoreCase("resetHighScores")) {
            plugin.getDataBase().resetHighScores();
            plugin.reload();
            return true;
        } else if (args[0].equalsIgnoreCase("filetomysql")) {
            DataBase dataBase = plugin.getDataBase();
            if (!(dataBase instanceof MysqlDB)) {
                sender.sendMessage(lang.PREFIX + ChatColor.RED + " You must have MySQL enabled to do this!");
                return true;
            }
            ((MysqlDB) dataBase).convertFromFile(sender);
            return true;
        } else if (args[0].equalsIgnoreCase("hundredrandom")) {
            DataBase dataBase = plugin.getDataBase();
            Random random = new Random();
            for (int i = 0; i < 100; i++) {
                UUID uuid = UUID.randomUUID();
                dataBase.savePlayer(new GBPlayer(plugin, uuid, 0, true, true), true);
                dataBase.addStatistics(uuid, GameBox.MODULE_COOKIECLICKER, "weekly", random.nextInt(5000), SaveType.HIGH_NUMBER_SCORE);
            }
            return true;
        } else if (args[0].equalsIgnoreCase("findcolumns")) {
            if (args.length < 2) return true;
            DataBase dataBase = plugin.getDataBase();
            if (!(dataBase instanceof MysqlDB)) return true;
            List<String> columns = ((MysqlDB) dataBase).getHighScoreColumnsBeginningWith(args[1]);
            sender.sendMessage(String.join(", ", columns));
            return true;
        } else if (args[0].equalsIgnoreCase("resetstats")) {
            if (args.length < 5) return true;
            DataBase dataBase = plugin.getDataBase();
            try {
                SaveType saveType = SaveType.valueOf(args[3]);
                dataBase.resetHighScores(args[1], args[2], saveType);
                sender.sendMessage( " High score reset successful");
            } catch (IllegalArgumentException exception) {
                sender.sendMessage("Valid saveTyps: " + SaveType.values().toString());
            }
            return true;
        }
        sendHelpMessages(sender);
        return true;
    }

    private boolean languageCommand(CommandSender sender, String[] args) {
        // args length is min. 1
        if (args.length == 1) {
            if (missingLanguageKeys.isEmpty()) {
                plugin.info(ChatColor.GREEN + " You have no missing messages in your language files :)");
                return true;
            } else {
                printIncompleteLangFilesInfo();
                return true;
            }
        } else if (args.length == 2) {
            if (args[1].equalsIgnoreCase("all")) {
                printMissingKeys();
                return true;
            }
            if (missingLanguageKeys.get(args[1].toLowerCase()) == null) {
                plugin.info(" Module '" + args[1].toLowerCase() + "' does not exist or has no missing keys.");
                return true;
            }
            plugin.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - +");
            printMissingModuleKeys(args[1].toLowerCase());
            plugin.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - +");
            return true;
        }
        sendHelpMessages(sender);
        return true;
    }

    private boolean getTokenCountCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(lang.PREFIX + " /gba token [player name]");
            return true;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
        if (player == null || !player.hasPlayedBefore()) {
            sender.sendMessage(lang.PREFIX + ChatColor.RED + " can't find player " + args[1]);
            return true;
        }

        // handle cached players
        cachedPlayer:
        if (player.isOnline()) {
            GBPlayer gbPlayer = plugin.getPluginManager().getPlayer(player.getUniqueId());
            if (gbPlayer == null) {
                break cachedPlayer;
            }
            sender.sendMessage(lang.PREFIX + lang.CMD_TOKEN_INFO.replace("%player%", player.getName()).replace("%token%", String.valueOf(gbPlayer.getTokens())));
            return true;
        }

        plugin.getDataBase().getToken(player.getUniqueId(), new DataBase.Callback<Integer>() {
            @Override
            public void onSuccess(Integer done) {
                sender.sendMessage(lang.PREFIX + lang.CMD_TOKEN_INFO
                        .replace("%player%", player.getName())
                        .replace("%token%", String.valueOf(done)));
            }

            @Override
            public void onFailure(@Nullable Throwable throwable, @Nullable Integer value) {
                sender.sendMessage(lang.PREFIX + " Failed to get token for player: " + player.getName());
                if (throwable != null) throwable.printStackTrace();
            }
        });
        return true;
    }

    private boolean manipulateTokenCommand(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(lang.PREFIX + " /gba [givetoken:taketoken:settoken] [player name] [count (integer)]");
            return true;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
        if (player == null || !player.hasPlayedBefore()) {
            sender.sendMessage(lang.PREFIX + ChatColor.RED + " can't find player " + args[1]);
            return true;
        }
        int count;
        try {
            count = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(lang.PREFIX + ChatColor.RED + " last argument has to be an integer!");
            return true;
        }
        if (count < 0) {
            sender.sendMessage(lang.PREFIX + ChatColor.RED + " the number can't be negative!");
            return true;
        }

        // all arguments are valid

        // handle cached online players
        GBPlayer gbPlayer = plugin.getPluginManager().getPlayer(player.getUniqueId());
        if (gbPlayer != null && gbPlayer.isLoaded()) {
            switch (args[0].toLowerCase()) {
                case "givetoken":
                    gbPlayer.setTokens(gbPlayer.getTokens() + count);
                    sender.sendMessage(lang.PREFIX + lang.CMD_GAVE_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(count)));
                    return true;

                case "taketoken":
                    if (gbPlayer.getTokens() >= count) {
                        gbPlayer.setTokens(gbPlayer.getTokens() - count);
                        sender.sendMessage(lang.PREFIX + lang.CMD_TOOK_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(count)));
                        return true;
                    } else {
                        sender.sendMessage(lang.PREFIX + lang.CMD_NOT_ENOUGH_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(gbPlayer.getTokens())));
                        return true;
                    }

                case "settoken":
                    gbPlayer.setTokens(count);
                    sender.sendMessage(lang.PREFIX + lang.CMD_SET_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(count)));
                    return true;

                default: // can't happen due to the check at the beginning of the command
                    return false;

            }
        }

        // handle offline or not cached players
        switch (args[0].toLowerCase()) {
            case "givetoken":
                plugin.getApi().giveToken(player, count);
                sender.sendMessage(lang.PREFIX + lang.CMD_GAVE_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(count)));
                return true;

            case "taketoken":
                plugin.getApi().takeToken(player, count, new DataBase.Callback<Integer>() {
                    @Override
                    public void onSuccess(Integer done) {
                        sender.sendMessage(lang.PREFIX + lang.CMD_TOOK_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(count)));
                    }

                    @Override
                    public void onFailure(@Nullable Throwable throwable, @Nullable Integer value) {
                        if (value != null) {
                            sender.sendMessage(lang.PREFIX + lang.CMD_NOT_ENOUGH_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(value)));
                        } else {
                            sender.sendMessage(lang.PREFIX + " Error...");
                            if (throwable != null) {
                                throwable.printStackTrace();
                            }
                        }
                    }
                });
                return true;

            case "settoken":
                plugin.getDataBase().setToken(player.getUniqueId(), count);
                sender.sendMessage(lang.PREFIX + lang.CMD_SET_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(count)));
                return true;

            default: // can't happen due to the check at the beginning of the command
                return false;
        }
    }

    public void printIncompleteLangFilesInfo() {
        if (missingLanguageKeys.isEmpty()) return;
        plugin.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - +");
        plugin.info(ChatColor.BOLD + " There are missing keys in the following module(s):");
        plugin.info("");
        Iterator<String> iterator = missingLanguageKeys.keySet().iterator();
        String moduleID;
        while (iterator.hasNext()) {
            moduleID = iterator.next();
            plugin.info(ChatColor.RED + "   -> " + ChatColor.RESET + plugin.getLanguage(moduleID).DEFAULT_PLAIN_NAME);
        }
        plugin.info("");
        plugin.info(" To get the specific missing keys of one module run ");
        plugin.info("      " + ChatColor.BLUE + "/gba language <" + String.join(":", missingLanguageKeys.keySet()) + ">");
        plugin.info(" To get the specific missing keys of all files run ");
        plugin.info("      " + ChatColor.BLUE + "/gba language all");
        plugin.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - +");
    }

    private void sendHelpMessages(CommandSender sender) {
        plugin.info(ChatColor.BOLD.toString() + ChatColor.GOLD + " Change the number of tokens for online/offline players");
        plugin.info(ChatColor.DARK_GREEN + " /gba [givetoken:taketoken:settoken] [player name] [count (integer)]");
        plugin.info(ChatColor.BOLD.toString() + ChatColor.GOLD + " Get the number of tokens an online/offline player has");
        plugin.info(ChatColor.DARK_GREEN + " /gba [token] [player name]");
        plugin.info(ChatColor.BOLD.toString() + ChatColor.GOLD + " Reload Gamebox and all registered games");
        plugin.info(ChatColor.DARK_GREEN + " /gba reload");
        plugin.info(ChatColor.BOLD.toString() + ChatColor.GOLD + " Display information about your used language file");
        plugin.info(ChatColor.DARK_GREEN + " /gba language");
    }

    private void printMissingKeys() {
        if (missingLanguageKeys.isEmpty()) return;
        plugin.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - +");
        Iterator<String> iterator = missingLanguageKeys.keySet().iterator();
        String moduleID;
        while (iterator.hasNext()) {
            moduleID = iterator.next();
            printMissingModuleKeys(moduleID);
            if (iterator.hasNext()) {
                plugin.info(" ");
                plugin.info(" ");
            } else {
                plugin.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - +");
            }
        }
    }

    private void printMissingModuleKeys(String moduleID) {
        HashMap<String, List<String>> currentKeys = missingLanguageKeys.get(moduleID);
        List<String> keys;
        plugin.info(" Missing from " + ChatColor.BLUE + plugin.getLanguage(moduleID).DEFAULT_PLAIN_NAME
                + ChatColor.RESET + " language file:");
        if (currentKeys.keySet().contains("string")) {
            plugin.info(" ");
            plugin.info(ChatColor.BOLD + "   Strings:");
            keys = currentKeys.get("string");
            for (String key : keys) {
                plugin.info(ChatColor.RED + "   -> " + ChatColor.RESET + key);
            }
        }
        if (currentKeys.keySet().contains("list")) {
            plugin.info(" ");
            plugin.info(ChatColor.BOLD + "   Lists:");
            keys = currentKeys.get("list");
            for (String key : keys) {
                plugin.info(ChatColor.RED + "   -> " + ChatColor.RESET + key);
            }
        }
    }


    private void checkLanguageFiles() {
        HashMap<String, List<String>> currentKeys;
        for (String moduleID : plugin.getGameRegistry().getModuleIDs()) {
            currentKeys = collectMissingKeys(moduleID);
            if (!currentKeys.isEmpty()) {
                missingLanguageKeys.put(moduleID, currentKeys);
            }
        }
    }

    private HashMap<String, List<String>> collectMissingKeys(String moduleID) {
        Language language = plugin.getLanguage(moduleID);
        List<String> missingStringKeys = language.findMissingStringMessages();
        List<String> missingListKeys = language.findMissingListMessages();
        HashMap<String, List<String>> toReturn = new HashMap<>();
        if (!missingListKeys.isEmpty()) {
            toReturn.put("list", missingListKeys);
        }
        if (!missingStringKeys.isEmpty()) {
            toReturn.put("string", missingStringKeys);
        }
        return toReturn;
    }
}
