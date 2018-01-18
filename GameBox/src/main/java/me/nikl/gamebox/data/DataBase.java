package me.nikl.gamebox.data;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.UUID;

/**
 * @author Niklas Eicker
 *
 *
 */
public abstract class DataBase {
    protected static final String GAMES_STATISTICS_NODE = "gameStatistics";
    protected static final String PLAYER_PLAY_SOUNDS = "playSounds";
    protected static final String PLAYER_ALLOW_INVITATIONS = "allowInvitations";
    protected static final String PLAYER_TOKEN_PATH = "tokens";
    protected static final String PLAYER_UUID = "uuid";
    protected static final String PLAYER_NAME = "name";
    protected static final String PLAYER_TABLE = "GBPlayers";

    private BukkitRunnable autoSave;
    protected GameBox plugin;

    public DataBase(GameBox plugin){
        this.plugin = plugin;
        createAutoSaveRunnable();
        if(GameBoxSettings.autoSaveInterval > 0){
            autoSave.runTaskTimerAsynchronously(plugin
                    , GameBoxSettings.autoSaveInterval * 60 * 20
                    , GameBoxSettings.autoSaveInterval * 60 * 20);
        }
    }

    public abstract boolean load(boolean async);

    public abstract void save(boolean async);

    public abstract void addStatistics(UUID uuid, String gameID, String gameTypeID, double value, SaveType saveType);

    public abstract ArrayList<Stat> getTopList(String gameID, String gameTypeID, SaveType saveType, int maxNumber);

    public abstract void loadPlayer(GBPlayer player, boolean async);

    public abstract void savePlayer(GBPlayer player, boolean async);

    public abstract void getToken(UUID uuid, final Callback<Integer> callback);

    public abstract void setToken(UUID uuid, int token);

    private void createAutoSaveRunnable() {
        this.autoSave = new BukkitRunnable() {
            @Override
            public void run() {
                GameBox.debug(" auto saving...");
                if(plugin == null || plugin.getDataBase() == null)
                    this.cancel();
                GameBox.debug("    saving all players...");
                for(GBPlayer player : plugin.getPluginManager().getGbPlayers().values()){
                    player.save(true);
                }
                GameBox.debug("    saving database...");
                // already async, no need to create a second async task
                save(false);
                GameBox.debug(" done!");
            }
        };
    }

    public void onShutDown(){
        if(autoSave != null)
            autoSave.cancel();
        save(false);
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

    public interface Callback<T> {
        void onSuccess(T done);
        void onFailure(@Nullable Throwable throwable, @Nullable T value);
    }
}
