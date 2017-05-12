package me.nikl.gamebox;

import me.nikl.gamebox.data.Statistics;
import me.nikl.gamebox.players.GBPlayer;
import org.bukkit.ChatColor;
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


    public boolean giveToken(Player player, int count){
        if(player == null) return false;
        if(count < 0) return false;
        // handle cached online players
        cachedPlayer:
        if(player.isOnline()) {
            GBPlayer gbPlayer = plugin.getPluginManager().getPlayer(player.getUniqueId());
            if (gbPlayer == null) {
                break cachedPlayer;
            }
            gbPlayer.setTokens(gbPlayer.getTokens() + count);
            return true;
        }

        // handle offline or not cached players
        if(!plugin.getStatistics().isSet(player.getUniqueId().toString())) {
            plugin.getStatistics().set(player.getUniqueId().toString(), Statistics.TOKEN_PATH, count);
            return true;
        } else {
            int oldCount = plugin.getStatistics().getInt(player.getUniqueId(), Statistics.TOKEN_PATH, 0);
            plugin.getStatistics().set(player.getUniqueId().toString(), Statistics.TOKEN_PATH, count + oldCount);
            return true;
        }
    }

    public boolean setToken(Player player, int count){
        if(player == null) return false;
        if(count < 0) return false;
        // handle cached online players
        cachedPlayer:
        if(player.isOnline()) {
            GBPlayer gbPlayer = plugin.getPluginManager().getPlayer(player.getUniqueId());
            if (gbPlayer == null) {
                break cachedPlayer;
            }
            gbPlayer.setTokens(count);
            return true;
        }
        plugin.getStatistics().set(player.getUniqueId().toString(), Statistics.TOKEN_PATH, count);
        return true;
    }

    public boolean takeToken(Player player, int count){
        if(player == null) return false;
        if(count < 0) return false;
        // handle cached online players

        cachedPlayer:
        if(player.isOnline()) {
            GBPlayer gbPlayer = plugin.getPluginManager().getPlayer(player.getUniqueId());
            if (gbPlayer == null) {
                break cachedPlayer;
            }
            if(gbPlayer.getTokens() >= count){
                gbPlayer.setTokens(gbPlayer.getTokens() - count);
                return true;
            } else {
                return false;
            }
        }

        // handle offline or not cached players
        if(plugin.getStatistics().isSet(player.getUniqueId().toString())) {

            int oldCount = plugin.getStatistics().getInt(player.getUniqueId(), Statistics.TOKEN_PATH, 0);
            if(oldCount >= count){
                plugin.getStatistics().set(player.getUniqueId().toString(), Statistics.TOKEN_PATH, oldCount - count);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public int getToken(Player player){
        if(player == null) return 0;

        // handle cached players
        GBPlayer gbPlayer = plugin.getPluginManager().getPlayer(player.getUniqueId());
        if(gbPlayer != null){
            return gbPlayer.getTokens();
        }

        return plugin.getStatistics().getInt(player.getUniqueId(), Statistics.TOKEN_PATH, 0);
    }




}
