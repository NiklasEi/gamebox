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

    public HandleInviteInput(GameBox plugin){
        this.runTaskTimerAsynchronously(plugin, 20, 20);
    }

    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();
        Set<UUID> removing = new HashSet<>();
        Iterator<Waiting> it = waitings.values().iterator();
        while(it.hasNext()){
            Waiting waiting = it.next();
            removing.add(waiting.uuid);
            if(waiting.timestamp < currentTime) it.remove();
        }
    }

    public void onChat(AsyncPlayerChatEvent event){
        if(!waitings.keySet().contains(event.getPlayer().getUniqueId())) return;
        
        String message = event.getMessage();
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
        // invite successfull
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
