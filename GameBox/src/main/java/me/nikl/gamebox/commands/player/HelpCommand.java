package me.nikl.gamebox.commands.player;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.utility.Permission;
import org.bukkit.command.CommandSender;

/**
 * @author Niklas Eicker
 */
@CommandAlias("%mainCommand")
public class HelpCommand extends GameBoxBaseCommand {
    public HelpCommand(GameBox gameBox) {
        super(gameBox);
    }

    @Subcommand("help")
    @CommandPermission("%helpPermission")
    public void onHelp(CommandSender sender) {
        for (String message : gameBox.lang.CMD_HELP) {
            sender.sendMessage(gameBox.lang.PREFIX + message);
        }
    }
}
