package me.nikl.gamebox.listeners;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.events.LeftGameBoxEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;

import java.util.List;

/**
 * Created by Niklas
 */
public class LeftGameBoxListener extends GameBoxListener {
    private List<String> commands;

    public LeftGameBoxListener(GameBox gameBox) {
        super(gameBox);
        if (gameBox.getConfig().isSet("listeners.leftGameBox")) {
            ConfigurationSection listener = gameBox.getConfig().getConfigurationSection("listeners.leftGameBox");
            if (listener.isList("commands")) {
                this.commands = listener.getStringList("commands");
            }
        }
    }

    @EventHandler
    public void onLeftGameBox(LeftGameBoxEvent event) {
        if (commands != null) {
            for (String cmd : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", event.getPlayer().getName()));
            }
        }
    }
}
