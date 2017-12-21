package me.nikl.gamebox;

import me.nikl.gamebox.data.DataBase;
import me.nikl.gamebox.data.GBPlayer;
import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

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

        // handle cached online players
        cachedPlayer:
        if(player.isOnline()) {
            GBPlayer gbPlayer = plugin.getPluginManager().getPlayer(player.getUniqueId());
            if (gbPlayer == null) {
                break cachedPlayer;
            }
            gbPlayer.setTokens(gbPlayer.getTokens() + count);
            return;
        }

        // handle offline or not cached players
        plugin.getDataBase().getToken(player.getUniqueId(), new DataBase.Callback<Integer>() {
            @Override
            public void onSuccess(Integer done) {
                plugin.getDataBase().setToken(player.getUniqueId(), done + count);
            }

            @Override
            public void onFailure(Throwable throwable) {
                plugin.getLogger().warning(" Failed to handle API call giveToken for player: " + player.getName());
            }
        });
    }

    public void setToken(OfflinePlayer player, int count){
        Validate.notNull(player, "Player cannot be null!");
        Validate.isTrue(count >= 0, "token count must be greater then or equal 0");

        // handle cached online players
        cachedPlayer:
        if(player.isOnline()) {
            GBPlayer gbPlayer = plugin.getPluginManager().getPlayer(player.getUniqueId());
            if (gbPlayer == null) {
                break cachedPlayer;
            }
            gbPlayer.setTokens(count);
            return;
        }
        plugin.getDataBase().setToken(player.getUniqueId(), count);
    }

    /**
     * Take token from a specified player
     * @param player
     * @param count token to take
     *              ToDo!?
     * @throws IllegalArgumentException if the player does not have enough token.
     * Check with {@link #getToken(Player)} before attempting to take any token.
     */
    public void takeToken(OfflinePlayer player, int count){
        Validate.notNull(player, "Player cannot be null!");
        Validate.isTrue(count > 0, "token to take must be greater then 0");
        // handle cached online players

        cachedPlayer:
        if(player.isOnline()) {
            GBPlayer gbPlayer = plugin.getPluginManager().getPlayer(player.getUniqueId());
            if (gbPlayer == null) {
                break cachedPlayer;
            }
            if(gbPlayer.getTokens() >= count){
                gbPlayer.setTokens(gbPlayer.getTokens() - count);
                return;
            } else {
                throw new IllegalArgumentException("player does not have enough token!");
            }
        }

        // ToDo: handle offline or not cached players
    }

    public int getToken(Player player){
        Validate.notNull(player, "Player cannot be null!");

        // handle cached players
        GBPlayer gbPlayer = plugin.getPluginManager().getPlayer(player.getUniqueId());
        if(gbPlayer != null){
            return gbPlayer.getTokens();
        }

        plugin.getDataBase().getToken(player.getUniqueId(), new DataBase.Callback<Integer>() {
            @Override
            public void onSuccess(Integer done) {

            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        });

        // ToDo: return token count...
        return 0;
    }




}
