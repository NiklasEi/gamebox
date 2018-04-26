package me.nikl.gamebox.commands.player;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Private;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.inventory.GUIManager;
import org.bukkit.entity.Player;

/**
 * @author Niklas Eicker
 */
@CommandAlias("%mainCommand")
public class InvitationClickCommand extends PlayerBaseCommand {
    private GUIManager guiManager;

    public InvitationClickCommand(GameBox gameBox) {
        super(gameBox);
        this.guiManager = gameBox.getPluginManager().getGuiManager();
    }

    @Subcommand("%INVITE_CLICK_COMMAND")
    @Private
    public void onInvitationMessageClick(Player player, String[] args) {
        guiManager.openGameGui(player, args);
    }
}
