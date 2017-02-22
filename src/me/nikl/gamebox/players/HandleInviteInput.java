package me.nikl.gamebox.players;

import me.nikl.gamebox.GameBox;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Created by Niklas on 22.02.2017.
 *
 */
public class HandleInviteInput extends BukkitRunnable{
    private Map<UUID, Waiting> waitings = new HashMap<>();
    private GameBox plugin;

    public HandleInviteInput(GameBox plugin){
        this.plugin = plugin;
        this.runTaskTimerAsynchronously(plugin, 20, 20);
    }

    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();
        Set<UUID> removing = new HashSet<>();
        Iterator<Waiting> it = waitings.values().iterator();
        while(it.hasNext()){
            Waiting waiting = it.next();
            if(waiting.timestamp < currentTime){
                removing.add(waiting.uuid);
                it.remove();
            }
        }
        Player player;
        for(UUID uuid: removing){
            player = Bukkit.getPlayer(uuid);
            if(player != null){
                player.sendMessage(plugin.lang.PREFIX + plugin.lang.INPUT_TIME_RAN_OUT);
            }
        }
    }

    public void onChat(AsyncPlayerChatEvent event){
        if(!waitings.keySet().contains(event.getPlayer().getUniqueId())) return;

        String message = event.getMessage();

        if(message.equals("%exit")){
            event.getPlayer().sendMessage("closed input");
            waitings.remove(event.getPlayer().getUniqueId());
            return;
        }

        if(message.split(" ").length > 1){
            event.setCancelled(true);
            event.getPlayer().sendMessage(" not a valid player name");
            return;
        }

        Player player = Bukkit.getPlayer(message);

        if(player == null){
            event.setCancelled(true);
            event.getPlayer().sendMessage(" Could not find the player: " + message);
            event.getPlayer().sendMessage(" Maybe he is offline?");
            return;
        }

        event.setCancelled(true);
        if(player.getUniqueId().equals(event.getPlayer().getUniqueId())){
            event.getPlayer().sendMessage(" You cannot invite yourself " + message);
        }

        Waiting waiting = waitings.get(event.getPlayer().getUniqueId());
        // invite successfull
        plugin.getPluginManager().getHandleInvitations().addInvite(event.getPlayer().getUniqueId(), player.getUniqueId(), System.currentTimeMillis() + 15*1000, waiting.args);
    }

    public boolean addWaiting(UUID uuid, long timeStamp, String... args){
        if(waitings.keySet().contains(uuid)) return false;
        new Waiting(uuid, timeStamp, args);
        return true;
    }


    public class Waiting{
        protected UUID uuid;
        protected long timestamp;
        protected String[] args;

        public Waiting(UUID player, long timestampUntil, String... args){
            this.uuid = player;
            this.timestamp = timestampUntil;
            this.args = args;

            waitings.put(player, this);
        }
    }
}
