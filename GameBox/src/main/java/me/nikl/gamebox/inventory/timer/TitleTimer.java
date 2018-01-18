package me.nikl.gamebox.inventory.timer;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.PluginManager;
import me.nikl.gamebox.nms.NMSUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Niklas Eicker
 */
public class TitleTimer extends BukkitRunnable {
    private String title;
    private long timestamp;
    private Player player;
    private NMSUtil nms;
    private PluginManager pluginManager;

    public TitleTimer(GameBox plugin, String title, Player player, long timestamp){
        this.title = title;
        this.timestamp = timestamp;
        this.player = player;
        this.nms = plugin.getNMS();
        this.pluginManager = plugin.getPluginManager();

        runTaskTimer(plugin, 10,10);
    }


    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();
        if(currentTime > timestamp){
            nms.updateInventoryTitle(player, title);
            pluginManager.removeTitleTimer(player.getUniqueId());
        }
    }
}
