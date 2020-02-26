package me.nikl.gamebox.commands.general;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.utility.Permission;
import org.bukkit.command.CommandSender;

/**
 * @author Niklas Eicker
 */
@CommandAlias("%mainCommand")
public class HelpCommand extends GeneralBaseCommand {
  public HelpCommand(GameBox gameBox) {
    super(gameBox);
  }

  @Subcommand("help|?")
  public void onHelp(CommandSender sender) {
    if (!Permission.CMD_HELP.hasPermission(sender)) {
      sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_NO_PERM);
      return;
    }
    for (String message : gameBox.lang.CMD_HELP) {
      sender.sendMessage(gameBox.lang.PREFIX + message);
    }
  }
}
