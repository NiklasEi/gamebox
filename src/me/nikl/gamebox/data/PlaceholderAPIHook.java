package me.nikl.gamebox.data;

import me.clip.placeholderapi.external.EZPlaceholderHook;
import me.nikl.gamebox.GameBox;
import org.bukkit.entity.Player;

/**
 * Created by Niklas
 *
 * Provide GameBox placeholders through Placeholder API
 */
public class PlaceholderAPIHook extends EZPlaceholderHook{

    private GameBox plugin;

    public PlaceholderAPIHook(GameBox plugin, String identifier) {
        super(plugin, identifier);
        this.plugin = plugin;

        this.hook();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        String gameID = "";

        if(plugin.getPluginManager().getGames().containsKey(identifier.split("_")[identifier.split("_").length - 1])){
            gameID = identifier.split("_")[identifier.split("_").length - 1];

            identifier = identifier.replace("_" + gameID, "");

        }

        switch (identifier){
            case "game_name":
                return plugin.getPluginManager().getGame(gameID).getName();







        }
        return null;
    }
}
