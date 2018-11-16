package me.nikl.gamebox.commands.admin;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.PreCommand;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.data.database.DataBase;
import me.nikl.gamebox.utility.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;

/**
 * @author Niklas Eicker
 */
@CommandAlias("%adminCommand")
public class TokenCommands extends GameBoxBaseCommand {
    public TokenCommands(GameBox gameBox) {
        super(gameBox);
    }

    @Override
    @PreCommand
    public boolean preCommand(CommandSender sender) {
        GameBox.debug("in TokenCommands pre command");
        if (!Permission.ADMIN_TOKEN.hasPermission(sender)) {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_NO_PERM);
            return true;
        }
        return false;
    }

    @Subcommand("token")
    @CommandCompletion("@players")
    public void getToken(CommandSender sender, @Single String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player == null || !player.hasPlayedBefore()) {
            sender.sendMessage(gameBox.lang.PREFIX + ChatColor.RED + " can't find player " + playerName);
            return;
        }
        cachedPlayer:
        if (player.isOnline()) {
            GBPlayer gbPlayer = gameBox.getPluginManager().getPlayer(player.getUniqueId());
            if (gbPlayer == null) {
                break cachedPlayer;
            }
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_TOKEN_INFO.replace("%player%", player.getName()).replace("%token%", String.valueOf(gbPlayer.getTokens())));
            return;
        }
        gameBox.getDataBase().getToken(player.getUniqueId(), new DataBase.Callback<Integer>() {
            @Override
            public void onSuccess(Integer done) {
                sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_TOKEN_INFO
                        .replace("%player%", player.getName())
                        .replace("%token%", String.valueOf(done)));
            }

            @Override
            public void onFailure(@Nullable Throwable throwable, @Nullable Integer value) {
                sender.sendMessage(gameBox.lang.PREFIX + " Failed to get token for player: " + player.getName());
                if (throwable != null) throwable.printStackTrace();
            }
        });
    }

    @Subcommand("token give|g")
    @CommandCompletion("@players 1|5|10|25|50")
    public void giveToken(CommandSender sender, String playerName, int token) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player == null || !player.hasPlayedBefore()) {
            sender.sendMessage(gameBox.lang.PREFIX + ChatColor.RED + " can't find player " + playerName);
            return;
        }
        GBPlayer gbPlayer = gameBox.getPluginManager().getPlayer(player.getUniqueId());
        if (gbPlayer != null && gbPlayer.isLoaded()) {
            gbPlayer.setTokens(gbPlayer.getTokens() + token);
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_GAVE_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(token)));
            return;
        }
        gameBox.getApi().giveToken(player, token);
        sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_GAVE_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(token)));
    }

    @Subcommand("token take|t")
    @CommandCompletion("@players 1|5|10|25|50")
    public void tokenToken(CommandSender sender, String playerName, int token) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player == null || !player.hasPlayedBefore()) {
            sender.sendMessage(gameBox.lang.PREFIX + ChatColor.RED + " can't find player " + playerName);
            return;
        }
        GBPlayer gbPlayer = gameBox.getPluginManager().getPlayer(player.getUniqueId());
        if (gbPlayer != null && gbPlayer.isLoaded()) {
            if (gbPlayer.getTokens() >= token) {
                gbPlayer.setTokens(gbPlayer.getTokens() - token);
                sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_TOOK_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(token)));
                return;
            } else {
                sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_NOT_ENOUGH_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(gbPlayer.getTokens())));
                return;
            }
        }
        gameBox.getApi().takeToken(player, token, new DataBase.Callback<Integer>() {
            @Override
            public void onSuccess(Integer done) {
                sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_TOOK_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(token)));
            }

            @Override
            public void onFailure(@Nullable Throwable throwable, @Nullable Integer value) {
                if (value != null) {
                    sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_NOT_ENOUGH_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(value)));
                } else {
                    sender.sendMessage(gameBox.lang.PREFIX + " Error...");
                    if (throwable != null) {
                        throwable.printStackTrace();
                    }
                }
            }
        });
    }

    @Subcommand("token set|s")
    @CommandCompletion("@players 1|5|10|25|50")
    public void setToken(CommandSender sender, String playerName, int token) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player == null || !player.hasPlayedBefore()) {
            sender.sendMessage(gameBox.lang.PREFIX + ChatColor.RED + " can't find player " + playerName);
            return;
        }
        GBPlayer gbPlayer = gameBox.getPluginManager().getPlayer(player.getUniqueId());
        if (gbPlayer != null && gbPlayer.isLoaded()) {
            gbPlayer.setTokens(token);
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_SET_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(token)));
            return;
        }
        gameBox.getDataBase().setToken(player.getUniqueId(), token);
        sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_SET_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(token)));
    }
}
