package me.nikl.connectfour;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Random;

/**
 * Created by Niklas on 14.04.2017.
 *
 *
 */
public class Game {

    private GameRules rule;
    private boolean playSounds;
    private Main plugin;

    private ItemStack firstChip, secondChip;


    public Game(GameRules rule, Main plugin, boolean playSounds, Player player, Map<Integer, ItemStack> chips){
        this.plugin = plugin;
        this.rule = rule;
        this.playSounds = playSounds;

        Random rand = new Random(System.currentTimeMillis());
        int first, second;
        first = rand.nextInt(chips.size());
        second = rand.nextInt(chips.size());
        while (first == second){
            second = rand.nextInt(chips.size());
        }

        firstChip = chips.get(first);
        secondChip = chips.get(second);
    }




}
