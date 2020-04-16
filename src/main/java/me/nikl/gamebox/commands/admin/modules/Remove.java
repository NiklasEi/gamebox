package me.nikl.gamebox.commands.admin.modules;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.PreCommand;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.module.GameBoxModule;
import me.nikl.gamebox.module.ModulesManager;
import me.nikl.gamebox.utility.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

@CommandAlias("%adminCommand")
public class Remove extends GameBoxBaseCommand {
    public Remove(GameBox gameBox) {
        super(gameBox);
    }

    @Override
    @PreCommand
    public boolean preCommand(CommandSender sender) {
        GameBox.debug("in Remove pre command");
        if (!Permission.ADMIN_MODULES.hasPermission(sender)) {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_NO_PERM);
            return true;
        }
        return false;
    }

    @Subcommand("module|m remove|rm")
    @CommandCompletion("@loadedModuleIds")
    public void onModuleRemove(CommandSender sender, @Single String moduleID) {
        BukkitRunnable remove = new BukkitRunnable() {
            @Override
            public void run() {
                    ModulesManager modulesManager = gameBox.getModulesManager();
                    GameBoxModule installedInstance = modulesManager.getModuleInstance(moduleID);
                    if (installedInstance == null) {
                        sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_MODULES_NOT_INSTALLED
                                .replaceAll("%id%", moduleID));
                        return;
                    }
                    modulesManager.removeModule(installedInstance.getModuleData());
                    sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_MODULES_REMOVE_SUCCESS
                            .replaceAll("%name%", installedInstance.getModuleData().getName())
                            .replaceAll("%version%", installedInstance.getModuleData().getVersionData().getVersion().toString()));
            }
        };
        remove.runTaskAsynchronously(gameBox);
    }
}
