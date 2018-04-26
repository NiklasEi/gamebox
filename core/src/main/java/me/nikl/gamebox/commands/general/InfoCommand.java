package me.nikl.gamebox.commands.general;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.game.Game;
import me.nikl.gamebox.utility.Permission;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Set;

/**
 * @author Niklas Eicker
 */
@CommandAlias("%mainCommand")
public class InfoCommand extends GeneralBaseCommand {
    public InfoCommand(GameBox gameBox) {
        super(gameBox);
    }

    @Subcommand("info|information")
    public void onInfo(CommandSender sender) {
        if (!Permission.CMD_INFO.hasPermission(sender)) {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_NO_PERM);
            return;
        }
        for (String message : gameBox.lang.CMD_INFO_HEADER) {
            sender.sendMessage(gameBox.lang.PREFIX + message);
        }
        String allSubCommands;
        Game game;
        for (String gameID : gameBox.getGameRegistry().getModuleIDs()) {
            game = gameBox.getPluginManager().getGame(gameID);
            if (game == null) continue;

            // get subcommands
            Set<String> subCmds = gameBox.getGameRegistry().getModuleSubCommands(game.getModule());
            if (subCmds != null && !subCmds.isEmpty()) {
                allSubCommands = String.join(":", subCmds);
            } else {
                allSubCommands = " ";
            }
            for (String message : gameBox.lang.CMD_INFO_PER_GAME) {
                sender.sendMessage(gameBox.lang.PREFIX + message
                        .replace("%name%", ChatColor.stripColor(game.getGameLang().PLAIN_NAME))
                        .replace("%shorts%", allSubCommands)
                        .replace("%playerNum%", String.valueOf(game.getSettings().getGameType().getPlayerNumber())));
            }
        }
        for (String message : gameBox.lang.CMD_INFO_FOOTER) {
            sender.sendMessage(gameBox.lang.PREFIX + message);
        }
    }
}
