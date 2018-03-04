package me.nikl.gamebox.games.cookieclicker;

import me.nikl.gamebox.data.database.DataBase;
import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.games.GameManager;
import me.nikl.gamebox.games.GameRule;
import me.nikl.gamebox.games.exceptions.GameStartException;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created by Niklas.
 *
 * GameManager
 */

public class CCGameManager implements GameManager {
    private CookieClicker game;

    private Map<UUID, CCGame> games = new HashMap<>();

    private Map<String, CCGameRules> gameRules = new HashMap<>();
    private DataBase statistics;
    private CCLanguage lang;

    private File savesFile;
    private FileConfiguration saves;

    public CCGameManager(CookieClicker game) {
        this.game = game;
        this.statistics = game.getGameBox().getDataBase();
        this.lang = (CCLanguage) game.getGameLang();

        savesFile = new File(game.getDataFolder().toString() + File.separatorChar + "saves.yml");
        if (!savesFile.exists()) {
            try {
                savesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            this.saves = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(savesFile), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void onInventoryClick(InventoryClickEvent inventoryClickEvent) {
        if (!games.keySet().contains(inventoryClickEvent.getWhoClicked().getUniqueId())) return;
        CCGame game = games.get(inventoryClickEvent.getWhoClicked().getUniqueId());
        game.onClick(inventoryClickEvent);
    }


    public void onInventoryClose(InventoryCloseEvent inventoryCloseEvent) {
        if (!games.keySet().contains(inventoryCloseEvent.getPlayer().getUniqueId())) return;

        // do same stuff as on removeFromGame()
        removeFromGame(inventoryCloseEvent.getPlayer().getUniqueId());
    }


    public boolean isInGame(UUID uuid) {
        return games.containsKey(uuid);
    }


    public void startGame(Player[] players, boolean playSounds, String... strings) throws GameStartException {
        if (strings.length != 1) {
            Bukkit.getLogger().log(Level.WARNING, " unknown number of arguments to start a game: " + Arrays.asList(strings));
            throw new GameStartException(GameStartException.Reason.ERROR);
        }
        CCGameRules rule = gameRules.get(strings[0]);
        if (rule == null) {
            Bukkit.getLogger().log(Level.WARNING, " unknown gametype: " + Arrays.asList(strings));
            throw new GameStartException(GameStartException.Reason.ERROR);
        }
        if (!game.payIfNecessary(players[0], rule.getCost())) {
            throw new GameStartException(GameStartException.Reason.NOT_ENOUGH_MONEY);
        }
        if (saves.isConfigurationSection(rule.getKey() + "." + players[0].getUniqueId())) {
            games.put(players[0].getUniqueId(), new CCGame(rule, game, players[0], playSounds, saves.getConfigurationSection(rule.getKey() + "." + players[0].getUniqueId())));
        } else {
            games.put(players[0].getUniqueId(), new CCGame(rule, game, players[0], playSounds, null));
        }
        return;
    }


    public void removeFromGame(UUID uuid) {
        CCGame game = games.get(uuid);
        if (game == null) return;
        game.cancel();
        game.onGameEnd();
        games.remove(uuid);
    }

    public void loadGameRules(ConfigurationSection buttonSec, String buttonID) {
        int moveCookieAfterClicks = buttonSec.getInt("moveCookieAfterClicks", 0);
        if (moveCookieAfterClicks < 1) moveCookieAfterClicks = 0;
        double cost = buttonSec.getDouble("cost", 0.);
        boolean saveStats = buttonSec.getBoolean("saveStats", false);
        CCGameRules rules = new CCGameRules(buttonID, cost, moveCookieAfterClicks, saveStats);
        if (buttonSec.isConfigurationSection("rewards")) {
            ConfigurationSection rewards = buttonSec.getConfigurationSection("rewards");
            for (String key : rewards.getKeys(false)) {
                try {
                    int minScore = Integer.valueOf(key);
                    int token = rewards.getInt(key + ".token", 0);
                    double money = rewards.getDouble(key + ".money", 0.);
                    rules.addMoneyReward(minScore, money);
                    rules.addTokenReward(minScore, token);
                } catch (NumberFormatException exception) {
                    continue;
                }
            }
        }
        gameRules.put(buttonID, rules);
    }

    @Override
    public Map<String, ? extends GameRule> getGameRules() {
        return gameRules;
    }

    public void saveGame(CCGameRules rule, UUID uuid, Map<String, Double> cookies, Map<String, Integer> productions, List<Integer> upgrades) {
        for (String key : cookies.keySet()) {
            saves.set(rule.getKey() + "." + uuid.toString() + "." + "cookies" + "." + key, Math.floor(cookies.get(key)));
        }
        for (String production : productions.keySet()) {
            saves.set(rule.getKey() + "." + uuid.toString() + "." + "productions" + "." + production, productions.get(production));
        }
        saves.set(rule.getKey() + "." + uuid.toString() + "." + "upgrades", upgrades);
        statistics.addStatistics(uuid, game.getGameID(), rule.getKey(), Math.floor(cookies.get("total")), SaveType.HIGH_NUMBER_SCORE);
    }

    public void onShutDown() {
        // save all open games!
        for (CCGame game : games.values()) {
            game.cancel();
            game.onGameEnd();
        }
        try {
            saves.save(savesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void restart(String key) {
        CCGameRules rule = (CCGameRules) gameRules.get(key);
        if (rule == null) return;
        Set<Player> players = new HashSet<>();
        for (CCGame game : games.values()) {
            if (game.getRule().getKey().equals(key)) {
                players.add(game.getPlayer());
            }
        }
        for (Player player : players) {
            if (player != null) player.closeInventory();
        }
        // here pay out any rewards and gather the top players

        // ToDo: update GameBox and allow for removing of statistics

        // delete saves
        if (saves.isConfigurationSection(key)) {
            saves.set(key, null);
        }
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
