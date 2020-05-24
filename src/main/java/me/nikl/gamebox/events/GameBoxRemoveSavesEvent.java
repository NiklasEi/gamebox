package me.nikl.gamebox.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Event fired when game saves should be cleared
 */
public class GameBoxRemoveSavesEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    /**
     * Players to delete saves for
     *
     * If empty, ALL players are effected
     */
    private List<UUID> players = new ArrayList<>();
    /**
     * Games to delete saves for
     *
     * If empty, ALL games are effected
     */
    private List<String> gameIds = new ArrayList<>();

    public GameBoxRemoveSavesEvent() {
        Bukkit.getPluginManager().callEvent(this);
    }

    public GameBoxRemoveSavesEvent(List<UUID> players, List<String> gameIds) {
        this.players = players;
        this.gameIds = gameIds;
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

    public List<UUID> getPlayers() {
        return players;
    }

    public List<String> getGameIds() {
        return gameIds;
    }
}
