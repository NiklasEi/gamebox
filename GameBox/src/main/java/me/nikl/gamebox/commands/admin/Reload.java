package me.nikl.gamebox.commands.admin;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.utility.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * @author Niklas Eicker
 */
@CommandAlias("%adminCommand")
public class Reload extends GameBoxBaseCommand {
    public Reload(GameBox gameBox) {
        super(gameBox);
    }

    @Subcommand("reload")
    public void onReload(CommandSender sender) {
        if (!Permission.ADMIN_RELOAD.hasPermission(sender)) {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_NO_PERM);
            return;
        }
        sender.sendMessage(gameBox.lang.PREFIX + ChatColor.GREEN + " Reloading...");
        if (gameBox.reload()) {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.RELOAD_SUCCESS);
        } else {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.RELOAD_FAIL);
            Bukkit.getPluginManager().disablePlugin(gameBox);
        }
    }
}
