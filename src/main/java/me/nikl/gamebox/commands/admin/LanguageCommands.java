package me.nikl.gamebox.commands.admin;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.PreCommand;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.utility.ConfigManager;
import me.nikl.gamebox.utility.Permission;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Niklas Eicker
 */
@CommandAlias("%adminCommand")
public class LanguageCommands extends GameBoxBaseCommand {

  public LanguageCommands(GameBox gameBox) {
    super(gameBox);
  }

  @Override
  @PreCommand
  public boolean preCommand(CommandSender sender) {
    GameBox.debug("in LanguageCommands pre command");
    if (!Permission.ADMIN_LANGUAGE.hasPermission(sender)) {
      sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_NO_PERM);
      return true;
    }
    if (sender instanceof Player) {
      sender.sendMessage(gameBox.lang.PREFIX + " Only from the console!");
      return true;
    }
    return false;
  }

  @Subcommand("language|lang")
  public void onLanguageCommand(CommandSender sender) {
    ConfigManager.printIncompleteLangFilesInfo(gameBox);
  }

  @Subcommand("language|lang all")
  public void onLanguageAllCommand(CommandSender sender) {
    ConfigManager.printMissingKeys(gameBox);
  }

  @Subcommand("language|lang")
  @CommandCompletion("@installedGameIds")
  public void onLanguageCommand(CommandSender sender, @Single String gameId) {
    if (!ConfigManager.getGameIdsWithMissingKeys().contains(gameId.toLowerCase())) {
      gameBox.info(" Game '" + gameId.toLowerCase() + "' does not exist or has no missing keys.");
      gameBox.info(" Valid options: " + String.join(", ", ConfigManager.getGameIdsWithMissingKeys()));
      return;
    }
    gameBox.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - +");
    ConfigManager.printMissingGameKeys(gameBox, gameId.toLowerCase());
    gameBox.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - +");
  }
}
