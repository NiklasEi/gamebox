package me.nikl.gamebox.listeners;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.events.EnterGameBoxEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

/**
 * Created by Niklas
 *
 */
public class EnterGameBoxListener implements Listener {
    private GameBox plugin;
    private List<String > commands;

    public EnterGameBoxListener(GameBox plugin){
        this.plugin = plugin;
        if(plugin.getConfig().isSet("listeners.enteringGameBox")){
            ConfigurationSection listener = plugin.getConfig().getConfigurationSection("listeners.enteringGameBox");
            if(listener.isList("commands")){
                this.commands = listener.getStringList("commands");
            }
        }
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEnteringGameBox(EnterGameBoxEvent event){
        if(commands != null) {
            for (String cmd : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", event.getPlayer().getName()));
            }
        }
    }
}
