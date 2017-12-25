package me.nikl.gamebox;

import me.nikl.gamebox.data.DataBase;
import me.nikl.gamebox.data.GBPlayer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * API for GameBox
 *
 * Use this class to retrieve and manipulate GameBox data.
 */
public class GameBoxAPI {
    private GameBox plugin;


    public GameBoxAPI(GameBox plugin){
        this.plugin = plugin;
    }


    /**
     * Give token to the specified player.
     * This might be done async and this method returns before the value is changed!
     * @param player name
     * @param count token to give
     */
    public void giveToken(OfflinePlayer player, int count){
        Validate.notNull(player, "Player cannot be null!");
        Validate.isTrue(count > 0, "token count to give must be greater then 0");

        // handle cached players
        GBPlayer gbPlayer = plugin.getPluginManager().getPlayer(player.getUniqueId());
        if (gbPlayer != null) {
            if(gbPlayer.isLoaded()){
                gbPlayer.setTokens(gbPlayer.getTokens() + count);
                return;
            } else {
                // player data is being looked up... Wait a bit and try again
                Bukkit.getScheduler().runTaskLater(plugin, () -> giveToken(player, count), 2);
                return;
            }
        }

        // handle offline or not cached players
        plugin.getDataBase().getToken(player.getUniqueId(), new DataBase.Callback<Integer>() {
            @Override
            public void onSuccess(Integer done) {
                plugin.getDataBase().setToken(player.getUniqueId(), done + count);
            }

            @Override
            public void onFailure(Throwable throwable, Integer value) {
                plugin.getLogger().warning(" Failed to handle API call giveToken for player: " + player.getName());
                if(throwable != null) throwable.printStackTrace();
            }
        });
    }

    /**
     * Set Token count for a player to a specific value.
     *
     * @param player to manipulate
     * @param count new token count
     */
    public void setToken(OfflinePlayer player, int count){
        Validate.notNull(player, "Player cannot be null!");
        Validate.isTrue(count >= 0, "token count must be greater then or equal 0");

        // handle cached players
        GBPlayer gbPlayer = plugin.getPluginManager().getPlayer(player.getUniqueId());
        if (gbPlayer != null) {
            if(gbPlayer.isLoaded()){
                gbPlayer.setTokens(count);
                return;
            } else {
                // player data is being looked up... Wait a bit and try again
                Bukkit.getScheduler().runTaskLater(plugin, () -> setToken(player, count), 2);
                return;
            }
        }

        plugin.getDataBase().setToken(player.getUniqueId(), count);
    }

    /**
     * Take token from a specified player
     * @param player
     * @param count token to take
     */
    public void takeToken(OfflinePlayer player, int count, DataBase.Callback<Integer> callback){
        Validate.notNull(player, "Player cannot be null!");
        Validate.isTrue(count > 0, "token to take must be greater then 0");

        // handle cached players
        GBPlayer gbPlayer = plugin.getPluginManager().getPlayer(player.getUniqueId());
        if (gbPlayer != null) {
            if(gbPlayer.isLoaded()){
                if(gbPlayer.getTokens() >= count){
                    gbPlayer.setTokens(gbPlayer.getTokens() - count);
                    callback.onSuccess(gbPlayer.getTokens());
                    return;
                } else {
                    callback.onFailure(null, gbPlayer.getTokens());
                    return;
                }
            } else {
                // player data is being looked up... Wait a bit and try again
                Bukkit.getScheduler().runTaskLater(plugin, () -> takeToken(player, count, callback), 2);
                return;
            }
        }

        plugin.getDataBase().getToken(player.getUniqueId(), new DataBase.Callback<Integer>() {
            @Override
            public void onSuccess(Integer done) {
                if(done >= count){
                    plugin.getDataBase().setToken(player.getUniqueId(), done - count);
                    callback.onSuccess(done - count);
                    return;
                } else {
                    callback.onFailure(null, done);
                    return;
                }
            }

            @Override
            public void onFailure(@Nullable Throwable throwable, @Nullable Integer value) {
                callback.onFailure(throwable, null);
                return;
            }
        });
    }

    /**
     * Get token count for online/offline player
     *
     * This is done async and will return the value via Callback
     * @param player to look up
     * @param callback to call with loaded token count
     */
    public void getToken(OfflinePlayer player, DataBase.Callback<Integer> callback){
        Validate.notNull(player, "Player cannot be null!");

        // handle cached players
        GBPlayer gbPlayer = plugin.getPluginManager().getPlayer(player.getUniqueId());
        if(gbPlayer != null){
            callback.onSuccess(gbPlayer.getTokens());
        }

        plugin.getDataBase().getToken(player.getUniqueId(),callback);
    }

    /**
     * Get token count for a loaded GBPlayer
     *
     * First check via GameBoxAPI#isGBPlayer(UUID)
     * @param player to check
     * @return token count
     */
    public int getCachedToken(Player player){
        Validate.notNull(player, "Player cannot be null!");
        Validate.isTrue(isGBPlayer(player), "Player has to be GBPlayer with loaded data! Check via GameBoxAPI#isGBPlayer(UUID)");

        // handle cached player
        return plugin.getPluginManager().getPlayer(player.getUniqueId()).getTokens();
    }

    /**
     * Check for a player being a cached gamebox player and having his data loaded
     *
     * If this returns true, you can get the player and manipulate his tokens...
     * @param uuid to look up
     * @return player is GBPlayer and has his data loaded
     */
    public boolean isGBPlayer(UUID uuid){
        GBPlayer gbPlayer = plugin.getPluginManager().getPlayer(uuid);
        return gbPlayer != null && gbPlayer.isLoaded();
    }

    /**
     * Check for a player being a cached gamebox player and having his data loaded
     *
     * If this returns true, you can get the player and manipulate his tokens...
     * @param player to look up
     * @return player is GBPlayer and has his data loaded
     */
    public boolean isGBPlayer(OfflinePlayer player){
        return isGBPlayer(player.getUniqueId());
    }




}
