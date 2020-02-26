package me.nikl.gamebox.listeners;

import me.nikl.gamebox.GameBox;
import org.bukkit.event.Listener;

/**
 * Created by nikl on 14.02.18.
 */
public abstract class GameBoxListener implements Listener {
  protected GameBox gameBox;

  public GameBoxListener(GameBox gameBox) {
    this.gameBox = gameBox;
    gameBox.getServer().getPluginManager().registerEvents(this, gameBox);
  }
}
