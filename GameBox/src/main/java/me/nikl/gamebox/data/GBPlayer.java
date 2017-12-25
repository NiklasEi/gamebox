package me.nikl.gamebox.data;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Niklas on 17.02.2017.
 *
 */
public class GBPlayer {
    private UUID uuid;
    private boolean playSounds = true;
    private GameBox plugin;
    private DataBase dataBase;
    private boolean allowInvites = true;
    private int tokens;
    private Player player;

    private boolean loaded = false;

    public GBPlayer(GameBox plugin, UUID uuid){
        this.uuid = uuid;
        this.plugin = plugin;
        this.dataBase = plugin.getDataBase();

        loadData();
    }

    private void loadData() {
        dataBase.loadPlayer(this, true);
    }

    public void setPlayerData(int token, boolean playSounds, boolean allowInvites){
        this.tokens = token;
        this.allowInvites = allowInvites;
        this.playSounds = playSounds;

        this.loaded = true;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isPlaySounds() {
        return playSounds;
    }

    public void setPlaySounds(boolean playSounds) {
        this.playSounds = playSounds;
    }

    public void toggleSound(){
        this.playSounds = !playSounds;
    }

    public int getTokens(){
        return this.tokens;
    }

    public void setTokens(int newTokens){
        this.tokens = newTokens;
        plugin.getPluginManager().getGuiManager().updateTokens(this);
    }

    public void remove() {
        //TOdO (async not possible on shutdown...)
        // remove special inventories and save any data
        // after this call this object will be removed from player map
        plugin.getPluginManager().getGuiManager().removePlayer(this.uuid);
        save(true);
    }

    public void save(){
        save(false);
    }

    public void save(boolean async) {
        dataBase.savePlayer(this, async);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean allowsInvites() {
        return allowInvites;
    }

    public Player getPlayer() {
        if(player == null){
            this.player = Bukkit.getPlayer(uuid);
        }
        return player;
    }
}
