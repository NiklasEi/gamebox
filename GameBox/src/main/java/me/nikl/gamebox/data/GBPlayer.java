package me.nikl.gamebox.data;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Niklas on 17.02.2017.
 *
 */
public class GBPlayer {
    private UUID uuid;
    private boolean playSounds = true;
    private GameBox plugin;
    private DataBase dataBase;

    private int tokens;

    public GBPlayer(GameBox plugin, UUID uuid){
        this.uuid = uuid;
        this.plugin = plugin;
        this.dataBase = plugin.getDataBase();

        // save current name of player for easier management (only in file!)
        if(!GameBoxSettings.useMysql) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                dataBase.set(uuid.toString(), "name", player.getName());
            }
        }

        loadData();
    }

    private void loadData() {
        dataBase.loadPlayer(this, true);
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

    public int getTokens(){
        return this.tokens;
    }

    public void setTokens(int newTokens){
        this.tokens = newTokens;
        plugin.getPluginManager().getGuiManager().updateTokens(this);
    }

    public void remove() {
        //TOdO
        // remove special inventories and save any data
        // after this call this object will be removed from player map
        plugin.getPluginManager().getGuiManager().removePlayer(this.uuid);
        save();
    }

    public void save(){
        save(false);
    }

    public void save(boolean async) {
        dataBase.savePlayer(this, async);
    }
}
