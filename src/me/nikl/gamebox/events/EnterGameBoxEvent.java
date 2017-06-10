package me.nikl.gamebox.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Niklas
 *
 * Event called when a player enters a GameBox GUI or game
 *
 * This is called before the player inventory is stored and cleared
 */
public class EnterGameBoxEvent extends Event implements Cancellable{
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private String cancelMessage = "none";
    private String[] args;

    private Player player;

    public EnterGameBoxEvent(Player player, String... args){
        this.player = player;
        this.args = args;
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

    public String getCancelMessage() {
        return cancelMessage;
    }

    public void setCancelMessage(String message){
        this.cancelMessage = message;
    }

    public String[] getArgs() {
        return args;
    }
}
