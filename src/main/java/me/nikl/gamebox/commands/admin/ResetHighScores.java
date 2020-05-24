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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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

  @Subcommand("resetscores")
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

  @Subcommand("resetscores")
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

  @Subcommand("resetscores")
  @CommandCompletion("@players")
  public void resetHighScores(CommandSender sender, @Single String playerName) {
    OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
    if (player == null || !player.hasPlayedBefore()) {
      sender.sendMessage(gameBox.lang.PREFIX + " cannot find player '" + playerName + "'");
      return;
    }
    gameBox.getDataBase().resetHighScores(player.getUniqueId());
    sender.sendMessage(gameBox.lang.PREFIX + " deleted scores of player '" + playerName + "'");
    sender.sendMessage(gameBox.lang.PREFIX + " If you also want to delete the players game saves, run '/gba removesaves " + playerName);
    sender.sendMessage(gameBox.lang.PREFIX + " reloading GameBox...");
    gameBox.reload(sender);
  }

  @Subcommand("resetscores")
  @CommandCompletion("@players @allGameIds check_your_game_config @SaveTypes")
  public void resetHighScores(CommandSender sender, String playerName, String gameId, String gameTypeID, @Single String saveTypeStr) {
    OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
    if (player == null || !player.hasPlayedBefore()) {
      sender.sendMessage(gameBox.lang.PREFIX + " cannot find player '" + playerName + "'");
      return;
    }
    SaveType saveType;
    try {
      saveType = SaveType.valueOf(saveTypeStr.toUpperCase());
    } catch (IllegalArgumentException exception) {
      sender.sendMessage("Valid saveTypes: " + Arrays.stream(SaveType.values()).map(Enum::toString).collect(Collectors.joining(", ")));
      return;
    }
    gameBox.getDataBase().resetHighScores(player.getUniqueId(), gameId, gameTypeID, saveType);
    sender.sendMessage(gameBox.lang.PREFIX + " deleted scores of player '" + playerName + "' in game '" + gameId + "'");
    sender.sendMessage(gameBox.lang.PREFIX + " If you also want to delete the players game saves, run '/gba removesaves " + playerName);
    sender.sendMessage(gameBox.lang.PREFIX + " reloading GameBox...");
    gameBox.reload(sender);
  }
}
