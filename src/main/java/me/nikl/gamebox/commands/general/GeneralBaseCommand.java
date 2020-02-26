package me.nikl.gamebox.commands.general;

import co.aikar.commands.annotation.PreCommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.utility.Permission;
import org.bukkit.command.CommandSender;

/**
 * @author Niklas Eicker
 */
public class GeneralBaseCommand extends GameBoxBaseCommand {
  public GeneralBaseCommand(GameBox gameBox) {
    super(gameBox);
  }

  @Override
  @PreCommand
  public boolean preCommand(CommandSender sender) {
    GameBox.debug("in GeneralBaseCommand pre command");
    if (!Permission.USE.hasPermission(sender)) {
      sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_NO_PERM);
      return true;
    }
    return false;
  }
}
