package me.nikl.gamebox.commands.admin;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.commands.ConsoleBaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Niklas Eicker
 */
@CommandAlias("%adminCommand")
public class ToggleDebug extends ConsoleBaseCommand {
    public ToggleDebug(GameBox gameBox) {
        super(gameBox);
    }

    @Subcommand("debug")
    public void toggleDebug(CommandSender sender) {
        if (sender instanceof Player) {
            sender.sendMessage(gameBox.lang.PREFIX + " Only as console...");
            return;
        }
        GameBox.debug = !GameBox.debug;
        sender.sendMessage(gameBox.lang.PREFIX + " Set debug mode to: " + GameBox.debug);
    }
}
