package me.nikl.gamebox.commands.player;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.utility.Permission;
import org.bukkit.entity.Player;

/**
 * @author Niklas Eicker
 */
@CommandAlias("%mainCommand")
public class GetTokenCount extends PlayerBaseCommand {
  public GetTokenCount(GameBox gameBox) {
    super(gameBox);
  }

  @Subcommand("token|t")
  public void getOwnTokenCount(Player player) {
    if (!Permission.CMD_TOKEN.hasPermission(player)) {
      player.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_NO_PERM);
      return;
    }
    GBPlayer gbPlayer = gameBox.getPluginManager().getPlayer(player.getUniqueId());
    if (gbPlayer == null) return;
    player.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_OWN_TOKEN_INFO.replace("%token%", String.valueOf(gbPlayer.getTokens())));
  }
}
