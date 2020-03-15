package me.nikl.gamebox.external;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.game.Game;
import org.bukkit.entity.Player;

/**
 * @author Niklas Eicker
 * <p>
 * Provide GameBox placeholders through Placeholder API
 */
public class PlaceholderAPIHook extends PlaceholderExpansion {
  private GameBox plugin;

  public PlaceholderAPIHook(GameBox plugin) {
    this.plugin = plugin;
    register();
  }

  @Override
  public boolean persist() {
    return true;
  }

  @Override
  public boolean canRegister() {
    return true;
  }

  @Override
  public String getAuthor() {
    return plugin.getDescription().getAuthors().toString();
  }

  @Override
  public String getIdentifier() {
    return GameBoxSettings.getGameBoxModuleInfo().getId();
  }

  @Override
  public String getVersion() {
    return plugin.getDescription().getVersion();
  }

  @Override
  public String onPlaceholderRequest(Player player, String identifier) {
    String gameID;
    if (plugin.getPluginManager().getGames().containsKey(identifier.split("_")[identifier.split("_").length - 1])) {
      gameID = identifier.split("_")[identifier.split("_").length - 1];
      identifier = identifier.replace("_" + gameID, "");
    }
    switch (identifier) {
      // return the name of the game that the player is currently playing
      case "game_name":
        if (player == null) return null;
        Game game = plugin.getPluginManager().getGame(player.getUniqueId());
        if (game == null) return null;
        return game.getGameLang().PLAIN_NAME;
    }
    return null;
  }
}
