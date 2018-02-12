package me.nikl.gamebox.games.connectfour;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.data.database.DataBase;
import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.games.GameManager;
import me.nikl.gamebox.games.GameRule;
import me.nikl.gamebox.nms.NMSUtil;
import me.nikl.gamebox.utility.Permission;
import me.nikl.gamebox.utility.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Niklas Eicker
 *
 * ConnectFour GameManager
 */

public class CFGameManager implements GameManager {
    private ConnectFour connectFour;

    private Map<UUID, CFGame> games = new HashMap<>();
    private CFLanguage lang;

    private NMSUtil nms;

    private DataBase statistics;

    private Map<Integer, ItemStack> chips = new HashMap<>();


    private Map<String, CFGameRules> gameRules;



    public CFGameManager(ConnectFour connectFour){
        this.connectFour = connectFour;
        this.lang = (CFLanguage) connectFour.getGameLang();

        this.nms = connectFour.getGameBox().getNMS();

        this.statistics = connectFour.getGameBox().getDataBase();

        this.gameRules = new HashMap<>();

        loadChips();
    }

    @Override
    public void loadGameRules(ConfigurationSection buttonSec, String buttonID) {
        double cost = buttonSec.getDouble("cost", 0.);
        double reward = buttonSec.getDouble("reward", 0.);
        int tokens = buttonSec.getInt("tokens", 0);
        boolean saveStats = buttonSec.getBoolean("saveStats", false);
        int timePerMove = buttonSec.getInt("timePerMove", 30);
        if(timePerMove < 1){
            timePerMove = 30;
        }
        int minNumberOfPlayedChips = buttonSec.getInt("minNumberOfPlayedChips", 7);
        gameRules.put(buttonID, new CFGameRules(buttonID, timePerMove, minNumberOfPlayedChips
                , cost, reward, tokens, saveStats));
    }

    @Override
    public Map<String, ? extends GameRule> getGameRules() {
        return this.gameRules;
    }

    private void loadChips() {
        if(!connectFour.getConfig().isConfigurationSection("chips")){
            Bukkit.getConsoleSender().sendMessage(lang.PREFIX + ChatColor.RED +" the configuration section 'chips' can not be found!");
            Bukkit.getConsoleSender().sendMessage(lang.PREFIX + ChatColor.RED +" using two default chips");

            chips.put(0, new ItemStack(Material.BLAZE_POWDER));
            chips.put(1, new ItemStack(Material.DIAMOND));
            return;
        }

        ItemMeta meta;
        String displayName;
        List<String> lore;
        ItemStack chipStack;

        ConfigurationSection chipsSection = connectFour.getConfig().getConfigurationSection("chips");

        int count = 0;
        for(String key: chipsSection.getKeys(false)){

            chipStack = me.nikl.gamebox.utility.ItemStackUtil.getItemStack(chipsSection.getString(key + ".materialData"));

            if(chipStack == null){
                Bukkit.getConsoleSender().sendMessage(lang.PREFIX + ChatColor.RED +" problem loading chip: "+key);
                continue;
            }
            meta = chipStack.getItemMeta();

            if(chipsSection.isString(key + ".displayName")){
                displayName = StringUtil.color(chipsSection.getString(key + ".displayName"));
                meta.setDisplayName(displayName);
            }

            if(chipsSection.isList(key + ".lore")){
                meta.setLore(StringUtil.color(chipsSection.getStringList(key + ".lore")));
            }

            chipStack.setItemMeta(meta);

            chips.put(count, chipStack.clone());
            count++;
        }
        if (count < 2){
            Bukkit.getConsoleSender().sendMessage(lang.PREFIX + ChatColor.RED +" not enough chips set in config!");
            Bukkit.getConsoleSender().sendMessage(lang.PREFIX + ChatColor.RED +" define at least 2! Using two defaults now.");

            chips.put(0, new ItemStack(Material.BLAZE_POWDER));
            chips.put(1, new ItemStack(Material.DIAMOND));
            return;
        }
    }


    @Override
    public boolean onInventoryClick(InventoryClickEvent inventoryClickEvent) {
        CFGame game = getGame(inventoryClickEvent.getWhoClicked().getUniqueId());
        if(game == null) return false;

        if(inventoryClickEvent.getCurrentItem() != null && inventoryClickEvent.getCurrentItem().getType() != Material.AIR) return false;

        game.onClick(inventoryClickEvent);

        return true;
    }

    @Override
    public boolean onInventoryClose(InventoryCloseEvent inventoryCloseEvent) {

        if(!isInGame(inventoryCloseEvent.getPlayer().getUniqueId())){
            return false;
        }

        CFGame game = getGame(inventoryCloseEvent.getPlayer().getUniqueId());
        boolean firstClosed = inventoryCloseEvent.getPlayer().getUniqueId().equals(game.getFirstUUID());
        Player winner = firstClosed?game.getSecond():game.getFirst();
        Player loser = firstClosed?game.getFirst():game.getSecond();
        if((!firstClosed && game.getFirst() == null) || (firstClosed && game.getSecond() == null)){
            games.remove(game.getFirstUUID());
            return true;
        }
        removeFromGame(firstClosed, winner, loser, game);
        return true;
    }

    private void removeFromGame(boolean firstClosed, Player winner, Player loser, CFGame game) {
        // make sure the player is not counted as in connectFour anymore
        if(game.getState() != CFGameState.FINISHED) game.onRemove(firstClosed);

        if(firstClosed){
            game.setFirst(null);
        } else {
            game.setSecond(null);
        }


        if(game.getState() != CFGameState.FINISHED) {
            game.cancel();

            if(this.connectFour.getSettings().isEconEnabled() && game.getPlayedChips() >= game.getRule().getMinNumberOfPlayedChips()){
                if(!winner.hasPermission(Permission.BYPASS_ALL.getPermission()) && !winner.hasPermission(Permission.BYPASS_GAME.getPermission(connectFour.getGameID()))){
                    GameBox.econ.depositPlayer(winner, game.getRule().getReward());
                    winner.sendMessage(StringUtil.color(lang.PREFIX + lang.GAME_WON_MONEY_GAVE_UP.replaceAll("%reward%", game.getRule().getReward()+"").replaceAll("%loser%", loser.getName())));
                } else {
                    winner.sendMessage(StringUtil.color(lang.PREFIX + lang.GAME_OTHER_GAVE_UP.replaceAll("%loser%", loser.getName())));
                }
            } else if(connectFour.getSettings().isEconEnabled()){
                if(!winner.hasPermission(Permission.BYPASS_ALL.getPermission()) && !winner.hasPermission(Permission.BYPASS_GAME.getPermission(connectFour.getGameID()))){
                    GameBox.econ.depositPlayer(winner, game.getRule().getCost());
                    winner.sendMessage(StringUtil.color(lang.PREFIX + lang.GAME_WON_MONEY_GAVE_UP.replaceAll("%reward%", game.getRule().getCost()+"").replaceAll("%loser%", loser.getName())));
                } else {
                    winner.sendMessage(StringUtil.color(lang.PREFIX + lang.GAME_OTHER_GAVE_UP.replaceAll("%loser%", loser.getName())));
                }
            } else {
                winner.sendMessage(StringUtil.color(lang.PREFIX + lang.GAME_OTHER_GAVE_UP.replaceAll("%loser%", loser.getName())));
            }

            loser.sendMessage(StringUtil.color(lang.PREFIX + lang.GAME_GAVE_UP));


            nms.updateInventoryTitle(winner, lang.TITLE_WON);
            game.setState(CFGameState.FINISHED);

            // pay out token and save stats in enabled
            onGameEnd(winner, loser, game.getRule().getKey(), game.getPlayedChips());
        }
    }

    @Override
    public boolean isInGame(UUID uuid) {
        for(CFGame game : games.values()){
            if((game.getFirstUUID().equals(uuid) && game.getFirst() != null ) || (game.getSecondUUID().equals(uuid) && game.getSecond() != null)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int startGame(Player[] players, boolean playSounds, String... args) {

        CFGameRules rule = gameRules.get(args[0]);
        if(rule == null){
            return GameBox.GAME_NOT_STARTED_ERROR;
        }

        double cost = rule.getCost();

        boolean firstCanPay = true;

        if (connectFour.getSettings().isEconEnabled() && !players[0].hasPermission(Permission.BYPASS_ALL.getPermission()) && !players[0].hasPermission(Permission.BYPASS_GAME.getPermission(connectFour.getGameID())) && cost > 0.0) {
            if (GameBox.econ.getBalance(players[0]) >= cost) {

            } else {
                players[0].sendMessage(StringUtil.color(lang.PREFIX + lang.GAME_NOT_ENOUGH_MONEY));
                firstCanPay = false;
            }
        }


        if (connectFour.getSettings().isEconEnabled() && !players[1].hasPermission(Permission.BYPASS_ALL.getPermission()) && !players[1].hasPermission(Permission.BYPASS_GAME.getPermission(connectFour.getGameID())) && cost > 0.0) {
            if (GameBox.econ.getBalance(players[1]) >= cost) {

            } else {
                players[1].sendMessage(StringUtil.color(lang.PREFIX + lang.GAME_NOT_ENOUGH_MONEY));
                if(firstCanPay){
                    // only second player cannot pay
                    return GameBox.GAME_NOT_ENOUGH_MONEY_2;
                } else {
                    // both players cannot pay
                    return GameBox.GAME_NOT_ENOUGH_MONEY;
                }
            }
        }

        if(!firstCanPay){
            // only first player cannot pay
            return GameBox.GAME_NOT_ENOUGH_MONEY_1;
        }

        // both players can pay!


        if (connectFour.getSettings().isEconEnabled()) {
            GameBox.econ.withdrawPlayer(players[0], cost);
            players[0].sendMessage(StringUtil.color(lang.PREFIX + lang.GAME_PAYED.replaceAll("%cost%", String.valueOf(cost))));


            GameBox.econ.withdrawPlayer(players[1], cost);
            players[1].sendMessage(StringUtil.color(lang.PREFIX + lang.GAME_PAYED.replaceAll("%cost%", String.valueOf(cost))));
        }

        games.put(players[0].getUniqueId(), new CFGame(gameRules.get(args[0]), connectFour, playSounds && connectFour.getSettings().isPlaySounds(), players, chips));
        return GameBox.GAME_STARTED;
    }

    @Override
    public void removeFromGame(UUID uuid) {
        if(!isInGame(uuid)){
            return;
        }

        CFGame game = getGame(uuid);
        boolean firstClosed = uuid.equals(game.getFirstUUID());
        Player winner = firstClosed?game.getSecond():game.getFirst();
        Player loser = firstClosed?game.getFirst():game.getSecond();
        if((!firstClosed && game.getFirst() == null) || (firstClosed && game.getSecond() == null)){
            games.remove(game.getFirstUUID());
            return;
        }
        removeFromGame(firstClosed, winner, loser, game);
        return;
    }

    private CFGame getGame(UUID uuid){
        for (CFGame game : games.values()){
            if(game.getFirstUUID().equals(uuid) || game.getSecondUUID().equals(uuid)){
                return game;
            }
        }
        return null;
    }

    public void onGameEnd(Player winner, Player loser, String key, int chipsPlayed) {

        CFGameRules rule = gameRules.get(key);

        if(rule.isSaveStats()){
            addWin(winner.getUniqueId(), rule.getKey());
        }
        if(rule.getTokens() > 0 && chipsPlayed >= rule.getMinNumberOfPlayedChips()){
            connectFour.getGameBox().wonTokens(winner.getUniqueId(), rule.getTokens(), connectFour.getGameID());
        }
    }

    public void addWin(UUID uuid, String key){
        statistics.addStatistics(uuid, connectFour.getGameID(), key, 1., SaveType.WINS);
    }
}
