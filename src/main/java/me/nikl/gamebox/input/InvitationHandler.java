package me.nikl.gamebox.input;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxLanguage;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.PluginManager;
import me.nikl.gamebox.commands.GameBoxCommands;
import me.nikl.gamebox.inventory.gui.game.StartMultiplayerGamePage;
import me.nikl.nmsutilities.NmsFactory;
import me.nikl.nmsutilities.NmsUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * @author Niklas Eicker
 * <p>
 * Handle Invitations for multi player games
 */
public class InvitationHandler extends BukkitRunnable {
  private final String COMMAND = GameBoxSettings.mainCommand.split("\\|")[0];
  private Set<Invitation> invitations = new HashSet<>();
  private PluginManager pluginManager;
  private GameBox plugin;
  private NmsUtility nmsUtility;
  private GameBoxLanguage lang;
  private String clickMessagePartOne;
  private String getClickMessagePartTwo;

  public InvitationHandler(GameBox plugin) {
    pluginManager = plugin.getPluginManager();
    this.plugin = plugin;
    this.nmsUtility = NmsFactory.getNmsUtility();
    this.lang = plugin.lang;
    cacheClickMessageParts();
    this.runTaskTimerAsynchronously(plugin, 20, 10);
  }

  private void cacheClickMessageParts() {
    boolean boldClick = false;
    clickMessagePartOne = " [{\"text\":\"" + lang.JSON_PREFIX_PRE_TEXT + "\",\"color\":\"" + lang.JSON_PREFIX_PRE_COLOR + "\"},{\"text\":\"" + lang.JSON_PREFIX_TEXT + "\",\"color\":\""
            + lang.JSON_PREFIX_COLOR + "\"},{\"text\":\"" + lang.JSON_PREFIX_AFTER_TEXT + "\",\"color\":\"" + lang.JSON_PREFIX_AFTER_COLOR + "\"}" +
            ",{\"text\":\"" + lang.INVITATION_PRE_TEXT + "\",\"color\":\""
            + lang.INVITATION_PRE_COLOR + "\"},{\"text\":\"" + lang.INVITATION_CLICK_TEXT + "\",\"color\":\""
            + lang.INVITATION_CLICK_COLOR + "\",\"bold\":" + boldClick + ",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/" + COMMAND + " "
            + GameBoxCommands.INVITE_CLICK_COMMAND + " ";
    getClickMessagePartTwo = "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"" + lang.INVITATION_HOVER_TEXT + "\",\"color\":\""
            + lang.INVITATION_HOVER_COLOR + "\"}}}, {\"text\":\"" + lang.INVITATION_AFTER_TEXT + "\",\"color\":\"" + lang.INVITATION_AFTER_COLOR + "\"}]";
  }

  @Override
  public void run() {
    Iterator<Invitation> it = invitations.iterator();
    ArrayList<UUID> ranOut = new ArrayList<>();
    while (it.hasNext()) {
      Invitation inv = it.next();
      long currentTimeMillis = System.currentTimeMillis();
      if (currentTimeMillis > inv.timeStamp) {
        ranOut.add(inv.player1);
        it.remove();
        ((StartMultiplayerGamePage) pluginManager.getGuiManager().getGameGui(inv.args[0], inv.args[1])).removeInvite(inv.player1, inv.player2);
        GameBox.debug("removing old invitation");
      }
    }
  }

  public boolean addInvite(UUID player1, UUID player2, long timeStamp, String... args) {
    for (Invitation inv : invitations) {
      if (inv.player1.equals(player1) && inv.player2.equals(player2)) {
        Player player = Bukkit.getPlayer(player1);
        if (player != null) player.sendMessage(plugin.lang.INVITATION_ALREADY_THERE);
        return false;
      }
    }
    Player first = Bukkit.getPlayer(player1);
    Player second = Bukkit.getPlayer(player2);
    if (first != null && second != null) {
      for (String message : plugin.lang.INVITE_MESSAGE) {
        second.sendMessage(plugin.lang.PREFIX + message.replace("%player%", first.getName()).replace("%game%", pluginManager.getGame(args[0]).getGameLang().PLAIN_NAME));
      }
      if (GameBoxSettings.sendInviteClickMessage) {
        nmsUtility.sendJSON(second, buildClickMessage(args));
      }
      if (GameBoxSettings.sendInviteActionbarMessage) {
        nmsUtility.sendActionbar(second, lang.INVITE_ACTIONBAR_MESSAGE.replace("%player%", first.getName()).replace("%game%", pluginManager.getGame(args[0]).getGameLang().PLAIN_NAME));
      }
      if (GameBoxSettings.sendInviteTitleMessage) {
        nmsUtility.sendTitle(second
                , lang.INVITE_TITLE_MESSAGE.replace("%player%", first.getName()).replace("%game%", pluginManager.getGame(args[0]).getGameLang().PLAIN_NAME)
                , lang.INVITE_SUBTITLE_MESSAGE.replace("%player%", first.getName()).replace("%game%", pluginManager.getGame(args[0]).getGameLang().PLAIN_NAME)
                , 80);
      }
    } else {
      return false;
    }
    new Invitation(player1, player2, timeStamp, args);
    ((StartMultiplayerGamePage) pluginManager.getGuiManager().getGameGui(args[0], args[1])).addInvite(player1, player2);
    return true;
  }

  private String buildClickMessage(String[] args) {
    return clickMessagePartOne + args[0] + " " + args[1] + getClickMessagePartTwo;
  }

  public class Invitation {
    protected long timeStamp;
    protected UUID player1, player2;
    protected String[] args;

    public Invitation(UUID player1, UUID player2, long timeStamp, String... args) {
      this.player1 = player1;
      this.player2 = player2;
      this.timeStamp = timeStamp;
      this.args = args;

      GameBox.debug("adding new invitation");
      invitations.add(this);
    }
  }
}
