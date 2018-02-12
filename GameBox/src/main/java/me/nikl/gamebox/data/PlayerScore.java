package me.nikl.gamebox.data;

import java.util.UUID;

/**
 * Created by nikl on 22.01.18.
 */
public class PlayerScore {
    private double value;
    private UUID uuid;

    private SaveType saveType;

    public PlayerScore(UUID uuid, double value, SaveType saveType){
        this.uuid = uuid;
        this.value = value;
        this.saveType = saveType;
    }

    public double getValue() {
        return value;
    }

    public UUID getUuid() {
        return uuid;
    }

    public SaveType getSaveType() {
        return saveType;
    }

    public boolean isBetterThen(PlayerScore score){
        if(saveType.isHigherScore()) return value > score.getValue();
        return value < score.getValue();
    }

    public void updateValue(double newValue){
        this.value = newValue;
    }
}
