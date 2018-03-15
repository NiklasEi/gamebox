package me.nikl.gamebox.games.connectfour;

import me.nikl.gamebox.GameBox;
import me.nikl.nmsutilities.NmsFactory;
import me.nikl.nmsutilities.NmsUtility;
import me.nikl.gamebox.utility.Permission;
import me.nikl.gamebox.utility.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * @author Niklas Eicker
 */
public class CFGame extends BukkitRunnable {
    private CFGameRules rule;
    private boolean playSounds;
    private ConnectFour connectFour;
    private UUID firstUUID, secondUUID;
    private Player first, second;
    private ItemStack firstChip, secondChip;
    private Inventory inv;
    private org.bukkit.Sound falling = Sound.WOOD_CLICK.bukkitSound()
            , insert = Sound.CLICK.bukkitSound()
            , turn = Sound.NOTE_PLING.bukkitSound()
            , won = Sound.VILLAGER_YES.bukkitSound()
            , lose = Sound.VILLAGER_NO.bukkitSound();
    private float volume = 0.5f, pitch = 1f;
    private double time;
    private String timeStr = "";
    private CFGameState state;
    private int fallingChip;
    private int playedChips = 0;
    private NmsUtility nms;
    private CFLanguage lang;

    CFGame(CFGameRules rule, ConnectFour connectFour, boolean playSounds, Player[] players, Map<Integer, ItemStack> chips) {
        this.connectFour = connectFour;
        this.lang = (CFLanguage) connectFour.getGameLang();
        this.nms = NmsFactory.getNmsUtility();
        this.rule = rule;
        this.playSounds = playSounds;
        this.time = rule.getTimePerMove();
        this.timeStr = String.valueOf((int) time);
        this.first = players[0];
        this.second = players[1];
        this.firstUUID = first.getUniqueId();
        this.secondUUID = second.getUniqueId();
        Random rand = new Random(System.currentTimeMillis());
        int first, second;
        first = rand.nextInt(chips.size());
        second = rand.nextInt(chips.size());
        while (first == second) {
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
        if (rand.nextDouble() < 0.5) {
            this.state = CFGameState.FIRST_TURN;
        } else {
            this.state = CFGameState.SECOND_TURN;
        }
        inv = connectFour.createInventory(54, "default");
        this.first.openInventory(inv);
        this.second.openInventory(inv);
        updateStatus();
        runTaskTimer(connectFour.getGameBox(), 0, 5);
    }

    private void updateStatus() {
        switch (this.state) {
            case FIRST_TURN:
                if (first != null && second != null) {
                    nms.updateInventoryTitle(first, lang.TITLE_IN_GAME_YOUR_TURN.replace("%player%", first.getName()).replace("%time%", timeStr));
                    nms.updateInventoryTitle(second, lang.TITLE_IN_GAME_OTHERS_TURN.replace("%player%", first.getName()).replace("%time%", timeStr));
                }
                break;
            case SECOND_TURN:
                if (first != null && second != null) {
                    nms.updateInventoryTitle(first, lang.TITLE_IN_GAME_OTHERS_TURN.replace("%player%", second.getName()).replace("%time%", timeStr));
                    nms.updateInventoryTitle(second, lang.TITLE_IN_GAME_YOUR_TURN.replace("%player%", second.getName()).replace("%time%", timeStr));
                }
                break;
            case FINISHED:
                // title is updated in onGameEnd
                break;
        }
    }

    @Override
    public void run() {

        if (state == CFGameState.FIRST_TURN || state == CFGameState.SECOND_TURN) {
            int oldTime = (int) Math.ceil(time);
            // for the wanted 20 ticks per second:
            //   this is not very accurate but sufficient in this case (imho)
            time -= 0.25;
            if (time < 0.25) {
                // timer has run out!

                state = state == CFGameState.SECOND_TURN ? CFGameState.FIRST_TURN : CFGameState.SECOND_TURN;
                if (state == CFGameState.FIRST_TURN) {
                    if (playSounds) first.playSound(first.getLocation(), turn, volume, pitch);
                } else {
                    if (playSounds) second.playSound(second.getLocation(), turn, volume, pitch);
                }
                time = rule.getTimePerMove();
            }

            if ((int) Math.ceil(time) != oldTime) {
                timeStr = String.valueOf((int) Math.ceil(time));
                updateStatus();
            }
        }

        switch (state) {
            case FINISHED:
            case SECOND_TURN:
            case FIRST_TURN:
                break;
            case FALLING_FIRST:
                if (fallingChip / 9 < 5 && (inv.getItem(fallingChip + 9) == null || inv.getItem(fallingChip + 9).getType() == Material.AIR)) {
                    connectFour.debug("set " + fallingChip + " to null");
                    inv.setItem(fallingChip, null);
                    fallingChip += 9;
                    connectFour.debug("set " + fallingChip + " to fallingChip");
                    inv.setItem(fallingChip, firstChip);
                    if (playSounds) {
                        first.playSound(first.getLocation(), falling, volume * 0.5f, pitch);
                        second.playSound(second.getLocation(), falling, volume * 0.5f, pitch);
                    }
                } else {
                    if (checkForMatches(fallingChip)) {
                        onGameEnd();
                        state = CFGameState.FINISHED;
                        if (playSounds) {
                            first.playSound(first.getLocation(), won, volume, pitch);
                            second.playSound(second.getLocation(), lose, volume, pitch);
                        }
                        return;
                    } else if (isDraw()) {
                        state = CFGameState.FINISHED;
                        if (first != null && second != null) {
                            nms.updateInventoryTitle(first, lang.TITLE_DRAW);
                            nms.updateInventoryTitle(second, lang.TITLE_DRAW);
                            if (playSounds) {
                                first.playSound(first.getLocation(), lose, volume, pitch);
                                second.playSound(second.getLocation(), lose, volume, pitch);
                            }
                        }
                        return;
                    } else {
                        state = CFGameState.SECOND_TURN;
                        time = rule.getTimePerMove();
                        timeStr = String.valueOf((int) time);
                        updateStatus();
                        if (playSounds) second.playSound(second.getLocation(), turn, volume, pitch);
                        return;
                    }
                }
                break;
            case FALLING_SECOND:
                if (fallingChip / 9 < 5 && (inv.getItem(fallingChip + 9) == null || inv.getItem(fallingChip + 9).getType() == Material.AIR)) {
                    connectFour.debug("set " + fallingChip + " to null");
                    inv.setItem(fallingChip, null);
                    fallingChip += 9;
                    connectFour.debug("set " + fallingChip + " to fallingChip");
                    inv.setItem(fallingChip, secondChip);
                    if (playSounds) {
                        first.playSound(first.getLocation(), falling, volume * 0.5f, pitch);
                        second.playSound(second.getLocation(), falling, volume * 0.5f, pitch);
                    }
                } else {
                    if (checkForMatches(fallingChip)) {
                        onGameEnd();
                        state = CFGameState.FINISHED;
                        if (playSounds) {
                            first.playSound(first.getLocation(), lose, volume, pitch);
                            second.playSound(second.getLocation(), won, volume, pitch);
                        }
                        return;
                    } else if (isDraw()) {
                        state = CFGameState.FINISHED;
                        if (first != null && second != null) {
                            nms.updateInventoryTitle(first, lang.TITLE_DRAW);
                            nms.updateInventoryTitle(second, lang.TITLE_DRAW);
                            if (playSounds) {
                                first.playSound(first.getLocation(), lose, volume, pitch);
                                second.playSound(second.getLocation(), lose, volume, pitch);
                            }
                        }
                        cancel();
                        return;
                    } else {
                        state = CFGameState.FIRST_TURN;
                        time = rule.getTimePerMove();
                        timeStr = String.valueOf((int) time);
                        updateStatus();
                        if (playSounds) first.playSound(first.getLocation(), turn, volume, pitch);
                        return;
                    }
                }
                break;
        }
    }

    private void onGameEnd() {
        cancel();
        if (state != CFGameState.FALLING_SECOND && state != CFGameState.FALLING_FIRST) {
            Bukkit.getConsoleSender().sendMessage(lang.PREFIX + " *** wrong game state on game end ***");
            Bukkit.getConsoleSender().sendMessage(" Please contact Nikl on Spigot and show him this log");
            return;
        }
        Player winner = state == CFGameState.FALLING_SECOND ? second : first;
        Player loser = state == CFGameState.FALLING_SECOND ? first : second;

        if (winner != null && loser != null) {
            loser.sendMessage((lang.PREFIX + lang.GAME_LOSE));
            nms.updateInventoryTitle(winner, lang.TITLE_WON);
            nms.updateInventoryTitle(loser, lang.TITLE_LOST);
            // if the game is ended regularly ignore the played chips
            ((CFGameManager) connectFour.getGameManager()).onGameEnd(winner, loser, rule.getKey(), 999);
        }
    }

    private boolean isDraw() {
        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                return false;
            }
        }
        return true;
    }

    private boolean checkForMatches(int chip) {
        Set<Integer> toMark = new HashSet<>();
        toMark.add(chip);

        // check to the right
        for (int newChip = chip + 1; newChip <= 53 && newChip % 9 <= 8 && newChip % 9 != 0; newChip++) {
            if (checkEntry(newChip)) {
                toMark.add(newChip);
            } else {
                break;
            }
        }
        // check to the left
        for (int newChip = chip - 1; newChip >= 0 && newChip % 9 >= 0 && newChip % 9 != 8; newChip--) {
            if (checkEntry(newChip)) {
                toMark.add(newChip);
            } else {
                break;
            }
        }

        // test whether left/right was successful
        if (mark(toMark)) {
            return true;
        } else {
            toMark.clear();
            toMark.add(chip);
        }
        connectFour.debug("passed left/right check");

        // check for up
        for (int newChip = chip - 9; newChip >= 0 && newChip / 9 >= 0; newChip -= 9) {
            if (checkEntry(newChip)) {
                toMark.add(newChip);
            } else {
                break;
            }
        }
        // check to the down
        for (int newChip = chip + 9; newChip <= 53 && newChip / 9 <= 5; newChip += 9) {
            if (checkEntry(newChip)) {
                toMark.add(newChip);
            } else {
                break;
            }
        }

        // test whether up/down was successful
        if (mark(toMark)) {
            return true;
        } else {
            toMark.clear();
            toMark.add(chip);
        }
        connectFour.debug("passed up/down check");


        // check for up-right
        for (int newChip = chip - 8; newChip >= 0 && newChip <= 53 && newChip / 9 >= 0 && newChip % 9 <= 8 && newChip % 9 != 0; newChip -= 8) {
            if (checkEntry(newChip)) {
                toMark.add(newChip);
            } else {
                break;
            }
        }
        // check to the down-left
        for (int newChip = chip + 8; newChip >= 0 && newChip <= 53 && newChip / 9 <= 5 && newChip % 9 >= 0 && newChip % 9 != 8; newChip += 8) {
            if (checkEntry(newChip)) {
                toMark.add(newChip);
            } else {
                break;
            }
        }

        // test whether up-right/down-left was successful
        if (mark(toMark)) {
            return true;
        } else {
            toMark.clear();
            toMark.add(chip);
        }
        connectFour.debug("passed up-right/down-left check");


        // check for up-left
        for (int newChip = chip - 10; newChip >= 0 && newChip <= 53 && newChip / 9 >= 0 && newChip % 9 >= 0 && newChip % 9 != 8; newChip -= 10) {
            if (checkEntry(newChip)) {
                toMark.add(newChip);
            } else {
                break;
            }
        }
        // check to the down-right
        for (int newChip = chip + 10; newChip >= 0 && newChip <= 53 && newChip / 9 <= 5 && newChip % 9 <= 8 && newChip % 9 != 0; newChip += 10) {
            if (checkEntry(newChip)) {
                toMark.add(newChip);
            } else {
                break;
            }
        }

        // test whether up/down was successful
        if (mark(toMark)) {
            return true;
        }
        connectFour.debug("passed all checks");
        return false;
    }

    private boolean checkEntry(int newChip) {
        if (inv.getItem(newChip) != null && inv.getItem(newChip).getType() != Material.AIR && inv.getItem(newChip).isSimilar(inv.getItem(fallingChip))) {
            connectFour.debug("found " + newChip);
            return true;
        } else {
            return false;
        }
    }

    private boolean mark(final Set<Integer> toMark) {
        connectFour.debug("   toMark entries: " + toMark.size());
        if (toMark.size() > 3) {

            connectFour.debug("found match! ");
            ItemStack glowingChip;
            if (state == CFGameState.FALLING_SECOND) {
                glowingChip = nms.addGlow(secondChip.clone());
            } else {
                glowingChip = nms.addGlow(firstChip.clone());
            }
            for (int i : toMark) {
                inv.setItem(i, glowingChip);
            }
            return true;
        }
        return false;
    }

    void onClick(InventoryClickEvent inventoryClickEvent) {
        connectFour.debug("onClick in game called");
        // clicked slot is empty!
        UUID uuid = inventoryClickEvent.getWhoClicked().getUniqueId();
        if (uuid.equals(firstUUID) && state == CFGameState.FIRST_TURN) {
            inv.setItem(inventoryClickEvent.getSlot() % 9, firstChip);
            fallingChip = inventoryClickEvent.getSlot() % 9;
            state = CFGameState.FALLING_FIRST;
            nms.updateInventoryTitle(first, lang.TITLE_IN_GAME_YOUR_TURN.replace("%player%", first.getName()).replace("%time%", ""));
            nms.updateInventoryTitle(second, lang.TITLE_IN_GAME_OTHERS_TURN.replace("%player%", first.getName()).replace("%time%", ""));
            if (playSounds) first.playSound(first.getLocation(), insert, volume, pitch);
            playedChips++;
        } else if (uuid.equals(secondUUID) && state == CFGameState.SECOND_TURN) {
            inv.setItem(inventoryClickEvent.getSlot() % 9, secondChip);
            fallingChip = inventoryClickEvent.getSlot() % 9;
            state = CFGameState.FALLING_SECOND;
            nms.updateInventoryTitle(first, lang.TITLE_IN_GAME_OTHERS_TURN.replace("%player%", first.getName()).replace("%time%", ""));
            nms.updateInventoryTitle(second, lang.TITLE_IN_GAME_YOUR_TURN.replace("%player%", first.getName()).replace("%time%", ""));
            if (playSounds) second.playSound(second.getLocation(), insert, volume, pitch);
            playedChips++;
        }
    }

    void onRemove(boolean firstClosed) {
        if (firstClosed) {
            if (first != null) {
                if (playSounds) first.playSound(first.getLocation(), lose, volume, pitch);
            }
            if (second != null) {
                if (playSounds) second.playSound(second.getLocation(), won, volume, pitch);
            }
        } else {
            if (first != null) {
                if (playSounds) first.playSound(first.getLocation(), won, volume, pitch);
            }
            if (second != null) {
                if (playSounds) second.playSound(second.getLocation(), lose, volume, pitch);
            }
        }
    }


    // Getters and setters

    UUID getFirstUUID() {
        return firstUUID;
    }

    UUID getSecondUUID() {
        return secondUUID;
    }

    Player getFirst() {
        return first;
    }

    void setFirst(Player first) {
        this.first = first;
    }

    Player getSecond() {
        return second;
    }

    void setSecond(Player second) {
        this.second = second;
    }

    CFGameState getState() {
        return state;
    }

    void setState(CFGameState state) {
        this.state = state;
    }

    CFGameRules getRule() {
        return rule;
    }

    public int getPlayedChips() {
        return playedChips;
    }
}
