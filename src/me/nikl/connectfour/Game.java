package me.nikl.connectfour;

import me.nikl.gamebox.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Created by Niklas on 14.04.2017.
 *
 *
 */
public class Game extends BukkitRunnable{

    private GameRules rule;
    private boolean playSounds;
    private Main plugin;

    private UUID firstUUID, secondUUID;
    private Player first, second;

    private ItemStack firstChip, secondChip;

    private Inventory inv;

    private Sound falling = Sounds.WOOD_CLICK.bukkitSound()
            , insert = Sounds.CLICK.bukkitSound()
            , turn = Sounds.NOTE_PLING.bukkitSound()
            , won = Sounds.VILLAGER_YES.bukkitSound()
            , lose = Sounds.VILLAGER_NO.bukkitSound();

    private float volume = 0.5f, pitch= 1f;

    private GameState state;

    private int fallingChip;


    public Game(GameRules rule, Main plugin, boolean playSounds, Player[] players, Map<Integer, ItemStack> chips){
        this.plugin = plugin;
        this.rule = rule;
        this.playSounds = playSounds;

        this.first = players[0];
        this.second = players[1];

        this.firstUUID = first.getUniqueId();
        this.secondUUID = second.getUniqueId();

        Random rand = new Random(System.currentTimeMillis());
        int first, second;
        first = rand.nextInt(chips.size());
        second = rand.nextInt(chips.size());
        while (first == second){
            second = rand.nextInt(chips.size());
        }

        firstChip = chips.get(first).clone();
        ItemMeta meta = firstChip.getItemMeta();
        meta.setDisplayName(meta.getDisplayName().replace("%player%", this.first.getName()));
        firstChip.setItemMeta(meta);

        secondChip = chips.get(second).clone();
        meta = secondChip.getItemMeta();
        meta.setDisplayName(meta.getDisplayName().replace("%player%", this.second.getName()));
        secondChip.setItemMeta(meta);

        if(rand.nextDouble() < 0.5){
            this.state = GameState.FIRST_TURN;
        } else {
            this.state = GameState.SECOND_TURN;
        }

        inv = Bukkit.createInventory(null, 54, "default");
        this.first.openInventory(inv);
        this.second.openInventory(inv);
        updateStatus();

        runTaskTimer(plugin, 0, 5);
    }

    private void updateStatus() {
        switch (this.state){
            case FIRST_TURN:
                if(first!=null && second!=null){
                    plugin.getNms().updateInventoryTitle(first, plugin.lang.TITLE_IN_GAME.replace("%player%", first.getName()));
                    plugin.getNms().updateInventoryTitle(second, plugin.lang.TITLE_IN_GAME.replace("%player%", first.getName()));
                }
                break;
            case SECOND_TURN:
                if(first!=null && second!=null){
                    plugin.getNms().updateInventoryTitle(first, plugin.lang.TITLE_IN_GAME.replace("%player%", second.getName()));
                    plugin.getNms().updateInventoryTitle(second, plugin.lang.TITLE_IN_GAME.replace("%player%", second.getName()));
                }
                break;
            case FINISHED:
                Bukkit.getConsoleSender().sendMessage("ToDo! (updateStatus)");
                break;
        }
    }

    @Override
    public void run() {
        switch (state){
            case FINISHED:
            case SECOND_TURN:
            case FIRST_TURN:
                break;
            case FALLING_FIRST:
                if(fallingChip /9 < 5 && (inv.getItem(fallingChip +9) == null || inv.getItem(fallingChip +9).getType() == Material.AIR)){
                    plugin.debug("set " + fallingChip + " to null");
                    inv.setItem(fallingChip, null);
                    fallingChip +=9;
                    plugin.debug("set " + fallingChip + " to fallingChip");
                    inv.setItem(fallingChip, firstChip);
                    if(playSounds) {
                        first.playSound(first.getLocation(), falling, volume*0.5f, pitch);
                        second.playSound(second.getLocation(), falling, volume*0.5f, pitch);
                    }
                } else {
                    if(checkForMatches(fallingChip)){
                        onGameEnd();
                        state = GameState.FINISHED;
                        if(playSounds) {
                            first.playSound(first.getLocation(), won, volume, pitch);
                            second.playSound(second.getLocation(), lose, volume, pitch);
                        }
                        return;
                    } else if(isDraw()) {
                        state = GameState.FINISHED;
                        if(first!=null && second!=null){
                            plugin.getNms().updateInventoryTitle(first, plugin.lang.TITLE_DRAW);
                            plugin.getNms().updateInventoryTitle(second, plugin.lang.TITLE_DRAW);
                            if(playSounds) {
                                first.playSound(first.getLocation(), lose, volume, pitch);
                                second.playSound(second.getLocation(), lose, volume, pitch);
                            }
                        }
                        return;
                    } else {
                        state = GameState.SECOND_TURN;
                        updateStatus();
                        if(playSounds) second.playSound(second.getLocation(), turn, volume, pitch);
                        return;
                    }
                }
                break;
            case FALLING_SECOND:
                if(fallingChip /9 < 5 && (inv.getItem(fallingChip +9) == null || inv.getItem(fallingChip +9).getType() == Material.AIR)){
                    plugin.debug("set " + fallingChip + " to null");
                    inv.setItem(fallingChip, null);
                    fallingChip +=9;
                    plugin.debug("set " + fallingChip + " to fallingChip");
                    inv.setItem(fallingChip, secondChip);
                    if(playSounds) {
                        first.playSound(first.getLocation(), falling, volume*0.5f, pitch);
                        second.playSound(second.getLocation(), falling, volume*0.5f, pitch);
                    }
                } else {
                    if(checkForMatches(fallingChip)){
                        onGameEnd();
                        state = GameState.FINISHED;
                        if(playSounds) {
                            first.playSound(first.getLocation(), lose, volume, pitch);
                            second.playSound(second.getLocation(), won, volume, pitch);
                        }
                        return;
                    } else if(isDraw()) {
                        state = GameState.FINISHED;
                        if(first!=null && second!=null){
                            plugin.getNms().updateInventoryTitle(first, plugin.lang.TITLE_DRAW);
                            plugin.getNms().updateInventoryTitle(second, plugin.lang.TITLE_DRAW);
                            if(playSounds) {
                                first.playSound(first.getLocation(), lose, volume, pitch);
                                second.playSound(second.getLocation(), lose, volume, pitch);
                            }
                        }
                        cancel();
                        return;
                    } else {
                        state = GameState.FIRST_TURN;
                        updateStatus();
                        if(playSounds) first.playSound(first.getLocation(), turn, volume, pitch);
                        return;
                    }
                }
                break;
        }
    }

    private void onGameEnd(){
        cancel();

        if(state != GameState.FALLING_SECOND && state != GameState.FALLING_FIRST){
            Bukkit.getConsoleSender().sendMessage(plugin.lang.PREFIX + " *** wrong game state on game end ***");
            Bukkit.getConsoleSender().sendMessage(" Please contact Nikl on Spigot and show him this log");
            return;
        }

        Player winner = state==GameState.FALLING_SECOND?second:first;
        Player loser = state==GameState.FALLING_SECOND?first:second;

        Language lang = plugin.lang;

        if(winner!=null && loser !=null){

            if(plugin.isEconEnabled()){
                if(!winner.hasPermission(Permissions.BYPASS_ALL.getPermission()) && !winner.hasPermission(Permissions.BYPASS_GAME.getPermission(Main.gameID))){
                    Main.econ.depositPlayer(winner, rule.getReward());
                    winner.sendMessage((lang.PREFIX + lang.GAME_WON_MONEY.replaceAll("%reward%", rule.getReward()+"").replaceAll("%loser%", loser.getName())));
                } else {
                    winner.sendMessage((lang.PREFIX + lang.GAME_WON.replaceAll("%loser%", loser.getName())));
                }
            } else {
                winner.sendMessage((lang.PREFIX + lang.GAME_WON.replaceAll("%loser%", loser.getName())));
            }
            loser.sendMessage((lang.PREFIX + lang.GAME_LOSE));

            plugin.getNms().updateInventoryTitle(winner, plugin.lang.TITLE_WON);
            plugin.getNms().updateInventoryTitle(loser, plugin.lang.TITLE_LOST);


            plugin.getGameManager().onGameEnd(winner, loser, rule.getKey());
        }
    }

    private boolean isDraw() {
        for(ItemStack item : inv.getContents()){
            if(item == null || item.getType() == Material.AIR){
                return false;
            }
        }
        return true;
    }

    private boolean checkForMatches(int chip) {
        Set<Integer> toMark = new HashSet<>();
        toMark.add(chip);

        // check to the right
        for(int newChip = chip + 1; newChip<=53 && newChip%9<=8 && newChip%9!=0 ; newChip++){
            if(checkEntry(newChip)){
                toMark.add(newChip);
            } else {
                break;
            }
        }
        // check to the left
        for(int newChip = chip - 1; newChip>=0 && newChip%9>=0 && newChip%9 != 8; newChip--){
            if(checkEntry(newChip)){
                toMark.add(newChip);
            } else {
                break;
            }
        }

        // test whether left/right was successful
        if(mark(toMark)){
            return true;
        } else {
            toMark.clear();
            toMark.add(chip);
        }
        plugin.debug("passed left/right check");

        // check for up
        for(int newChip = chip - 9; newChip>=0 && newChip/9>=0 ; newChip-=9){
            if(checkEntry(newChip)){
                toMark.add(newChip);
            } else {
                break;
            }
        }
        // check to the down
        for(int newChip = chip + 9; newChip<=53 && newChip/9<=5; newChip += 9){
            if(checkEntry(newChip)){
                toMark.add(newChip);
            } else {
                break;
            }
        }

        // test whether up/down was successful
        if(mark(toMark)){
            return true;
        } else {
            toMark.clear();
            toMark.add(chip);
        }
        plugin.debug("passed up/down check");



        // check for up-right
        for(int newChip = chip - 8; newChip>=0 && newChip<=53 && newChip/9>=0 && newChip%9<=8 && newChip%9!=0; newChip-=8){
            if(checkEntry(newChip)){
                toMark.add(newChip);
            } else {
                break;
            }
        }
        // check to the down-left
        for(int newChip = chip + 8; newChip>=0 && newChip<=53 && newChip/9<=5 && newChip%9>=0 && newChip%9 != 8; newChip += 8){
            if(checkEntry(newChip)){
                toMark.add(newChip);
            } else {
                break;
            }
        }

        // test whether up-right/down-left was successful
        if(mark(toMark)){
            return true;
        } else {
            toMark.clear();
            toMark.add(chip);
        }
        plugin.debug("passed up-right/down-left check");



        // check for up-left
        for(int newChip = chip - 10; newChip>=0 && newChip<=53 && newChip/9>=0  && newChip%9>=0 && newChip%9 != 8; newChip-=10){
            if(checkEntry(newChip)){
                toMark.add(newChip);
            } else {
                break;
            }
        }
        // check to the down-right
        for(int newChip = chip + 10; newChip>=0 && newChip<=53 && newChip/9<=5 && newChip%9<=8 && newChip%9!=0; newChip += 10){
            if(checkEntry(newChip)){
                toMark.add(newChip);
            } else {
                break;
            }
        }

        // test whether up/down was successful
        if(mark(toMark)){
            return true;
        }
        plugin.debug("passed all checks");
        return false;
    }

    private boolean checkEntry(int newChip) {
        if(inv.getItem(newChip) != null && inv.getItem(newChip).getType() != Material.AIR && inv.getItem(newChip).isSimilar(inv.getItem(fallingChip))){
            plugin.debug("found " + newChip);
            return true;
        } else {
            return false;
        }
    }

    private boolean mark(Set<Integer> toMark) {
        plugin.debug("   toMark entries: " + toMark.size());
        if(toMark.size() > 3){

            plugin.debug("found match! ");
            ItemStack glowingChip;
            if(state == GameState.FALLING_SECOND){
                glowingChip = plugin.getNms().addGlow(secondChip.clone());
            } else {
                glowingChip = plugin.getNms().addGlow(firstChip.clone());
            }
            for(int i : toMark){
                inv.setItem(i, glowingChip);
            }
            return true;
        }
        return false;
    }

    public void onClick(InventoryClickEvent inventoryClickEvent) {
        plugin.debug("onClick in game called");
        // clicked slot is empty!
        UUID uuid = inventoryClickEvent.getWhoClicked().getUniqueId();
        if(uuid.equals(firstUUID) && state == GameState.FIRST_TURN){
            inv.setItem(inventoryClickEvent.getSlot(), firstChip);
            fallingChip = inventoryClickEvent.getSlot();
            state = GameState.FALLING_FIRST;
            if(playSounds) first.playSound(first.getLocation(), insert, volume, pitch);
        } else if(uuid.equals(secondUUID) && state == GameState.SECOND_TURN){
            inv.setItem(inventoryClickEvent.getSlot(), secondChip);
            fallingChip = inventoryClickEvent.getSlot();
            state = GameState.FALLING_SECOND;
            if(playSounds) second.playSound(second.getLocation(), insert, volume, pitch);
        }
    }

    public void onRemove(boolean firstClosed) {
        if(firstClosed){
            if(first != null){
                if(playSounds) first.playSound(first.getLocation(), lose, volume, pitch);
            }
            if(second != null){
                if(playSounds) second.playSound(second.getLocation(), won, volume, pitch);
            }
        } else {
            if(first != null){
                if(playSounds) first.playSound(first.getLocation(), won, volume, pitch);
            }
            if(second != null){
                if(playSounds) second.playSound(second.getLocation(), lose, volume, pitch);
            }
        }
    }


    // Getters and setters

    public UUID getFirstUUID() {
        return firstUUID;
    }

    public void setFirstUUID(UUID firstUUID) {
        this.firstUUID = firstUUID;
    }

    public UUID getSecondUUID() {
        return secondUUID;
    }

    public void setSecondUUID(UUID secondUUID) {
        this.secondUUID = secondUUID;
    }

    public Player getFirst() {
        return first;
    }

    public void setFirst(Player first) {
        this.first = first;
    }

    public Player getSecond() {
        return second;
    }

    public void setSecond(Player second) {
        this.second = second;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public GameRules getRule() {
        return rule;
    }
}
