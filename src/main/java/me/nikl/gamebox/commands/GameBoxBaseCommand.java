package me.nikl.gamebox.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.PreCommand;
import me.nikl.gamebox.GameBox;
import org.bukkit.command.CommandSender;

/**
 * @author Niklas Eicker
 */
public abstract class GameBoxBaseCommand extends BaseCommand {
    protected GameBox gameBox;

    public GameBoxBaseCommand(GameBox gameBox) {
        this.gameBox = gameBox;
    }

    @PreCommand
    public boolean preCommand(CommandSender sender) {
        GameBox.debug("in GameBoxBaseCommand pre command");
        // gets called before any command
        // stop it via return true
        return false;
    }
}
