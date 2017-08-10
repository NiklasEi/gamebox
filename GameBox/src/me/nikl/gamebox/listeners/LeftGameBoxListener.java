package me.nikl.gamebox.listeners;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.events.LeftGameBoxEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

/**
 * Created by Niklas
 *
 *
 */
public class LeftGameBoxListener implements Listener {
    private GameBox plugin;
    private List<String > commands;

    public LeftGameBoxListener(GameBox plugin){
        this.plugin = plugin;

        if(plugin.getConfig().isSet("listeners.leftGameBox")){
            ConfigurationSection listener = plugin.getConfig().getConfigurationSection("listeners.leftGameBox");
            if(listener.isList("commands")){
                this.commands = listener.getStringList("commands");
            }
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler
    public void onLeftGameBox(LeftGameBoxEvent event){

        if(commands != null) {
            for (String cmd : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", event.getPlayer().getName()));
            }
        }

    }
}
