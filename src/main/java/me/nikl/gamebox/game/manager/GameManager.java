package me.nikl.gamebox.game.manager;

import me.nikl.gamebox.game.exceptions.GameStartException;
import me.nikl.gamebox.game.rules.GameRule;
import me.nikl.gamebox.inventory.GameBoxHolder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;

/**
 * @author Niklas Eicker
 */
public interface GameManager extends GameBoxHolder {
  /**
   * Return whether a player is in a game or not
   *
   * @param uuid player's uuid
   * @return ingame
   */
  boolean isInGame(UUID uuid);

  /**
   * Start a game for a player or players
   *
   * @param players    to start the game for
   * @param playSounds whether to play sounds in the game
   * @param args       game arguments
   * @throws GameStartException if game start fails
   */
  void startGame(Player[] players, boolean playSounds, String... args) throws GameStartException;

  /**
   * Remove the specified player from his game
   * Do not close the inventory
   * <p>
   * If the game is not finished yet, consider this as the player closing the inventory
   * This method is mainly called when a button from the lower inventory is clicked
   * and the player goes back to one of the menus
   *
   * @param uuid player to remove
   */
  void removeFromGame(UUID uuid);

  /**
   * Load game rules from the given ConfigurationSection
   *
   * @param buttonSec Configuration section with the rules
   * @param buttonID  ID of the rules
   */
  void loadGameRules(ConfigurationSection buttonSec, String buttonID);

  /**
   * Getter for game rules
   *
   * @return game rules
   */
  Map<String, ? extends GameRule> getGameRules();

  /**
   * Dummy method
   * Just to be able to make instance checks against InventoryHolder
   *
   * @return null
   */
  @Override
  Inventory getInventory();
}
