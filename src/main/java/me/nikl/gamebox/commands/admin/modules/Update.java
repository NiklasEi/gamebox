package me.nikl.gamebox.commands.admin.modules;

import co.aikar.commands.annotation.*;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.utility.Permission;
import org.bukkit.command.CommandSender;

@CommandAlias("%adminCommand")
public class Update extends GameBoxBaseCommand {
    public Update(GameBox gameBox) {
        super(gameBox);
    }

    @Override
    @PreCommand
    public boolean preCommand(CommandSender sender) {
        GameBox.debug("in Update pre command");
        if (!Permission.ADMIN_MODULES.hasPermission(sender)) {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_NO_PERM);
            return true;
        }
        return false;
    }

    @Subcommand("module update|u")
    @CommandCompletion("@moduleIDs")
    public void onModuleUpdate(CommandSender sender, @Single String moduleID, @Optional @Single String version) {
        sender.sendMessage(gameBox.lang.PREFIX + " Sorry, but this is currently not supported. Make sure GameBox is up to date.");
        sender.sendMessage(gameBox.lang.PREFIX + " Please manually remove the old version from the modules directory, reload gamebox, and use the install command.");
    }
}
