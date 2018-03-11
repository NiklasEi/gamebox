package me.nikl.gamebox.commands.player;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.UnknownHandler;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxLanguage;
import me.nikl.gamebox.Module;
import me.nikl.gamebox.PluginManager;
import me.nikl.gamebox.inventory.GUIManager;
import me.nikl.gamebox.utility.Permission;
import org.bukkit.entity.Player;

/**
 * @author Niklas Eicker
 */
@CommandAlias("%mainCommand")
public class OpenGameBox extends PlayerBaseCommand {
    private PluginManager pManager;
    private GUIManager guiManager;
    private GameBoxLanguage lang;

    public OpenGameBox(GameBox gameBox) {
        super(gameBox);
        this.pManager = gameBox.getPluginManager();
        this.guiManager = pManager.getGuiManager();
        this.lang = gameBox.lang;
    }

    @Default
    @UnknownHandler
    public void openGamebox(Player player, @Optional String subCommand){
        if (subCommand == null || subCommand.isEmpty()) {
            guiManager.openMainGui(player);
            return;
        }
        Module module = gameBox.getGameRegistry().getModuleBySubCommand(subCommand);
        if (module != null) {
            // this will be checked again when opening the gui but checking it here
            //   removes the necessity to save and later restore the inventory of the player
            if (!Permission.OPEN_GAME_GUI.hasPermission(player, module)) {
                player.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);
                return;
            }
            String[] arguments = new String[2];
            arguments[0] = module.getModuleID();
            arguments[1] = GUIManager.MAIN_GAME_GUI;
            guiManager.openGameGui(player, arguments);
            return;
        }
        guiManager.openMainGui(player);
    }
}
