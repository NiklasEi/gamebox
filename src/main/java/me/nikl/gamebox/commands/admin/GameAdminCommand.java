package me.nikl.gamebox.commands.admin;

import co.aikar.commands.annotation.PreCommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxModule;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.utility.Permission;
import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;

/**
 * @author Niklas Eicker
 */
public class GameAdminCommand extends GameBoxBaseCommand {
  private GameBoxModule gameBoxModule;

  public GameAdminCommand(GameBox gameBox, GameBoxModule gameBoxModule) {
    super(gameBox);
    Validate.notNull(gameBoxModule, "The GameAdminCommand needs a valid module!");
    this.gameBoxModule = gameBoxModule;
  }

  @Override
  @PreCommand
  public boolean preCommand(CommandSender sender) {
    GameBox.debug("in GameAdminCommand pre command");
    if (!Permission.ADMIN_GAME.hasPermission(sender, gameBoxModule)) {
      sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_NO_PERM);
      return true;
    }
    return false;
  }
}
