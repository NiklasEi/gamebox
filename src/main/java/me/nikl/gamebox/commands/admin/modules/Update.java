package me.nikl.gamebox.commands.admin.modules;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.PreCommand;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.exceptions.module.CloudModuleNotFoundException;
import me.nikl.gamebox.exceptions.module.CloudModuleVersionNotFoundException;
import me.nikl.gamebox.exceptions.module.GameBoxCloudException;
import me.nikl.gamebox.module.GameBoxModule;
import me.nikl.gamebox.module.ModulesManager;
import me.nikl.gamebox.module.data.CloudModuleData;
import me.nikl.gamebox.utility.Permission;
import me.nikl.gamebox.utility.versioning.SemanticVersion;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.ParseException;

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

    @Subcommand("module|m update|u")
    @CommandCompletion("@loadedModuleIds")
    public void onModuleUpdate(CommandSender sender, @Single String moduleID, @Optional @Single String version) {
        BukkitRunnable update = new BukkitRunnable() {
            @Override
            public void run() {
                SemanticVersion semVersion = null;
                try {
                    ModulesManager modulesManager = gameBox.getModulesManager();
                    CloudModuleData cloudModuleData = modulesManager.getCloudService().getModuleData(moduleID);
                    if (version != null) {
                        try {
                            semVersion = new SemanticVersion(version);
                        } catch (ParseException e) {
                            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_MODULES_INVALID_SEM_VERSION);
                            return;
                        }
                    } else {
                        semVersion = cloudModuleData.getLatestVersion();
                        sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_MODULES_INSTALLING_LATEST_VERSION
                                .replaceAll("%version%", semVersion.toString())
                                .replaceAll("%name%", cloudModuleData.getName()));
                    }
                    GameBoxModule installedInstance = modulesManager.getModuleInstance(moduleID);
                    if (installedInstance == null) {
                        sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_MODULES_NOT_INSTALLED
                                .replaceAll("%id%", moduleID));
                        return;
                    }
                    if (!semVersion.isUpdateFor(installedInstance.getModuleData().getVersionData().getVersion())) {
                        sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_MODULES_NO_UPDATE_AVAILABLE
                                .replaceAll("%name%", installedInstance.getModuleData().getName())
                                .replaceAll("%id%", moduleID));
                        return;
                    }
                    modulesManager.removeModule(installedInstance.getModuleData());
                    sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_MODULES_REMOVE_SUCCESS
                            .replaceAll("%name%", installedInstance.getModuleData().getName())
                            .replaceAll("%version%", installedInstance.getModuleData().getVersionData().getVersion().toString()));
                    modulesManager.installModule(moduleID, semVersion);
                    sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_MODULES_INSTALL_SUCCESS
                            .replaceAll("%name%", cloudModuleData.getName())
                            .replaceAll("%version%", semVersion.toString()));
                } catch (CloudModuleVersionNotFoundException e) {
                    String message = gameBox.lang.CMD_MODULES_VERSION_NOT_FOUND.replaceAll("%id%", moduleID);
                    if (semVersion != null) {
                        message = message.replaceAll("%version%", semVersion.toString());
                    }
                    sender.sendMessage(gameBox.lang.PREFIX + message);
                } catch (CloudModuleNotFoundException e) {
                    sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_CLOUD_MODULE_NOT_FOUND.replaceAll("%id%", moduleID));
                } catch (GameBoxCloudException e) {
                    sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_CANNOT_CONNECT_TO_MODULES_CLOUD);
                }
            }
        };
        update.runTaskAsynchronously(gameBox);
    }
}
