package me.nikl.gamebox.inventory;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.inventory.gui.AGui;
import me.nikl.gamebox.inventory.gui.game.GameGui;
import me.nikl.gamebox.inventory.gui.game.GameGuiPage;
import me.nikl.gamebox.inventory.shop.Page;
import me.nikl.gamebox.nms.NmsFactory;
import me.nikl.gamebox.nms.NmsUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Niklas Eicker
 */
public class InventoryTitleMessenger extends BukkitRunnable {
    private GameBox gameBox;
    private NmsUtility nmsUtility;
    private Map<UUID, Long> messageTimeStamps = new HashMap<>();
    private Map<UUID, String> resetTitles = new HashMap<>();

    public InventoryTitleMessenger(GameBox gameBox){
        this.gameBox = gameBox;
        nmsUtility = NmsFactory.getNmsUtility();
        runTaskTimer(gameBox, 10, 10);
    }

    @Override
    public void run() {
        long currentTimeMillis = System.currentTimeMillis();
        for(UUID uuid : messageTimeStamps.keySet()){
            if(currentTimeMillis > messageTimeStamps.get(uuid)) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) {
                    removeTitleMessage(uuid);
                    continue;
                }
                nmsUtility.updateInventoryTitle(player, resetTitles.get(uuid));
                removeTitleMessage(uuid);
            }
        }
    }

    public void removeTitleMessage(UUID uuid){
        messageTimeStamps.remove(uuid);
        resetTitles.remove(uuid);
    }

    public void sendInventoryTitle(Player player, String message, String title, int duration){
        nmsUtility.updateInventoryTitle(player, message);
        messageTimeStamps.put(player.getUniqueId(), System.currentTimeMillis() + duration*1000);
        resetTitles.put(player.getUniqueId(), title);
    }

    public void sendInventoryTitle(Player player, String message, int duration) {
        String currentTitle = gameBox.lang.TITLE_MAIN_GUI.replace("%player%", player.getName());
        AGui gui = gameBox.getPluginManager().getGuiManager().getCurrentGui(player.getUniqueId());
        if (gui != null) {
            if (gui instanceof GameGuiPage) {
                currentTitle = ((GameGuiPage) gui).getTitle().replace("%player%", player.getName());
            } else if (gui instanceof GameGui) {
                currentTitle = gameBox.lang.TITLE_GAME_GUI.replace("%game%", gameBox.getPluginManager().getGame(player.getUniqueId()).getGameLang().PLAIN_NAME).replace("%player%", player.getName());
            } else if (gui instanceof Page){
                currentTitle = gameBox.lang.SHOP_TITLE_PAGE_SHOP.replace("%page%", String.valueOf(((Page)gui).getPage() + 1));
            }
        }
        sendInventoryTitle(player, message, currentTitle, duration);
    }

}
