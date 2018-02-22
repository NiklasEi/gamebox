package me.nikl.gamebox.games;

import me.nikl.gamebox.games.exceptions.GameStartException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Map;
import java.util.UUID;

/**
 * Created by niklas on 2/4/17.
 */
public interface GameManager extends InventoryHolder {

    /**
     * Handle a click in the game
     *
     * @param event ClickEvent
     * @return successful
     */
    boolean onInventoryClick(InventoryClickEvent event);


    /**
     * Handle an InventoryCloseEvent
     * You should do the same as in IGameManager.removeFromGame
     *
     * @param event CloseEvent
     * @return successful
     */
    boolean onInventoryClose(InventoryCloseEvent event);


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
     * @param players players to start the game with
     * @param args    additional arguments
     */
    void startGame(Player[] players, boolean playSounds, String... args) throws GameStartException;


    /**
     * Remove the specified player from his game
     * Do not close the inventory
     *
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
