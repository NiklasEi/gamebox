package me.nikl.gamebox.game.manager;

import org.bukkit.inventory.Inventory;

/**
 * @author Niklas Eicker
 * <p>
 * Simple implementation of GameManager that takes away the need to override #getInventory() in every game manager.
 */
public abstract class EasyManager implements GameManager {

  @Override
  public Inventory getInventory() {
    return null;
  }
}
