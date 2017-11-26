package me.nikl.gamebox.commands;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxLanguage;
import me.nikl.gamebox.data.DataBase;
import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.util.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;


/**
 * Created by Niklas on 05.05.2017.
 *
 *
 */
public class AdminCommand implements CommandExecutor {
    private GameBox plugin;
    private GameBoxLanguage lang;


    public AdminCommand(GameBox plugin){
        this.plugin = plugin;
        this.lang = plugin.lang;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!sender.hasPermission(Permission.ADMIN.getPermission())){
            sender.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);
            return true;
        }

        if(args.length == 0) {
            sendHelpMessages(sender);
            return true;
        }

        if(args[0].equalsIgnoreCase("givetoken") || args[0].equalsIgnoreCase("taketoken") || args[0].equalsIgnoreCase("settoken")){
            if(args.length != 3){
                sender.sendMessage(lang.PREFIX + " /gba [givetoken:taketoken:settoken] [player name] [count (integer)]");
                return true;
            }
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
            if(player == null || !player.hasPlayedBefore()){
                sender.sendMessage(lang.PREFIX + ChatColor.RED + " can't find player " + args[1]);
                return true;
            }
            int count;
            try{
                count = Integer.parseInt(args[2]);
            } catch (NumberFormatException exception){
                sender.sendMessage(lang.PREFIX + ChatColor.RED + " last argument has to be an integer!");
                return true;
            }
            if(count < 0){
                sender.sendMessage(lang.PREFIX + ChatColor.RED + " count can't be negative!");
                return true;
            }

            // all arguments are valid

            // handle cached online players
            cachedPlayer:
            if(player.isOnline()){
                GBPlayer gbPlayer = plugin.getPluginManager().getPlayer(player.getUniqueId());
                if(gbPlayer == null){
                    break cachedPlayer;
                }
                switch (args[0].toLowerCase()){
                    case "givetoken":
                        gbPlayer.setTokens(gbPlayer.getTokens() + count);
                        break;

                    case "taketoken":
                        if(gbPlayer.getTokens() >= count){
                            gbPlayer.setTokens(gbPlayer.getTokens() - count);
                        } else {
                            sender.sendMessage(lang.PREFIX + ChatColor.RED + " " + player.getName() + " only has " + gbPlayer.getTokens() + " token!");
                            return true;
                        }
                        break;

                    case "settoken":
                        gbPlayer.setTokens(count);
                        break;

                    default: // can't happen due to the check at the beginning of the command
                        return false;

                }
                sender.sendMessage(lang.PREFIX + lang.CMD_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(gbPlayer.getTokens())));
                return true;
            }

            // handle offline or not cached players
            if(!plugin.getDataBase().isSet(player.getUniqueId().toString())){
                switch (args[0].toLowerCase()){
                    case "givetoken":
                        plugin.getDataBase().set(player.getUniqueId().toString(), DataBase.TOKEN_PATH, count);
                        break;

                    case "taketoken":
                        sender.sendMessage(lang.PREFIX + ChatColor.RED+ " " + player.getName() + " only has " + 0 + " token!");
                        return true;

                    case "settoken":
                        plugin.getDataBase().set(player.getUniqueId().toString(), DataBase.TOKEN_PATH, count);
                        break;

                    default: // can't happen due to the check at the beginning of the command
                        return false;

                }
                sender.sendMessage(lang.PREFIX + lang.CMD_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(count)));
                return true;
            } else {
                int oldCount = plugin.getDataBase().getInt(player.getUniqueId(), DataBase.TOKEN_PATH, 0);
                switch (args[0].toLowerCase()){
                    case "givetoken":
                        plugin.getDataBase().set(player.getUniqueId().toString(), DataBase.TOKEN_PATH, count + oldCount);
                        break;

                    case "taketoken":
                        if(oldCount >= count){
                            plugin.getDataBase().set(player.getUniqueId().toString(), DataBase.TOKEN_PATH, oldCount - count);
                        } else {
                            sender.sendMessage(lang.PREFIX + ChatColor.RED + " " + player.getName() + " only has " + oldCount + " token!");
                            return true;
                        }

                    case "settoken":
                        plugin.getDataBase().set(player.getUniqueId().toString(), DataBase.TOKEN_PATH, count);
                        break;

                    default: // can't happen due to the check at the beginning of the command
                        return false;

                }
                sender.sendMessage(lang.PREFIX + lang.CMD_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(plugin.getDataBase().getInt(player.getUniqueId(), DataBase.TOKEN_PATH, 0))));
                return true;
            }
        } // end of give/take/set token cmd
        else if(args[0].equalsIgnoreCase("token")){
            if(args.length != 2){
                sender.sendMessage(lang.PREFIX + " /gba [token] [player name]");
                return true;
            }
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
            if(player == null || !player.hasPlayedBefore()){
                sender.sendMessage(lang.PREFIX + ChatColor.RED + " can't find player " + args[1]);
                return true;
            }

            // handle cached players
            cachedPlayer:
            if(player.isOnline()){
                GBPlayer gbPlayer = plugin.getPluginManager().getPlayer(player.getUniqueId());
                if(gbPlayer == null){
                    break cachedPlayer;
                }
                sender.sendMessage(lang.PREFIX + lang.CMD_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(gbPlayer.getTokens())));
                return true;
            }

            sender.sendMessage(lang.PREFIX + lang.CMD_TOKEN.replace("%player%", player.getName()).replace("%token%"
                    , String.valueOf(plugin.getDataBase().getInt(player.getUniqueId(), DataBase.TOKEN_PATH, 0))));
            return true;
        } else if(args[0].equalsIgnoreCase("reload")){
            if(plugin.reload()){
                sender.sendMessage(lang.PREFIX + lang.RELOAD_SUCCESS);
                return true;
            } else {
                sender.sendMessage(lang.PREFIX + lang.RELOAD_FAIL);
                Bukkit.getPluginManager().disablePlugin(plugin);
                return true;
            }
        } else if(args[0].equalsIgnoreCase("language")){

            List<String> missingStringKeys = this.lang.findMissingStringMessages();
            List<String> missingListKeys = this.lang.findMissingListMessages();

            if(missingListKeys.isEmpty() && missingStringKeys.isEmpty()){
                Bukkit.getConsoleSender().sendMessage(" No missing messages in your language file:)");
                return true;
            }

            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - + - +");
            Bukkit.getConsoleSender().sendMessage(lang.PREFIX + " Your language file is missing messages!");
            Bukkit.getConsoleSender().sendMessage(lang.PREFIX + " ");
            if(!missingListKeys.isEmpty()){
                Bukkit.getConsoleSender().sendMessage(lang.PREFIX + "  String lists:");
                for(String key : missingListKeys){
                    Bukkit.getConsoleSender().sendMessage(lang.PREFIX + "     " + key);
                }
                Bukkit.getConsoleSender().sendMessage(lang.PREFIX + " ");
            }
            if(!missingStringKeys.isEmpty()){
                Bukkit.getConsoleSender().sendMessage(lang.PREFIX + "  Strings:");
                for(String key : missingStringKeys){
                    Bukkit.getConsoleSender().sendMessage(lang.PREFIX + "     " + key);
                }
                Bukkit.getConsoleSender().sendMessage(lang.PREFIX + " ");
            }
            Bukkit.getConsoleSender().sendMessage(lang.PREFIX + " ");
            Bukkit.getConsoleSender().sendMessage(lang.PREFIX + " Default messages are used for these keys.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - + - +");
            return true;
        }
        sendHelpMessages(sender);
        return true;
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
}
