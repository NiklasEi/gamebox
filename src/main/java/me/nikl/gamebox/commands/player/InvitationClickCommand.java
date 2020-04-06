package me.nikl.gamebox.commands.player;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Private;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.inventory.GuiManager;
import org.bukkit.entity.Player;

/**
 * @author Niklas Eicker
 */
@CommandAlias("%mainCommand")
public class InvitationClickCommand extends PlayerBaseCommand {
  private GuiManager guiManager;

  public InvitationClickCommand(GameBox gameBox) {
    super(gameBox);
    this.guiManager = gameBox.getPluginManager().getGuiManager();
  }

  /**
   * This command is used internally to open a gui when clicking a message in the chat.
   *
   * @param player for whom the GUI will open
   * @param args   arguments for the opening GUI
   */
  @Subcommand("%INVITE_CLICK_COMMAND")
  @Private
  public void onInvitationMessageClick(Player player, String[] args) {
    guiManager.openGameGui(player, args);
  }
}
