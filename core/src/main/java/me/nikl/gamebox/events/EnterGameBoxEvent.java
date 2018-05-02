package me.nikl.gamebox.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;

/**
 * @author Niklas Eicker
 *
 * Event called when a player enters a GameBox GUI or game
 *
 * This is called before the player inventory is stored and cleared
 */
public class EnterGameBoxEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private String cancelMessage;
    private String[] args;
    private Player player;

    public EnterGameBoxEvent(Player player, String... args) {
        this.player = player;
        this.args = args;
        Bukkit.getPluginManager().callEvent(this);
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public Player getPlayer() {
        return player;
    }

    public @Nullable
    String getCancelMessage() {
        return cancelMessage;
    }

    public void setCancelMessage(String message) {
        this.cancelMessage = message;
    }

    public String[] getArgs() {
        return args;
    }
}
