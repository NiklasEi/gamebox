package me.nikl.gamebox.commands.admin.modules;

import co.aikar.commands.annotation.*;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.exceptions.module.GameBoxCloudException;
import me.nikl.gamebox.exceptions.module.ModuleVersionException;
import me.nikl.gamebox.module.ModulesManager;
import me.nikl.gamebox.utility.Permission;
import me.nikl.gamebox.utility.versioning.SemanticVersion;
import org.bukkit.command.CommandSender;

import java.text.ParseException;

@CommandAlias("%adminCommand")
public class Install extends GameBoxBaseCommand {
    public Install(GameBox gameBox) {
        super(gameBox);
    }

    @Override
    @PreCommand
    public boolean preCommand(CommandSender sender) {
        GameBox.debug("in Install pre command");
        if (!Permission.ADMIN_MODULES_INSTALL.hasPermission(sender)) {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_NO_PERM);
            return true;
        }
        return false;
    }

    @Subcommand("module install|i")
    public void onModuleInstall(CommandSender sender, @Single String moduleID, @Optional @Single String version) {
        SemanticVersion semVersion;
        ModulesManager modulesManager = gameBox.getModulesManager();
        if (version != null) {
            try {
                semVersion = new SemanticVersion(version);
                modulesManager.installModule(moduleID, semVersion);
            } catch (ParseException e) {
                sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_MODULES_INVALID_SEM_VERSION);
                return;
            } catch (GameBoxCloudException e) {
                sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_CANNOT_CONNECT_TO_MODULES_CLOUD);
                return;
            }
        }
        try {
            SemanticVersion latestVersion = modulesManager.getCloudService().getModuleData(moduleID).getLatestVersion();
            sender.sendMessage(gameBox.lang.PREFIX + " attempting to install latest version (" + latestVersion.toString() + ")");
            modulesManager.installModule(moduleID, latestVersion);
        } catch (GameBoxCloudException e) {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_CANNOT_CONNECT_TO_MODULES_CLOUD);
        }
    }
}
