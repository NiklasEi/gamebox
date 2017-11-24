package me.nikl.gamebox.data;

import me.nikl.gamebox.GameBox;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Niklas on 19.02.2017.
 *
 *
 */
public abstract class DataBase {

    protected Set<BukkitRunnable> runnables = new HashSet<>();


    public static final String PLAYER_PLAY_SOUNDS = "playSounds";
    public static final String GAMES_STATISTICS_NODE = "gameStatistics";
    public static final String TOKEN_PATH = "tokens";

    protected GameBox plugin;


    // ToDo: this whole class is chaotic
    //   restructure for next main version update!
    public DataBase(GameBox plugin){
        this.plugin = plugin;
    }

    public abstract boolean load(boolean async);

    public abstract boolean getBoolean(UUID uuid, String path);

    public abstract boolean getBoolean(UUID uuid, String path, boolean defaultValue);

    public abstract void set(String uuid, String path, Object b);

    public abstract void save(boolean async);

    public abstract int getInt(UUID uuid, String path, int defaultValue);

    public abstract void addStatistics(UUID uuid, String gameID, String gameTypeID, double value, SaveType saveType);

    public abstract ArrayList<Stat> getTopList(String gameID, String gameTypeID, SaveType saveType, int maxNumber);

    public abstract boolean isSet(String path);

    public abstract void loadPlayer(GBPlayer player, boolean async);

    public abstract void savePlayer(GBPlayer player, boolean async);

    public void onShutDown(){
        save(true);
        boolean waiting = !runnables.isEmpty();
        if(waiting) plugin.info(" waiting on async tasks...");
        while (!runnables.isEmpty()){}
        if(waiting) plugin.info(" ... done");
    }

    public void removeRunnable(BukkitRunnable runnable){
        runnables.remove(runnable);
    }

    public class Stat{
        private double value;
        private UUID uuid;

        private SaveType saveType;

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

        public SaveType getSaveType() {
            return saveType;
        }
    }
}
