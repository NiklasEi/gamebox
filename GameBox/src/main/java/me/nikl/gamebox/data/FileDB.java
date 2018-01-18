package me.nikl.gamebox.data;

import me.nikl.gamebox.GameBox;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author Niklas Eicker
 */
public class FileDB extends DataBase {
    private File dataFile;
    private FileConfiguration data;
    public FileDB(GameBox plugin) {
        super(plugin);
        this.dataFile = new File(plugin.getDataFolder().toString() + File.separatorChar + "data.yml");
    }

    @Override
    public boolean load(boolean async) {
        if(!dataFile.exists()){
            try {
                dataFile.getParentFile().mkdir();
                dataFile.createNewFile();
            } catch (IOException e) {
               e.printStackTrace();
               return false;
            }
        }

        // load stats file
        try {
            this.data = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(this.dataFile), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }

    @Override
    public void save(boolean async) {
        if(async) {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    GameBox.debug(" saving to file async...");
                    try {
                        data.save(dataFile);
                    } catch (IOException e) {
                        Bukkit.getLogger().log(Level.SEVERE, "failed to save statistics (async)");
                        e.printStackTrace();
                    }
                    GameBox.debug(" ...done");
                }
            };
            runnable.runTaskAsynchronously(plugin);
        } else {
            try {
                this.data.save(dataFile);
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.SEVERE, "failed to save statistics");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addStatistics(UUID uuid, String gameID, String gameTypeID, double value, SaveType saveType) {

        GameBox.debug("Adding statistics '" +uuid.toString()+"."+gameID+"."+gameTypeID+ "." + saveType.toString().toLowerCase()+"' with the value: " + value);
        double oldScore;
        switch (saveType){
            case SCORE:
            case TIME_HIGH:
            case HIGH_NUMBER_SCORE:
                oldScore = data.getDouble(uuid.toString() + "."+ GAMES_STATISTICS_NODE+"." + gameID +"."+gameTypeID + "." + saveType.toString().toLowerCase(), 0.);
                if(oldScore >= value) return;
                data.set(uuid.toString() + "."+ GAMES_STATISTICS_NODE+"." + gameID +"."+gameTypeID + "." + saveType.toString().toLowerCase(), value);
                break;

            case TIME_LOW:
                oldScore = data.getDouble(uuid.toString() + "."+ GAMES_STATISTICS_NODE+"." + gameID +"."+gameTypeID + "." + saveType.toString().toLowerCase(), Double.MAX_VALUE);
                if(oldScore <= value) return;
                data.set(uuid.toString() + "."+ GAMES_STATISTICS_NODE+"." + gameID +"."+gameTypeID + "." + saveType.toString().toLowerCase(), value);
                break;

            case WINS:
                oldScore = data.getDouble(uuid.toString() + "."+ GAMES_STATISTICS_NODE+"." + gameID +"."+gameTypeID + "." + saveType.toString().toLowerCase(), 0.);
                data.set(uuid.toString() + "."+ GAMES_STATISTICS_NODE+"." + gameID +"."+gameTypeID + "." + saveType.toString().toLowerCase(), value + oldScore);
                break;

            default:
                Bukkit.getLogger().log(Level.WARNING, "trying to save unsupported statistics: " + saveType.toString());
        }
    }

    @Override
    public ArrayList<Stat> getTopList(String gameID, String gameTypeID, SaveType saveType, int maxNumber) {
        ArrayList<Stat> toReturn = new ArrayList<>();
        Map<UUID, Double> valuesMap = new HashMap<>();
        for(String uuid : data.getKeys(false)){
            if(!data.isSet(uuid.toString() + "."+ GAMES_STATISTICS_NODE+"." + gameID +"."+gameTypeID + "." + saveType.toString().toLowerCase())) continue;
            try{
                UUID uuid1 = UUID.fromString(uuid);
                valuesMap.put(uuid1, data.getDouble(uuid.toString() + "."+ GAMES_STATISTICS_NODE+"." + gameID +"."+gameTypeID + "." + saveType.toString().toLowerCase()));
            } catch (IllegalArgumentException exception){
                exception.printStackTrace();
                Bukkit.getLogger().log(Level.WARNING, "failed to load a statistic for a player due to a malformed UUID");
                continue;
            }
        }

        boolean higher;
        switch (saveType){
            case TIME_LOW:
                higher = false;
                break;
            case WINS:
            case TIME_HIGH:
            case SCORE:
            case HIGH_NUMBER_SCORE:
                higher = true;
                break;

            default:
                higher = true;
                Bukkit.getLogger().log(Level.SEVERE, "not supported SaveType found while loading a top list");
        }
        UUID currentBestUuid = null;
        double currentBestScore;


        int number = 0;
        while(number < maxNumber && !valuesMap.keySet().isEmpty()){
            currentBestScore = higher?0.:Double.MAX_VALUE;
            for(Iterator<Map.Entry<UUID, Double>> entries = valuesMap.entrySet().iterator(); entries.hasNext(); ) {
                Map.Entry<UUID, Double> entry = entries.next();
                if(higher){
                    if(entry.getValue() > currentBestScore){
                        currentBestScore = entry.getValue();
                        currentBestUuid = entry.getKey();
                    }
                } else {
                    if(entry.getValue() < currentBestScore){
                        currentBestScore = entry.getValue();
                        currentBestUuid = entry.getKey();
                    }
                }
            }
            GameBox.debug("Found rank " + (number + 1) + " with time: " + currentBestScore + "      higher: " + higher);
            toReturn.add(new Stat(currentBestUuid, valuesMap.get(currentBestUuid)));
            number++;
            valuesMap.remove(currentBestUuid);
        }
        return toReturn;
    }

    @Override
    public void loadPlayer(GBPlayer player, boolean async) {
        boolean playSounds = data.getBoolean(player.getUuid() + "." +  DataBase.PLAYER_PLAY_SOUNDS, true);
        boolean allowInvitations = data.getBoolean(player.getUuid() + "." +  DataBase.PLAYER_ALLOW_INVITATIONS, true);
        int token = data.getInt(player.getUuid() + "." +  DataBase.PLAYER_TOKEN_PATH, 0);

        player.setPlayerData(token, playSounds, allowInvitations);
    }

    @Override
    public void savePlayer(GBPlayer player, boolean async) {
        // async ignored here for file saving... It is in cache anyways
        String uuid = player.getUuid().toString();
        data.set(uuid + "." +  DataBase.PLAYER_PLAY_SOUNDS, player.isPlaySounds());
        data.set(uuid + "." +  DataBase.PLAYER_ALLOW_INVITATIONS, player.allowsInvites());
        data.set(uuid + "." +  DataBase.PLAYER_TOKEN_PATH, player.getTokens());
    }

    @Override
    public void getToken(UUID uuid, Callback<Integer> callback) {
        callback.onSuccess(data.getInt(uuid.toString() + "." +  PLAYER_TOKEN_PATH, 0));
    }

    @Override
    public void setToken(UUID uuid, int token) {
        data.set(uuid + "." +  DataBase.PLAYER_TOKEN_PATH, token);
    }

    @Override
    public void onShutDown(){
        super.onShutDown();
    }
}
