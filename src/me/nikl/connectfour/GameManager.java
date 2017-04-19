package me.nikl.connectfour;

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

import java.util.*;

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
        // ToDo
        return false;
    }

    @Override
    public boolean onInventoryClose(InventoryCloseEvent inventoryCloseEvent) {
        games.remove(inventoryCloseEvent.getPlayer().getUniqueId());
        return true;
    }

    @Override
    public boolean isInGame(UUID uuid) {
        return games.containsKey(uuid);
    }

    @Override
    public int startGame(Player[] players, boolean playSounds, String... args) {
        // ToDo
        games.put(players[0].getUniqueId(), new Game(gameTypes.get(args[0]), plugin, playSounds && plugin.getPlaySounds(), players[0], chips));
        return 1;
    }

    @Override
    public void removeFromGame(UUID uuid) {
        games.remove(uuid);
    }

    public void setGameTypes(Map<String, GameRules> gameTypes) {
        this.gameTypes = gameTypes;
    }


    private String chatColor(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
