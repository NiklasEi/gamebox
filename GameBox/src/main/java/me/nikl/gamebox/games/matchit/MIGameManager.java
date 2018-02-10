package me.nikl.gamebox.games.matchit;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.games.GameManager;
import me.nikl.gamebox.games.GameRule;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by nikl on 02.12.17.
 *
 */
public class MIGameManager implements GameManager {
    private MatchIt matchIt;
    private GameBox gameBox;

    private HashMap<String, MIGameRule> gameRules = new HashMap<>();
    private HashMap<UUID, MIGame> games = new HashMap<>();

    public MIGameManager (MatchIt matchIt){
        this.matchIt = matchIt;
        this.gameBox = matchIt.getGameBox();
    }

    @Override
    public boolean onInventoryClick(InventoryClickEvent event) {
        MIGame game = games.get(event.getWhoClicked().getUniqueId());
        if(game == null) return false;

        game.onClick(event);
        return true;
    }

    @Override
    public boolean onInventoryClose(InventoryCloseEvent event) {
        if(!games.keySet().contains(event.getPlayer().getUniqueId())) return false;

        // do same stuff as on removeFromGame()
        removeFromGame(event.getPlayer().getUniqueId());
        return true;
    }

    @Override
    public boolean isInGame(UUID uuid) {
        return games.keySet().contains(uuid);
    }

    @Override
    public int startGame(Player[] players, boolean playSounds, String... args) {
        MIGameRule rule = gameRules.get(args[0]);
        if(rule == null) return GameBox.GAME_NOT_STARTED_ERROR;
        games.put(players[0].getUniqueId(),
                new MIGame(matchIt, players[0]
                        , playSounds && matchIt.getSettings().isPlaySounds()
                        , rule));
        return GameBox.GAME_STARTED;
    }

    @Override
    public void removeFromGame(UUID uuid) {
        MIGame game = games.get(uuid);

        if(game == null) return;

        game.cancel();
        game.onGameEnd();

        games.remove(uuid);
    }

    @Override
    public void loadGameRules(ConfigurationSection buttonSec, String buttonID) {
        double cost = buttonSec.getDouble("cost", 0.);
        boolean saveStats = buttonSec.getBoolean("saveStats", false);
        double timeVisible = buttonSec.getDouble("timeVisible", 1.5);
        MatchIt.GridSize gridSize;
        try {
            gridSize = MatchIt.GridSize.valueOf(buttonSec.getString("size", "medium").toUpperCase());
        } catch (IllegalArgumentException exception){
            gridSize = MatchIt.GridSize.MIDDLE;
        }

        gameRules.put(buttonID, new MIGameRule(saveStats, cost, buttonID, gridSize, timeVisible));
    }

    @Override
    public Map<String, ? extends GameRule> getGameRules() {
        return gameRules;
    }

    public GameBox getGameBox() {
        return gameBox;
    }
}
