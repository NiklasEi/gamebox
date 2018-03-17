package me.nikl.gamebox.listeners;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.events.EnterGameBoxEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;

import java.util.List;

/**
 * @author Niklas Eicker
 */
public class EnterGameBoxListener extends GameBoxListener {
    private List<String> commands;

    public EnterGameBoxListener(GameBox gameBox) {
        super(gameBox);
        if (gameBox.getConfig().isSet("listeners.enteringGameBox")) {
            ConfigurationSection listener = gameBox.getConfig().getConfigurationSection("listeners.enteringGameBox");
            if (listener.isList("commands")) {
                this.commands = listener.getStringList("commands");
            }
        }
    }

    @EventHandler
    public void onEnteringGameBox(EnterGameBoxEvent event) {
        if (commands != null) {
            for (String cmd : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", event.getPlayer().getName()));
            }
        }
    }
}
