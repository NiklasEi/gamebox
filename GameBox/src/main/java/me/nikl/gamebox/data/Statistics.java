package me.nikl.gamebox.data;

import me.nikl.gamebox.GameBox;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Niklas on 19.02.2017.
 *
 *
 */
public abstract class Statistics {


    public static final String PLAYER_PLAY_SOUNDS = "playSounds";
    public static final String GAMES_STATISTICS_NODE = "gameStatistics";
    public static final String TOKEN_PATH = "tokens";

    protected GameBox plugin;


    // ToDo: this whole class is chaotic
    //   restructure for next main version update!
    public Statistics(GameBox plugin){
        this.plugin = plugin;
    }


    public abstract boolean load();

    public abstract boolean getBoolean(UUID uuid, String path);


    public abstract boolean getBoolean(UUID uuid, String path, boolean defaultValue);

    public abstract void set(String uuid, String path, Object b);

    public abstract void save();

    public abstract int getInt(UUID uuid, String path, int defaultValue);

    public abstract void addStatistics(UUID uuid, String gameID, String gameTypeID, double value, SaveType saveType);

    public abstract ArrayList<Stat> getTopList(String gameID, String gameTypeID, SaveType saveType, int maxNumber);

    public abstract boolean isSet(String path);


    public class Stat{
        private double value;
        private UUID uuid;

        Stat(UUID uuid, double value){
            this.uuid = uuid;
            this.value = value;
        }

        public double getValue() {
            return value;
        }

        public UUID getUuid() {
            return uuid;
        }
    }
}
