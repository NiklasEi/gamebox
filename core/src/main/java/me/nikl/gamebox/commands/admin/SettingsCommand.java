package me.nikl.gamebox.commands.admin;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.PreCommand;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Module;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.utility.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * @author Niklas Eicker
 */
@CommandAlias("%adminCommand")
public class SettingsCommand extends GameBoxBaseCommand {
    public SettingsCommand(GameBox gameBox) {
        super(gameBox);
    }

    @Override
    @PreCommand
    public boolean preCommand(CommandSender sender) {
        GameBox.debug("in SettingsCommand pre command");
        if (!Permission.ADMIN_SETTINGS.hasPermission(sender)) {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_NO_PERM);
            return true;
        }
        return false;
    }

    @Subcommand("settings help")
    public void onHelp(CommandSender sender) {
        sender.sendMessage(gameBox.lang.PREFIX + " Settings help:");
        sendHelp(sender, "<gameID>");
    }

    @Subcommand("settings game")
    public void gameSettings(CommandSender sender) {
        sender.sendMessage(gameBox.lang.PREFIX + " options:");
        sendHelp(sender, "<gameID>");
    }

    @Subcommand("settings game")
    @CommandCompletion("@gameIDs")
    public void gameSettings(CommandSender sender, String gameID, String[] args) {
        if (args == null || args.length == 0) {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_SETTINGS_GAME_INVALID_SETTING);
            sendHelp(sender, gameID);
            return;
        }
        switch (args[0].toLowerCase()) {
            case "enable":
                enableGame(sender, gameID);
                return;
            case "disable":
                disableGame(sender, gameID);
                return;
            default:
                sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_SETTINGS_GAME_INVALID_SETTING);
                sendHelp(sender, gameID);
                return;
        }
    }

    private void sendHelp(CommandSender sender, String gameID) {
        sender.sendMessage(gameBox.lang.PREFIX + "    /gba settings game " + gameID + " enable");
        sender.sendMessage(gameBox.lang.PREFIX + "    /gba settings game " + gameID + " disable");
    }

    private void disableGame(CommandSender sender, String gameID) {
        Module module = gameBox.getGameRegistry().getModule(gameID);
        if (module == null) {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_SETTINGS_GAME_DISABLE_FAIL);
            return;
        }
        gameBox.getGameRegistry().disableGame(gameID);
        sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_SETTINGS_GAME_DISABLE_SUCCESS);
        sender.sendMessage(gameBox.lang.PREFIX + ChatColor.GREEN + " Reloading...");
        if (gameBox.reload()) {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.RELOAD_SUCCESS);
        } else {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.RELOAD_FAIL);
            Bukkit.getPluginManager().disablePlugin(gameBox);
        }
    }

    private void enableGame(CommandSender sender, String gameID) {
        if (!gameBox.getGameRegistry().isDisabledModule(gameID)) {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_SETTINGS_GAME_ENABLE_FAIL);
            return;
        }
        gameBox.getGameRegistry().enableGame(gameID);
        sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_SETTINGS_GAME_ENABLE_SUCCESS);
        sender.sendMessage(gameBox.lang.PREFIX + ChatColor.GREEN + " Reloading...");
        if (gameBox.reload()) {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.RELOAD_SUCCESS);
        } else {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.RELOAD_FAIL);
            Bukkit.getPluginManager().disablePlugin(gameBox);
        }
    }
}
