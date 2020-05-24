package me.nikl.gamebox.commands.admin;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.PreCommand;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.events.GameBoxRemoveSavesEvent;
import me.nikl.gamebox.utility.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Niklas Eicker
 */
@CommandAlias("%adminCommand")
public class RemoveSaves extends GameBoxBaseCommand {
  private boolean hasAttemptedToRemoveAllSavesBefore = false;

  public RemoveSaves(GameBox gameBox) {
    super(gameBox);
  }

  @Override
  @PreCommand
  public boolean preCommand(CommandSender sender) {
    GameBox.debug("in RemoveSaves pre command");
    if (!Permission.ADMIN_DATABASE.hasPermission(sender)) {
      sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_NO_PERM);
      return true;
    }
    return false;
  }

  @Subcommand("removesaves")
  public void resetHighScores(CommandSender sender) {
    if (!hasAttemptedToRemoveAllSavesBefore) {
      hasAttemptedToRemoveAllSavesBefore = true;
      new BukkitRunnable() {
        @Override
        public void run() {
          hasAttemptedToRemoveAllSavesBefore = false;
        }
      }.runTaskLater(gameBox, 20 * 10);
      sender.sendMessage(gameBox.lang.PREFIX + " This will remove ALL game saves!");
      sender.sendMessage(gameBox.lang.PREFIX + " There is no going back, are you sure?");
      sender.sendMessage(gameBox.lang.PREFIX + " Run this command again in the next 10 seconds");
      sender.sendMessage(gameBox.lang.PREFIX + "     to remove all game saves.");
      sender.sendMessage(gameBox.lang.PREFIX + " FYI: removing high scores has to be done separately");
      return;
    }
    new GameBoxRemoveSavesEvent();
    gameBox.reload(sender);
  }

  @Subcommand("removesaves")
  @CommandCompletion("@players")
  public void resetHighScores(CommandSender sender, @Single String playerName) {
    OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
    if (player == null || !player.hasPlayedBefore()) {
      sender.sendMessage(gameBox.lang.PREFIX + " cannot find player '" + playerName + "'");
      return;
    }
    new GameBoxRemoveSavesEvent(Collections.singletonList(player.getUniqueId()), new ArrayList<>());
    sender.sendMessage(gameBox.lang.PREFIX + " deleted saves of player '" + playerName + "'");
    sender.sendMessage(gameBox.lang.PREFIX + " If you also want to delete the players high scores, run '/gba resetscores " + playerName);
    sender.sendMessage(gameBox.lang.PREFIX + " reloading GameBox...");
    gameBox.reload(sender);
  }

  @Subcommand("removesaves")
  @CommandCompletion("@players @allGameIds")
  public void resetHighScores(CommandSender sender, String playerName, String gameId) {
    OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
    if (player == null || !player.hasPlayedBefore()) {
      sender.sendMessage(gameBox.lang.PREFIX + " cannot find player '" + playerName + "'");
      return;
    }
    new GameBoxRemoveSavesEvent(Collections.singletonList(player.getUniqueId()), Collections.singletonList(gameId));
    sender.sendMessage(gameBox.lang.PREFIX + " deleted scores of player '" + playerName + "' in game '" + gameId + "'");
    sender.sendMessage(gameBox.lang.PREFIX + " If you also want to delete the players high scores, use resetscores command");
    sender.sendMessage(gameBox.lang.PREFIX + " reloading GameBox...");
    gameBox.reload(sender);
  }
}
