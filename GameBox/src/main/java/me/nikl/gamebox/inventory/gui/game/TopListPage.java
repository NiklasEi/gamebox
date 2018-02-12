package me.nikl.gamebox.inventory.gui.game;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.data.toplist.PlayerScore;
import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.data.toplist.TopList;
import me.nikl.gamebox.data.toplist.TopListUser;
import me.nikl.gamebox.inventory.GUIManager;
import me.nikl.gamebox.utility.NumberUtil;
import me.nikl.gamebox.utility.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niklas Eicker
 *
 */
public class TopListPage extends GameGuiPage implements TopListUser {

    private SaveType saveType;
    private List<String> skullLore;
    private TopList topList;

    public TopListPage(GameBox plugin, GUIManager guiManager, int slots, String gameID, String key, String title, SaveType saveType, List<String> skullLore) {
        super(plugin, guiManager, 54, gameID, key, title); // ToDo: slots are hardcoded because of the hardcoded top list structure
        this.saveType = saveType;
        this.skullLore = skullLore;
        this.topList = plugin.getDataBase().getTopList(args[0], args[1].replace(GUIManager.TOP_LIST_KEY_ADDON, ""), saveType);
        this.topList.registerTopListUser(this);
        update();
    }

    @Override
    public boolean open(Player player){
        return super.open(player);
    }

    @Override
    public void update(){
        List<PlayerScore> topListScores = this.topList.getPlayerScores();

        PlayerScore stat;
        ItemStack skull;
        SkullMeta skullMeta;
        OfflinePlayer player;
        for(int rank = 0; rank < topListScores.size();) {
            stat = topListScores.get(rank);
            rank ++;
            skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            skullMeta = (SkullMeta) skull.getItemMeta();

            player = Bukkit.getOfflinePlayer(stat.getUuid());

            if(player == null){
                GameBox.debug(" UUID could not be matched to a player while loading a top list");
                continue;
            }

            // without the following check top lists cause NPEs after server owners
            //   delete their player data
            // ToDo: check whether second case is not already covered by first case
            if(!player.hasPlayedBefore() || player.getName() == null){
                continue;
            }

            List<String> skullLore = getSkullLoreForScore(stat);
            // chat color is already handled in the game while loading the lore from config
            for(int i = 0; i < skullLore.size(); i++){
                skullLore.set(i, skullLore.get(i).replace("%player%", player.getName()).replace("%rank%", String.valueOf(rank)));
            }

            skullMeta.setOwner(player.getName());
            skullMeta.setLore(skullLore);
            skullMeta.setDisplayName(ChatColor.BLUE + player.getName());
            skull.setItemMeta(skullMeta);
            inventory.setItem(getSlotByRank(rank), skull);
        }
    }

    private List<String> getSkullLoreForScore(PlayerScore stat) {
        List<String> skullLore = new ArrayList<>(this.skullLore);
        switch (saveType){
            case TIME_LOW:
            case TIME_HIGH:
                String time = StringUtil.formatTime((int) stat.getValue());
                for(int i = 0; i < skullLore.size(); i++){
                    skullLore.set(i, skullLore.get(i).replace("%time%", time));
                }
                break;
            case SCORE:
                for(int i = 0; i < skullLore.size(); i++){
                    skullLore.set(i, skullLore.get(i).replace("%score%", String.format("%.0f", stat.getValue())));
                }
                break;
            case HIGH_NUMBER_SCORE:
                for(int i = 0; i < skullLore.size(); i++){
                    skullLore.set(i, skullLore.get(i).replace("%score%", NumberUtil.convertHugeNumber(stat.getValue())));
                }
                break;
            case WINS:
                for(int i = 0; i < skullLore.size(); i++){
                    skullLore.set(i, skullLore.get(i).replace("%wins%", String.valueOf((int)stat.getValue())));
                }
                break;
        }
        return skullLore;
    }

    private int getSlotByRank(int rank){
        if(rank == 1) return 4;
        if(rank > 1 && rank < 5) return rank + 8;
        if(rank > 4 && rank < 8) return rank + 9;
        return rank + 10;
    }
}
