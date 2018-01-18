package me.nikl.gamebox.inventory.gui.game;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.data.DataBase;
import me.nikl.gamebox.data.SaveType;
import me.nikl.gamebox.inventory.GUIManager;
import me.nikl.gamebox.utility.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;

/**
 * @author Niklas Eicker
 *
 */
public class TopListPage  extends GameGuiPage{

    private  SaveType saveType;
    private ArrayList<String> skullLore;

    public TopListPage(GameBox plugin, GUIManager guiManager, int slots, String gameID, String key, String title, SaveType saveType, ArrayList<String> skullLore) {
        super(plugin, guiManager, slots, gameID, key, title);
        this.saveType = saveType;
        this.skullLore = skullLore;
    }

    @Override
    public boolean open(Player player){
        update();
        return super.open(player);
    }

    public void update(){

        ArrayList<DataBase.Stat> topList = plugin.getDataBase().getTopList(args[0], args[1].replace(GUIManager.TOP_LIST_KEY_ADDON, ""), saveType, inventory.getSize());

        DataBase.Stat stat;
        ItemStack skull;
        SkullMeta skullMeta;
        ArrayList<String> skullLore;
        OfflinePlayer player;
        for(int rank = 0; rank < topList.size();) {
            stat = topList.get(rank);
            rank ++;
            skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            skullMeta = (SkullMeta) skull.getItemMeta();
            skullLore = new ArrayList<>(this.skullLore);

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

            // chat color is already handled in the game while loading the lor from config
            for(int i = 0; i < skullLore.size(); i++){
                skullLore.set(i, skullLore.get(i).replace("%player%", player.getName()).replace("%rank%", String.valueOf(rank)));
            }

            switch (saveType){
                case TIME_LOW:
                case TIME_HIGH:
                    // format mm:ss
                    int seconds = (int)(stat.getValue());
                    int min = seconds / 60;
                    int secsLeft = seconds % 60;
                    String time = (min < 9? "0" + String.valueOf(min): String.valueOf(min)) + ":" + (secsLeft > 9? String.valueOf(secsLeft): "0" + String.valueOf(secsLeft));
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

            // trying to load the texture of the skull
            /*
            Location loc = new Location(Bukkit.getWorlds().get(0), 0,0,0);
            Block block = loc.getBlock();
            loc.getBlock().setType(Material.SKULL);
            loc.getBlock().setData((byte) 3);
            Skull s = (Skull) loc.getBlock().getState();
            s.setOwner(player.getName());
            s.update();
            */


            skullMeta.setOwner(player.getName());
            skullMeta.setLore(skullLore);
            skullMeta.setDisplayName(ChatColor.BLUE + player.getName());

            skull.setItemMeta(skullMeta);

            inventory.setItem(rank - 1, skull);
        }
    }
}
