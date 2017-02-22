package me.nikl.gamebox.players;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Created by Niklas on 22.02.2017.
 *
 *
 */
public class HandleInvitations extends BukkitRunnable{
    private Set<Invitation> invitations = new HashSet<>();
    private PluginManager pluginManager;

    public HandleInvitations(GameBox plugin){
        pluginManager = plugin.getPluginManager();
        this.runTaskTimerAsynchronously(plugin, 20, 10);
    }

    @Override
    public void run() {
        Iterator<Invitation> it = invitations.iterator();
        ArrayList<UUID> ranOut = new ArrayList<>();
        while (it.hasNext()){
            Invitation inv = it.next();
            long currentTimeMillis = System.currentTimeMillis();
            if(currentTimeMillis > inv.timeStamp){
                ranOut.add(inv.player1);
                it.remove();
                GameBox.debug("removing old invitation");
            }
        }
    }


    public boolean addInvite(UUID player1, UUID player2, long timeStamp, String... args){
        for(Invitation inv : invitations){
            if(inv.player1.equals(player1) && inv.player2.equals(player2)){
                GameBox.debug("There is already such an invitation");
                //ToDo send message
                return false;
            }
            if(inv.player1.equals(player2) && inv.player2.equals(player1)){
                GameBox.debug("There is already an opposite inv. ToDo: accept");
                //ToDO: accept invite automatically
                return false;
            }
        }
        new Invitation(player1, player2, timeStamp, args);
        return true;
    }

    public class Invitation{
        protected long timeStamp;
        protected UUID player1, player2;
        protected String[] args;

        public Invitation(UUID player1, UUID player2, long timeStamp, String... args){
            this.player1 = player1;
            this.player2 = player2;
            this.timeStamp = timeStamp;
            this.args = args;

            GameBox.debug("adding new invitation");
            invitations.add(this);
        }
    }
}
