package me.nikl.gamebox.data;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.data.database.DataBase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Niklas Eicker
 */
public class GBPlayer {
    private static Set<TokenListener> tokenListeners = new HashSet<>();
    private UUID uuid;
    private boolean playSounds = true;
    private GameBox plugin;
    private DataBase dataBase;
    private boolean allowInvites = true;
    private int tokens;
    private Player player;

    private boolean loaded = false;

    private boolean updated = false;

    public GBPlayer(GameBox plugin, UUID uuid) {
        this.uuid = uuid;
        this.plugin = plugin;
        this.dataBase = plugin.getDataBase();

        loadData();
    }

    public GBPlayer(GameBox plugin, UUID uuid, int token, boolean playSounds, boolean allowInvites) {
        this.uuid = uuid;
        this.plugin = plugin;
        this.dataBase = plugin.getDataBase();

        this.tokens = token;
        this.allowInvites = allowInvites;
        this.playSounds = playSounds;

        for (TokenListener tokenListener : tokenListeners) tokenListener.updateToken(this);
        this.loaded = true;
    }

    private void loadData() {
        dataBase.loadPlayer(this, true);
    }

    public void setPlayerData(int token, boolean playSounds, boolean allowInvites) {
        this.tokens = token;
        this.allowInvites = allowInvites;
        this.playSounds = playSounds;

        for (TokenListener tokenListener : tokenListeners) tokenListener.updateToken(this);
        this.loaded = true;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isPlaySounds() {
        return playSounds;
    }

    public void setPlaySounds(boolean playSounds) {
        this.updated = true;
        this.playSounds = playSounds;
    }

    public void toggleSound() {
        this.updated = true;
        this.playSounds = !playSounds;
    }

    public int getTokens() {
        return this.tokens;
    }

    public void setTokens(int newTokens) {
        this.updated = true;
        this.tokens = newTokens;
        for (TokenListener tokenListener : tokenListeners) tokenListener.updateToken(this);
    }

    public void remove(boolean async) {
        // remove special inventories and save any data
        // after this call this object will be removed from player map
        plugin.getPluginManager().getGuiManager().removePlayer(this.uuid);
        save(async);
    }

    public void remove() {
        remove(false);
    }

    public void save() {
        save(false);
    }

    public void save(boolean async) {
        // only save the player data, if settings were changed
        // for the file db, this means players are not written in the file as long as they didn't play or changed their settings
        // in case of a MySQL db, the statements loading the player also save the default settings
        if (updated) dataBase.savePlayer(this, async);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean allowsInvites() {
        return allowInvites;
    }

    // ToDo: settings menu
    public void setAllowInvites(boolean allowInvites) {
        this.updated = true;
        this.allowInvites = allowInvites;
    }

    public Player getPlayer() {
        if (player == null) {
            this.player = Bukkit.getPlayer(uuid);
        }
        return player;
    }

    public static void addTokenListener(TokenListener tokenListener) {
        tokenListeners.add(tokenListener);
    }

    public static void clearTokenListeners() {
        tokenListeners.clear();
    }
}
