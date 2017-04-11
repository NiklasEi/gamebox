package me.nikl.gamebox.players;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.data.Statistics;

import java.util.UUID;

/**
 * Created by Niklas on 17.02.2017.
 *
 */
public class GBPlayer {
    private UUID uuid;
    private boolean playSounds = true;
    private GameBox plugin;
    private Statistics statistics;

    private int tokens;

    public GBPlayer(GameBox plugin, UUID uuid){
        this.uuid = uuid;
        this.plugin = plugin;
        this.statistics = plugin.getStatistics();

        loadData();
    }

    private void loadData() {
        // ToDo
        playSounds = statistics.getBoolean(uuid, Statistics.PLAYER_PLAY_SOUNDS, true);
        tokens = statistics.getInt(uuid, Statistics.TOKEN_PATH, 0);
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
        plugin.getPluginManager().getGuiManager().getMainGui().updateTokens(this);
    }

    public void remove() {
        //TOdO
        // remove special inventories and save any data
        // after this call this object will be removed
        plugin.getPluginManager().getGuiManager().removePlayer(this.uuid);
        save();
    }

    public void save() {
        //go through all values and save them
        statistics.set(uuid.toString(), Statistics.PLAYER_PLAY_SOUNDS, playSounds);
        statistics.set(uuid.toString(), Statistics.TOKEN_PATH, tokens);
    }
}
