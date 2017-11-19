package me.nikl.connectfour;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Permissions;
import me.nikl.gamebox.data.SaveType;
import me.nikl.gamebox.data.Statistics;
import me.nikl.gamebox.game.IGameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Niklas on 14.04.2017.
 *
 * 2048s GameManager
 */

public class GameManager implements IGameManager {
    private Main plugin;

    private Map<UUID, Game> games = new HashMap<>();
    private Language lang;

    private Statistics statistics;

    private Map<Integer, ItemStack> chips = new HashMap<>();


    private Map<String,GameRules> gameTypes;



    public GameManager(Main plugin){
        this.plugin = plugin;
        this.lang = plugin.lang;

        this.statistics = plugin.gameBox.getStatistics();

        loadChips();
    }

    private void loadChips() {
        if(!plugin.getConfig().isConfigurationSection("chips")){
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

        ConfigurationSection chipsSection = plugin.getConfig().getConfigurationSection("chips");

        int count = 0;
        for(String key: chipsSection.getKeys(false)){

            chipStack = me.nikl.gamebox.util.ItemStackUtil.getItemStack(chipsSection.getString(key + ".materialData"));

            if(chipStack == null){
                Bukkit.getConsoleSender().sendMessage(lang.PREFIX + ChatColor.RED +" problem loading chip: "+key);
                continue;
            }
            meta = chipStack.getItemMeta();

            if(chipsSection.isString(key + ".displayName")){
                displayName = chatColor(chipsSection.getString(key + ".displayName"));
                meta.setDisplayName(displayName);
            }

            if(chipsSection.isList(key + ".lore")){
                lore = new ArrayList<>(chipsSection.getStringList(key + ".lore"));
                for(int i = 0; i < lore.size();i++){
                    lore.set(i, chatColor(lore.get(i)));
                }
                meta.setLore(lore);
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
        Game game = getGame(inventoryClickEvent.getWhoClicked().getUniqueId());
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

        Game game = getGame(inventoryCloseEvent.getPlayer().getUniqueId());
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

    private void removeFromGame(boolean firstClosed, Player winner, Player loser, Game game) {
        // make sure the player is not counted as in game anymore
        if(game.getState() != GameState.FINISHED) game.onRemove(firstClosed);

        if(firstClosed){
            game.setFirst(null);
        } else {
            game.setSecond(null);
        }


        if(game.getState() != GameState.FINISHED) {
            game.cancel();

            if(plugin.isEconEnabled() && game.getPlayedChips() >= game.getRule().getMinNumberOfPlayedChips()){
                if(!winner.hasPermission(Permissions.BYPASS_ALL.getPermission()) && !winner.hasPermission(Permissions.BYPASS_GAME.getPermission(Main.gameID))){
                    Main.econ.depositPlayer(winner, game.getRule().getReward());
                    winner.sendMessage(chatColor(lang.PREFIX + lang.GAME_WON_MONEY_GAVE_UP.replaceAll("%reward%", game.getRule().getReward()+"").replaceAll("%loser%", loser.getName())));
                } else {
                    winner.sendMessage(chatColor(lang.PREFIX + lang.GAME_OTHER_GAVE_UP.replaceAll("%loser%", loser.getName())));
                }
            } else if(plugin.isEconEnabled()){
                if(!winner.hasPermission(Permissions.BYPASS_ALL.getPermission()) && !winner.hasPermission(Permissions.BYPASS_GAME.getPermission(Main.gameID))){
                    Main.econ.depositPlayer(winner, game.getRule().getCost());
                    winner.sendMessage(chatColor(lang.PREFIX + lang.GAME_WON_MONEY_GAVE_UP.replaceAll("%reward%", game.getRule().getCost()+"").replaceAll("%loser%", loser.getName())));
                } else {
                    winner.sendMessage(chatColor(lang.PREFIX + lang.GAME_OTHER_GAVE_UP.replaceAll("%loser%", loser.getName())));
                }
            } else {
                winner.sendMessage(chatColor(lang.PREFIX + lang.GAME_OTHER_GAVE_UP.replaceAll("%loser%", loser.getName())));
            }

            loser.sendMessage(chatColor(lang.PREFIX + lang.GAME_GAVE_UP));


            plugin.getNms().updateInventoryTitle(winner, lang.TITLE_WON);
            game.setState(GameState.FINISHED);

            // pay out token and save stats in enabled
            onGameEnd(winner, loser, game.getRule().getKey(), game.getPlayedChips());
        }
    }

    @Override
    public boolean isInGame(UUID uuid) {
        for(Game game : games.values()){
            if((game.getFirstUUID().equals(uuid) && game.getFirst() != null ) || (game.getSecondUUID().equals(uuid) && game.getSecond() != null)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int startGame(Player[] players, boolean playSounds, String... args) {

        GameRules rule = gameTypes.get(args[0]);
        if(rule == null){
            return GameBox.GAME_NOT_STARTED_ERROR;
        }

        double cost = rule.getCost();

        boolean firstCanPay = true;

        if (plugin.isEconEnabled() && !players[0].hasPermission(Permissions.BYPASS_ALL.getPermission()) && !players[0].hasPermission(Permissions.BYPASS_GAME.getPermission(Main.gameID)) && cost > 0.0) {
            if (Main.econ.getBalance(players[0]) >= cost) {

            } else {
                players[0].sendMessage(chatColor(lang.PREFIX + plugin.lang.GAME_NOT_ENOUGH_MONEY));
                firstCanPay = false;
            }
        }


        if (plugin.isEconEnabled() && !players[1].hasPermission(Permissions.BYPASS_ALL.getPermission()) && !players[1].hasPermission(Permissions.BYPASS_GAME.getPermission(Main.gameID)) && cost > 0.0) {
            if (Main.econ.getBalance(players[1]) >= cost) {

            } else {
                players[1].sendMessage(chatColor(lang.PREFIX + plugin.lang.GAME_NOT_ENOUGH_MONEY));
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


        if (plugin.isEconEnabled()) {
            Main.econ.withdrawPlayer(players[0], cost);
            players[0].sendMessage(chatColor(lang.PREFIX + lang.GAME_PAYED.replaceAll("%cost%", String.valueOf(cost))));


            Main.econ.withdrawPlayer(players[1], cost);
            players[1].sendMessage(chatColor(lang.PREFIX + lang.GAME_PAYED.replaceAll("%cost%", String.valueOf(cost))));
        }

        games.put(players[0].getUniqueId(), new Game(gameTypes.get(args[0]), plugin, playSounds && plugin.getPlaySounds(), players, chips));
        return GameBox.GAME_STARTED;
    }

    @Override
    public void removeFromGame(UUID uuid) {
        if(!isInGame(uuid)){
            return;
        }

        Game game = getGame(uuid);
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

    private Game getGame(UUID uuid){
        for (Game game : games.values()){
            if(game.getFirstUUID().equals(uuid) || game.getSecondUUID().equals(uuid)){
                return game;
            }
        }
        return null;
    }


    public void setGameTypes(Map<String, GameRules> gameTypes) {
        this.gameTypes = gameTypes;
    }


    private String chatColor(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public void onGameEnd(Player winner, Player loser, String key, int chipsPlayed) {

        GameRules rule = gameTypes.get(key);

        if(rule.isSaveStats()){
            addWin(winner.getUniqueId(), rule.getKey());
        }
        if(rule.getTokens() > 0 && chipsPlayed >= rule.getMinNumberOfPlayedChips()){
            plugin.gameBox.wonTokens(winner.getUniqueId(), rule.getTokens(), Main.gameID);
        }
    }

    public void addWin(UUID uuid, String key){
        plugin.gameBox.getStatistics().addStatistics(uuid, Main.gameID, key, 1., SaveType.WINS);
    }
}
