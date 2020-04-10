package me.nikl.gamebox.commands.admin;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.PreCommand;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.data.database.DataBase;
import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.utility.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Niklas Eicker
 */
@CommandAlias("%adminCommand")
public class ResetHighScores extends GameBoxBaseCommand {
  private boolean hasAttemptedToResetHighScoresBefore = false;

  public ResetHighScores(GameBox gameBox) {
    super(gameBox);
  }

  @Override
  @PreCommand
  public boolean preCommand(CommandSender sender) {
    GameBox.debug("in ResetHighScores pre command");
    if (!Permission.ADMIN_DATABASE.hasPermission(sender)) {
      sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_NO_PERM);
      return true;
    }
    return false;
  }

  @Subcommand("resetstats")
  @CommandCompletion("@allGameIds check_your_game_config @SaveTypes")
  public void resetHighScores(CommandSender sender, String gameID, String gameTypeID, @Single String saveTypeStr) {
    DataBase dataBase = gameBox.getDataBase();
    try {
      SaveType saveType = SaveType.valueOf(saveTypeStr.toUpperCase());
      dataBase.resetHighScores(gameID, gameTypeID, saveType);
      sender.sendMessage(" High score reset successful");
      gameBox.reload(sender);
    } catch (IllegalArgumentException exception) {
      sender.sendMessage("Valid saveTypes: " + Arrays.stream(SaveType.values()).map(Enum::toString).collect(Collectors.joining(", ")));
    }
  }

  @Subcommand("resetstats")
  public void resetHighScores(CommandSender sender) {
    if (!hasAttemptedToResetHighScoresBefore) {
      hasAttemptedToResetHighScoresBefore = true;
      new BukkitRunnable() {
        @Override
        public void run() {
          hasAttemptedToResetHighScoresBefore = false;
        }
      }.runTaskLater(gameBox, 20 * 10);
      sender.sendMessage(gameBox.lang.PREFIX + " This will remove ALL high scores!");
      sender.sendMessage(gameBox.lang.PREFIX + " There is no going back, are you sure?");
      sender.sendMessage(gameBox.lang.PREFIX + " Run this command again in the next 10 seconds");
      sender.sendMessage(gameBox.lang.PREFIX + "     to reset all high scores.");
      return;
    }
    gameBox.getDataBase().resetHighScores();
    gameBox.reload(sender);
  }
}
