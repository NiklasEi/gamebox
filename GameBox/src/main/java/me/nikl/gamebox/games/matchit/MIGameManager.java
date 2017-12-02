package me.nikl.gamebox.games.matchit;

import me.nikl.gamebox.games.GameManager;
import me.nikl.gamebox.games.GameRule;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Map;
import java.util.UUID;

/**
 * Created by nikl on 02.12.17.
 */
public class MIGameManager implements GameManager {
    private MatchIt matchIt;

    public MIGameManager (MatchIt matchIt){
        this.matchIt = matchIt;
    }

    @Override
    public boolean onInventoryClick(InventoryClickEvent event) {
        return false;
    }

    @Override
    public boolean onInventoryClose(InventoryCloseEvent event) {
        return false;
    }

    @Override
    public boolean isInGame(UUID uuid) {
        return false;
    }

    @Override
    public int startGame(Player[] players, boolean playSounds, String... args) {
        return 0;
    }

    @Override
    public void removeFromGame(UUID uuid) {

    }

    @Override
    public void loadGameRules(ConfigurationSection buttonSec, String buttonID) {

    }

    @Override
    public Map<String, ? extends GameRule> getGameRules() {
        return null;
    }
}
