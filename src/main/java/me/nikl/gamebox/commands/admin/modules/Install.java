package me.nikl.gamebox.commands.admin.modules;

import co.aikar.commands.annotation.*;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.exceptions.module.GameBoxCloudException;
import me.nikl.gamebox.module.GameBoxModule;
import me.nikl.gamebox.module.ModulesManager;
import me.nikl.gamebox.module.data.CloudModuleData;
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
        if (!Permission.ADMIN_MODULES.hasPermission(sender)) {
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
            } catch (ParseException e) {
                sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_MODULES_INVALID_SEM_VERSION);
                return;
            }
        } else {
            try {
                CloudModuleData cloudModuleData = modulesManager.getCloudService().getModuleData(moduleID);
                semVersion = cloudModuleData.getLatestVersion();
                sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_MODULES_INSTALLING_LATEST_VERSION
                        .replaceAll("%version%", semVersion.toString())
                        .replaceAll("%name%", cloudModuleData.getName()));
            } catch (GameBoxCloudException e) {
                sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_CANNOT_CONNECT_TO_MODULES_CLOUD);
                return;
            }
        }
        try {
            GameBoxModule installedInstance = modulesManager.getModuleInstance(moduleID);
            if (installedInstance != null) {
                sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_MODULES_ALREADY_INSTALLED
                        .replaceAll("%name%", installedInstance.getModuleData().getName())
                        .replaceAll("%id%", moduleID));
                return;
            }
            modulesManager.installModule(moduleID, semVersion);
        } catch (GameBoxCloudException e) {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_CANNOT_CONNECT_TO_MODULES_CLOUD);
        }
    }
}
