package me.nikl.gamebox.input;

import me.nikl.gamebox.GameBox;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Niklas Eicker
 */
public class HandleInviteInput extends BukkitRunnable {
    private Map<UUID, Waiting> waitings = new HashMap<>();
    private GameBox plugin;

    public HandleInviteInput(GameBox plugin) {
        this.plugin = plugin;
        this.runTaskTimerAsynchronously(plugin, 20, 20);
    }

    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();
        Set<UUID> removing = new HashSet<>();
        Iterator<Waiting> it = waitings.values().iterator();
        while (it.hasNext()) {
            Waiting waiting = it.next();
            if (waiting.timestamp < currentTime) {
                removing.add(waiting.uuid);
                it.remove();
            }
        }
        Player player;
        for (UUID uuid : removing) {
            player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(plugin.lang.PREFIX + plugin.lang.INPUT_TIME_RAN_OUT);
            }
        }
    }

    public void onChat(AsyncPlayerChatEvent event) {
        if (!waitings.keySet().contains(event.getPlayer().getUniqueId())) return;
        String message = event.getMessage();
        event.setCancelled(true);
        Player player = event.getPlayer();
        // go back on main thread from the async event
        new BukkitRunnable() {
            @Override
            public void run() {
                if (message == null || player == null || !player.isOnline() || !waitings.containsKey(player.getUniqueId()))
                    return;
                if (message.contains("%")) {
                    player.sendMessage(plugin.lang.PREFIX + plugin.lang.INPUT_CLOSED);
                    waitings.remove(player.getUniqueId());
                    return;
                }
                if (message.split(" ").length > 1) {
                    player.sendMessage(plugin.lang.INVITATION_NOT_VALID_PLAYER_NAME.replace("%player%", message));
                    return;
                }
                // color strip only for compatibility with plugins that color player names in chat
                Player player2 = Bukkit.getPlayer(ChatColor.stripColor(message));
                if (player2 == null) {
                    player.sendMessage(plugin.lang.INVITATION_NOT_ONLINE.replace("%player%", message));
                    return;
                }
                UUID uuid = player.getUniqueId();
                if (player2.getUniqueId().equals(uuid)) {
                    player.sendMessage(plugin.lang.INVITATION_NOT_YOURSELF);
                    return;
                }
                Waiting waiting = waitings.get(uuid);
                // invite successful
                if (plugin.getPluginManager().getHandleInvitations().addInvite(uuid, player2.getUniqueId(), System.currentTimeMillis() + 15 * 1000, waiting.args)) {
                    player.sendMessage(plugin.lang.PREFIX + plugin.lang.INVITATION_SUCCESSFUL.replace("%player%", player2.getName()));
                    waitings.remove(uuid);
                }
            }
        }.runTask(plugin);
    }

    public boolean addWaiting(UUID uuid, long timeStamp, String... args) {
        if (waitings.keySet().contains(uuid)) return false;
        new Waiting(uuid, timeStamp, args);
        return true;
    }


    public class Waiting {
        protected UUID uuid;
        protected long timestamp;
        protected String[] args;

        public Waiting(UUID player, long timestampUntil, String... args) {
            this.uuid = player;
            this.timestamp = timestampUntil;
            this.args = args;
            waitings.put(player, this);
        }
    }
}
