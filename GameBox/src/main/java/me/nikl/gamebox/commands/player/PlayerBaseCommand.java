package me.nikl.gamebox.commands.player;

import co.aikar.commands.annotation.PreCommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.utility.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Niklas Eicker
 */
public class PlayerBaseCommand extends GameBoxBaseCommand {
    public PlayerBaseCommand(GameBox gameBox) {
        super(gameBox);
    }

    @Override
    @PreCommand
    public boolean preCommand(CommandSender sender) {
        GameBox.debug("in PlayerBaseCommand pre command");
        if (!(sender instanceof Player)) {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_ONLY_PLAYER);
            return true;
        }
        if (!Permission.USE.hasPermission(sender)) {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_NO_PERM);
            return true;
        }
        Player player = (Player) sender;
        if (gameBox.getPluginManager().getBlockedWorlds().contains(player.getLocation().getWorld().getName())) {
            player.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_DISABLED_WORLD);
            return true;
        }
        return false;
    }
}
