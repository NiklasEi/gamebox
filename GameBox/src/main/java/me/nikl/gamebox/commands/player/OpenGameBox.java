package me.nikl.gamebox.commands.player;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxLanguage;
import me.nikl.gamebox.PluginManager;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.inventory.GUIManager;
import org.bukkit.entity.Player;

/**
 * @author Niklas Eicker
 */
public class OpenGameBox extends GameBoxBaseCommand {
    private PluginManager pManager;
    private GUIManager guiManager;
    private GameBoxLanguage lang;

    public OpenGameBox(GameBox gameBox) {
        super(gameBox);
        this.pManager = gameBox.getPluginManager();
        this.guiManager = pManager.getGuiManager();
        this.lang = gameBox.lang;
    }

    @CommandAlias("%mainCommand")
    public void openGamebox(Player player){
        if (gameBox.getPluginManager().getBlockedWorlds().contains(player.getLocation().getWorld().getName())) {
            player.sendMessage(lang.PREFIX + lang.CMD_DISABLED_WORLD);
            return;
        }
        guiManager.openMainGui(player);
    }
}
