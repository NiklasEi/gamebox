package me.nikl.gamebox.commands.admin;

import co.aikar.commands.annotation.PreCommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.module.GameBoxGame;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.utility.Permission;
import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;

/**
 * @author Niklas Eicker
 */
public class GameAdminCommand extends GameBoxBaseCommand {
  private GameBoxGame module;

  public GameAdminCommand(GameBox gameBox, GameBoxGame module) {
    super(gameBox);
    Validate.notNull(module, "The GameAdminCommand needs a valid module!");
    this.module = module;
  }

  @Override
  @PreCommand
  public boolean preCommand(CommandSender sender) {
    GameBox.debug("in GameAdminCommand pre command");
    if (!Permission.ADMIN_GAME.hasPermission(sender, module)) {
      sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_NO_PERM);
      return true;
    }
    return false;
  }
}
