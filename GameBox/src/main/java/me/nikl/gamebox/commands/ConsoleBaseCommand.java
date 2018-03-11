package me.nikl.gamebox.commands;

import co.aikar.commands.annotation.PreCommand;
import me.nikl.gamebox.GameBox;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Niklas Eicker
 */
public class ConsoleBaseCommand extends GameBoxBaseCommand {
    public ConsoleBaseCommand(GameBox gameBox) {
        super(gameBox);
    }

    @Override
    @PreCommand
    public boolean preCommand(CommandSender sender) {
        GameBox.debug("in ConsoleBaseCommand pre command");
        if (sender instanceof Player) {
            sender.sendMessage(gameBox.lang.PREFIX + " Only from the console!");
            return true;
        }
        return false;
    }
}
