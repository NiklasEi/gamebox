package me.nikl.gamebox.games.matchit;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.game.manager.GameManager;
import me.nikl.gamebox.game.rules.GameRule;
import me.nikl.gamebox.game.exceptions.GameStartException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Niklas Eicker
 */
public class MIGameManager implements GameManager {
    private MatchIt matchIt;
    private GameBox gameBox;

    private HashMap<String, MIGameRule> gameRules = new HashMap<>();
    private HashMap<UUID, MIGame> games = new HashMap<>();

    public MIGameManager(MatchIt matchIt) {
        this.matchIt = matchIt;
        this.gameBox = matchIt.getGameBox();
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        MIGame game = games.get(event.getWhoClicked().getUniqueId());
        if (game == null) return;
        game.onClick(event);
        return;
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!games.keySet().contains(event.getPlayer().getUniqueId())) return;
        // do same stuff as on removeFromGame()
        removeFromGame(event.getPlayer().getUniqueId());
    }

    @Override
    public boolean isInGame(UUID uuid) {
        return games.keySet().contains(uuid);
    }

    @Override
    public void startGame(Player[] players, boolean playSounds, String... args) throws GameStartException {
        MIGameRule rule = gameRules.get(args[0]);
        if (rule == null) throw new GameStartException(GameStartException.Reason.ERROR);
        if (!matchIt.payIfNecessary(players[0], rule.getCost())) {
            throw new GameStartException(GameStartException.Reason.NOT_ENOUGH_MONEY);
        }
        games.put(players[0].getUniqueId(),
                new MIGame(matchIt, players[0]
                        , playSounds && matchIt.getSettings().isPlaySounds()
                        , rule));
        return;
    }

    @Override
    public void removeFromGame(UUID uuid) {
        MIGame game = games.get(uuid);
        if (game == null) return;
        game.cancel();
        game.onGameEnd();
        games.remove(uuid);
    }

    @Override
    public void loadGameRules(ConfigurationSection buttonSec, String buttonID) {
        double cost = buttonSec.getDouble("cost", 0.);
        boolean saveStats = buttonSec.getBoolean("saveStats", false);
        double timeVisible = buttonSec.getDouble("timeVisible", 1.5);
        int token = buttonSec.getInt("token", 0);
        double money = buttonSec.getDouble("reward", 0.);
        MatchIt.GridSize gridSize;
        try {
            gridSize = MatchIt.GridSize.valueOf(buttonSec.getString("size", "medium").toUpperCase());
        } catch (IllegalArgumentException exception) {
            gridSize = MatchIt.GridSize.MIDDLE;
        }
        MIGameRule rule = new MIGameRule(saveStats, cost, buttonID, gridSize, timeVisible, money, token);
        gameRules.put(buttonID, rule);
    }

    @Override
    public Map<String, ? extends GameRule> getGameRules() {
        return gameRules;
    }

    public GameBox getGameBox() {
        return gameBox;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
