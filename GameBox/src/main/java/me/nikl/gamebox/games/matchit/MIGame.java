package me.nikl.gamebox.games.matchit;

import me.nikl.gamebox.nms.NMSUtil;
import me.nikl.gamebox.util.InventoryUtil;
import me.nikl.gamebox.util.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Created by nikl on 02.12.17.
 *
 *
 */
public class MIGame extends BukkitRunnable {

    private Player player;
    private MatchIt matchIt;

    private MILanguage language;

    private NMSUtil nms;

    private int time = 0;

    private int firstOpen = -1
            , secondOpen = -1;

    private Inventory inventory;
    private boolean started = false;

    private HashMap<Integer, Pair> pairs;

    private ItemStack cover;

    private MatchIt.GridSize gridSize = MatchIt.GridSize.SMALL;

    private boolean playSounds;
    private org.bukkit.Sound click = Sound.CLICK.bukkitSound();
    private org.bukkit.Sound match = Sound.VILLAGER_YES.bukkitSound();
    private org.bukkit.Sound nomatch = Sound.VILLAGER_NO.bukkitSound();
    private org.bukkit.Sound win = Sound.LEVEL_UP.bukkitSound();
    private float volume = 0.5f;
    private float pitch = 10f;

    private int matched = 0, nrPairs;

    private boolean over = false;

    public MIGame(MatchIt matchIt, Player player, boolean playSounds){
        this.playSounds = playSounds;
        this.player = player;
        this.matchIt = matchIt;
        this.language = (MILanguage) matchIt.getGameLang();
        this.nms = matchIt.getGameBox().getNMS();

        this.cover = new MaterialData(Material.STAINED_GLASS_PANE).toItemStack(1);

        nrPairs = gridSize.getSize()/2;

        this.inventory = InventoryUtil.createInventory(null, 54, language.INV_TITLE_START);
        generateGame();

        player.openInventory(inventory);
    }

    public void onClick(InventoryClickEvent event){
        if (over) return;

        if(!started){
            started = true;
            startGame();
        }

        int slot = event.getSlot();

        if(firstOpen < 0){
            if(inventoryToGrid(slot) >= 0){
                show(slot);
                firstOpen = slot;
            }
            return;
        }

        if(secondOpen < 0){
            if(inventoryToGrid(slot) >= 0){
                show(slot);

                if(pairs.get(inventoryToGrid(slot)).equals(pairs.get(inventoryToGrid(firstOpen)))){
                    firstOpen = -1;
                    secondOpen = -1;
                    matched++;
                    if(matched == nrPairs){
                        over = true;
                        nms.updateInventoryTitle(player, language.INV_TITLE_WON
                                .replace("%time%", String.valueOf(time)));
                        onGameEnd();
                    } else {
                        updateTitle();
                    }
                } else {
                    secondOpen = slot;
                }
            }
            return;
        }

        hide(firstOpen, secondOpen);
        firstOpen = -1;
        secondOpen = -1;
    }

    private void startGame(){
        updateTitle();
        runTaskTimerAsynchronously(matchIt.getGameBox(), 20, 20);
    }

    private void generateGame() {
        /*
        for(int slot = 0; slot < inventory.getSize(); slot++){
            inventory.setItem(slot, cover);
        }*/
        // FULL => 6 x 9, MIDDLE => 4 x 7, SMALL => 2 x 5


        pairs = new HashMap<>();
        List<ItemStack> pairItems = matchIt.getPairItems();
        Collections.shuffle(pairItems);
        Iterator<ItemStack> stackIterator = pairItems.iterator();

        List<Integer> slots = new ArrayList<>();
        for(int i = 0; i < gridSize.getSize(); i++){
            slots.add(i);
        }
        Collections.shuffle(slots);

        Iterator<Integer> iterator = slots.iterator();
        while (iterator.hasNext()){
            if(!stackIterator.hasNext()){
                Bukkit.getLogger().warning(" not enough items...");
                break;
            }
            int thisInt = iterator.next();
            if(!iterator.hasNext()) break;
            int nextInt = iterator.next();
            Pair pair = new Pair(stackIterator.next(), thisInt, nextInt);
            pairs.put(thisInt, pair);
            pairs.put(nextInt, pair);
        }

        for(int i = 0; i < gridSize.getSize(); i++){
            inventory.setItem(gridToInventory(i), cover);
        }
    }

    private int gridToInventory(int gridSlot){
        switch (this.gridSize){
            default:
            case FULL:
                return gridSlot;

            case SMALL:
                return (2 + gridSlot/5) * 9 + gridSlot%5 + 2;

            case MIDDLE:
                return (1 + gridSlot/7) * 9 + gridSlot%7 + 1;
        }
    }

    private int inventoryToGrid(int inventorySlot){

        switch (this.gridSize){
            default:
            case FULL:
                return inventorySlot;

            case SMALL:
                if(inventorySlot < 20 || (inventorySlot > 24 && inventorySlot < 29) || inventorySlot > 33)
                    return -1;
                if(inventorySlot > 19 && inventorySlot < 25)
                    return inventorySlot - 20;
                return inventorySlot - 24;

            case MIDDLE:
                if(inventorySlot < 10
                        || (inventorySlot > 16 && inventorySlot < 19)
                        || (inventorySlot > 25 && inventorySlot < 28)
                        || (inventorySlot > 34 && inventorySlot < 37)
                        || inventorySlot > 43)
                    return -1;
                if(inventorySlot > 9 && inventorySlot < 17)
                    return inventorySlot - 10;
                if(inventorySlot > 18 && inventorySlot < 26)
                    return inventorySlot - 12;
                if(inventorySlot > 27 && inventorySlot < 35)
                    return inventorySlot - 14;
                return inventorySlot - 16;
        }
    }

    private void show(int slot){
        inventory.setItem(slot, pairs.get(inventoryToGrid(slot)).item);
    }

    private void hide(int... slots){
        for(int slot : slots){
            inventory.setItem(slot, cover);
        }
    }

    private void updateTitle(){
        nms.updateInventoryTitle(player, language.INV_TITLE_GAME
                .replace("%time%", String.valueOf(time))
                .replace("%matched%", String.valueOf(matched))
                .replace("%pairs%", String.valueOf(nrPairs)));
    }

    @Override
    public void run() {
        time ++;
        updateTitle();
    }

    public void onGameEnd() {
        cancel();
    }

    private class Pair {
        public int slot1, slot2;
        public ItemStack item;

        public Pair(ItemStack item, int slot1, int slot2){
            this.item = item;
            this.slot1 = slot1;
            this.slot2 = slot2;
        }

        @Override
        public boolean equals(Object pair){
            if(!(pair instanceof Pair)) return false;
            return item.equals(((Pair) pair).item);
        }
    }
}
