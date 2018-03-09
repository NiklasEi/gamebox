package me.nikl.gamebox.commands.player;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.inventory.GUIManager;
import org.bukkit.entity.Player;

/**
 * @author Niklas Eicker
 */
@CommandAlias("%mainCommand")
public class InvitationClickCommand extends GameBoxBaseCommand {
    private GUIManager guiManager;

    public InvitationClickCommand(GameBox gameBox) {
        super(gameBox);
        this.guiManager = gameBox.getPluginManager().getGuiManager();
    }

    @Subcommand("%INVITE_CLICK_COMMAND")
    public void onInvitationMessageClick(Player player, String... args) {
        /*String[] args = new String[argsOld.length - 1];
        for (int i = 1; i < argsOld.length; i++) {
            args[i - 1] = argsOld[i];
        }*/
        guiManager.openGameGui(player, args);
    }
}
