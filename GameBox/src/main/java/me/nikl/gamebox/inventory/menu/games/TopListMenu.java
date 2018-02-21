package me.nikl.gamebox.inventory.menu.games;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.data.toplist.PlayerScore;
import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.data.toplist.TopList;
import me.nikl.gamebox.data.toplist.TopListUser;
import me.nikl.gamebox.games.Game;
import me.nikl.gamebox.inventory.GUIManager;
import me.nikl.gamebox.utility.NumberUtility;
import me.nikl.gamebox.utility.StringUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nikl on 21.02.18.
 */
public class TopListMenu extends GamePageMenu implements TopListUser {
    private SaveType saveType;
    private List<String> skullLore;
    private TopList topList;

    public TopListMenu(GameBox gameBox, Game game, String key, String title, SaveType saveType, List<String> skullLore) {
        super(gameBox, game);
        defaultTitle = title;
        this.saveType = saveType;
        this.skullLore = skullLore;
        this.topList = gameBox.getDataBase().getTopList(getModuleID(), key.replace(GUIManager.TOP_LIST_KEY_ADDON, ""), saveType);
        this.topList.registerTopListUser(this);
        update();
    }

    @Override
    public void update() {
        List<PlayerScore> topListScores = this.topList.getPlayerScores();
        PlayerScore stat;
        ItemStack skull;
        SkullMeta skullMeta;
        OfflinePlayer player;
        for (int rank = 0; rank < topListScores.size(); ) {
            stat = topListScores.get(rank);
            rank++;
            skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            skullMeta = (SkullMeta) skull.getItemMeta();
            player = Bukkit.getOfflinePlayer(stat.getUuid());
            // checking for name == null is important to prevent NPEs after player data reset on server
            if (player == null || player.getName() == null) {
                continue;
            }
            String name = player.getName();
            List<String> skullLore = getSkullLoreForScore(stat);
            // chat color is already handled when loading the lore from the configuration file
            for (int i = 0; i < skullLore.size(); i++) {
                skullLore.set(i, skullLore.get(i).replace("%player%", name).replace("%rank%", String.valueOf(rank)));
            }
            skullMeta.setOwner(name);
            skullMeta.setLore(skullLore);
            skullMeta.setDisplayName(ChatColor.BLUE + name);
            skull.setItemMeta(skullMeta);
            inventory.setItem(getSlotByRank(rank), skull);
        }
    }

    private List<String> getSkullLoreForScore(PlayerScore stat) {
        List<String> skullLore = new ArrayList<>(this.skullLore);
        switch (saveType) {
            case TIME_LOW:
            case TIME_HIGH:
                String time = StringUtility.formatTime((int) stat.getValue());
                for (int i = 0; i < skullLore.size(); i++) {
                    skullLore.set(i, skullLore.get(i).replace("%time%", time));
                }
                break;
            case SCORE:
                for (int i = 0; i < skullLore.size(); i++) {
                    skullLore.set(i, skullLore.get(i).replace("%score%", String.format("%.0f", stat.getValue())));
                }
                break;
            case HIGH_NUMBER_SCORE:
                for (int i = 0; i < skullLore.size(); i++) {
                    skullLore.set(i, skullLore.get(i).replace("%score%", NumberUtility.convertHugeNumber(stat.getValue())));
                }
                break;
            case WINS:
                for (int i = 0; i < skullLore.size(); i++) {
                    skullLore.set(i, skullLore.get(i).replace("%wins%", String.valueOf((int) stat.getValue())));
                }
                break;
        }
        return skullLore;
    }

    private int getSlotByRank(int rank) {
        if (rank == 1) return 4;
        if (rank > 1 && rank < 5) return rank + 8;
        if (rank > 4 && rank < 8) return rank + 9;
        return rank + 10;
    }
}
