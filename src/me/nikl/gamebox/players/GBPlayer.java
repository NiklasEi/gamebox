package me.nikl.gamebox.players;

import java.util.UUID;

/**
 * Created by Niklas on 17.02.2017.
 *
 */
public class GBPlayer {
    private UUID uuid;
    private boolean playSounds = true;

    public GBPlayer(UUID uuid){
        this.uuid = uuid;
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
}
