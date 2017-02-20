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

    public GBPlayer(GameBox plugin, UUID uuid){
        this.uuid = uuid;
        this.plugin = plugin;
        this.statistics = plugin.getStatistics();

        loadData();
        //plugin.getPluginManager().getGuiManager().getMainGui().loadMainGui(this);
    }

    private void loadData() {
        // ToDo
        if(statistics.isSet(uuid.toString())){
            playSounds = statistics.getBoolean(uuid, Statistics.PLAYER_PLAY_SOUNDS, true);
        }
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
    }
}
