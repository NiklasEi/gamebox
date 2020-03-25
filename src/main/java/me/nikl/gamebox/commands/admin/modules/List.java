package me.nikl.gamebox.commands.admin.modules;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.PreCommand;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.module.cloud.CloudService;
import me.nikl.gamebox.module.data.CloudModuleData;
import me.nikl.gamebox.utility.Permission;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

@CommandAlias("%adminCommand")
public class List extends GameBoxBaseCommand {
    public List(GameBox gameBox) {
        super(gameBox);
    }

    @Override
    @PreCommand
    public boolean preCommand(CommandSender sender) {
        GameBox.debug("in List pre command");
        if (!Permission.ADMIN_MODULES.hasPermission(sender)) {
            sender.sendMessage(gameBox.lang.PREFIX + gameBox.lang.CMD_NO_PERM);
            return true;
        }
        return false;
    }

    @Subcommand("module|modules list|l")
    @CommandCompletion("@moduleIDs")
    public void onModuleUpdate(CommandSender sender) {
        CloudService cloudService = gameBox.getModulesManager().getCloudService();
        java.util.List<CloudModuleData> cloudModuleData = cloudService.getCloudContent();
        HashMap<String, String> context = new HashMap<>();
        context.put("amount", String.valueOf(cloudModuleData.size()));
        gameBox.lang.sendMessage(sender, gameBox.lang.CMD_MODULES_LIST_HEADER, context);
        gameBox.lang.sendMessage(sender, gameBox.lang.CMD_MODULES_LIST_HEADER_SECOND, context);
        for(CloudModuleData moduleData : cloudModuleData) {
            context.put("moduleId", moduleData.getId());
            context.put("moduleName", moduleData.getName());
            context.put("latestVersion", moduleData.getLatestVersion().toString());
            gameBox.lang.sendMessage(sender, gameBox.lang.CMD_MODULES_LIST_ENTRY, context);
        }
        gameBox.lang.sendMessage(sender, gameBox.lang.CMD_MODULES_LIST_FOOTER, context);
    }
}
