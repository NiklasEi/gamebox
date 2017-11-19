package me.nikl.gamebox.games.cookieclicker;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.data.Statistics;
import me.nikl.gamebox.games.GameRule;
import me.nikl.gamebox.data.SaveType;
import me.nikl.gamebox.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.io.*;
import java.util.*;
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
    private Statistics statistics;
    private CCLanguage lang;

    private File savesFile;
    private FileConfiguration saves;

    public CCGameManager(CookieClicker game){
        this.game = game;
        this.statistics = game.getGameBox().getStatistics();
        this.lang = (CCLanguage) game.getGameLang();

        savesFile = new File(game.getDataFolder().toString() + File.separatorChar + "saves.yml");
        if(!savesFile.exists()){
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


    public boolean onInventoryClick(InventoryClickEvent inventoryClickEvent) {
        if(!games.keySet().contains(inventoryClickEvent.getWhoClicked().getUniqueId())) return false;

        CCGame game = games.get(inventoryClickEvent.getWhoClicked().getUniqueId());

        game.onClick(inventoryClickEvent);
        return true;
    }


    public boolean onInventoryClose(InventoryCloseEvent inventoryCloseEvent) {
        if(!games.keySet().contains(inventoryCloseEvent.getPlayer().getUniqueId())) return false;

        // do same stuff as on removeFromGame()
        removeFromGame(inventoryCloseEvent.getPlayer().getUniqueId());
        return true;
    }


    public boolean isInGame(UUID uuid) {
        return games.containsKey(uuid);
    }


    public int startGame(Player[] players, boolean playSounds, String... strings) {
        if (strings.length != 1) {
            Bukkit.getLogger().log(Level.WARNING, " unknown number of arguments to start a game: " + Arrays.asList(strings));
            return GameBox.GAME_NOT_STARTED_ERROR;
        }

        CCGameRules rule = gameRules.get(strings[0]);

        if (rule == null) {
            Bukkit.getLogger().log(Level.WARNING, " unknown gametype: " + Arrays.asList(strings));
            return GameBox.GAME_NOT_STARTED_ERROR;
        }

        if (!game.pay(players[0], rule.getCost())) {
            return GameBox.GAME_NOT_ENOUGH_MONEY;
        }

        if(saves.isConfigurationSection(rule.getKey() + "." + players[0].getUniqueId())) {
            games.put(players[0].getUniqueId(), new CCGame(rule, game, players[0], playSounds, saves.getConfigurationSection(rule.getKey() + "." + players[0].getUniqueId())));
        } else {
            games.put(players[0].getUniqueId(), new CCGame(rule, game, players[0], playSounds, null));
        }

        return GameBox.GAME_STARTED;
    }


    public void removeFromGame(UUID uuid) {

        CCGame game = games.get(uuid);

        if(game == null) return;

        game.cancel();
        game.onGameEnd();

        games.remove(uuid);
    }

    public void loadGameRules(ConfigurationSection buttonSec, String buttonID) {
        int moveCookieAfterClicks = buttonSec.getInt("moveCookieAfterClicks", 0);
        if(moveCookieAfterClicks < 1) moveCookieAfterClicks = 0;

        double cost = buttonSec.getDouble("cost", 0.);
        boolean saveStats = buttonSec.getBoolean("saveStats", false);

        gameRules.put(buttonID, new CCGameRules(buttonID, cost, moveCookieAfterClicks, saveStats));
    }

    @Override
    public Map<String, ? extends GameRule> getGameRules() {
        return gameRules;
    }

    public void saveGame(CCGameRules rule, UUID uuid, Map<String, Double> cookies, Map<String, Integer> productions, List<Integer> upgrades) {

        for(String key : cookies.keySet()){
            saves.set(rule.getKey() + "." + uuid.toString() + "." + "cookies" + "." + key, Math.floor(cookies.get(key)));
        }

        for(String production : productions.keySet()){
            saves.set(rule.getKey() + "." + uuid.toString() + "." + "productions" + "." + production, productions.get(production));
        }

        saves.set(rule.getKey() + "." + uuid.toString() + "." + "upgrades", upgrades);
        statistics.addStatistics(uuid, game.getGameID(), rule.getKey(), Math.floor(cookies.get("total")), SaveType.HIGH_NUMBER_SCORE);
    }

    public void onShutDown(){
        // save all open games!
        for(CCGame game : games.values()){
            game.cancel();
            game.onGameEnd();
        }

        try {
            saves.save(savesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void restart(String key){
        CCGameRules rule = (CCGameRules) gameRules.get(key);
        if(rule == null) return;

        Set<Player> players = new HashSet<>();

        for(CCGame game : games.values()){
            if(game.getRule().getKey().equals(key)){
                players.add(game.getPlayer());
            }
        }

        for(Player player : players){
            if(player != null) player.closeInventory();
        }

        // here pay out any rewards and gather the top players


        // ToDo: update GameBox and allow for removing of statistics

        // delete saves
        if (saves.isConfigurationSection(key)){
            saves.set(key, null);
        }

    }
}
