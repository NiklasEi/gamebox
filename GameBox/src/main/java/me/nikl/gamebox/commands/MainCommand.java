package me.nikl.gamebox.commands;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxLanguage;
import me.nikl.gamebox.Module;
import me.nikl.gamebox.PluginManager;
import me.nikl.gamebox.games.Game;
import me.nikl.gamebox.inventory.GUIManager;
import me.nikl.gamebox.utility.Permission;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

/**
 * Created by niklas on 10/17/16.
 *
 * Command class for the essential plugin commands
 */
public class MainCommand implements CommandExecutor {
    public static final String INVITE_CLICK_COMMAND = UUID.randomUUID().toString();

    private GameBox plugin;
    private PluginManager pManager;
    private GUIManager guiManager;
    private GameBoxLanguage lang;

    public MainCommand(GameBox plugin) {
        this.plugin = plugin;
        this.pManager = plugin.getPluginManager();
        this.guiManager = pManager.getGuiManager();
        this.lang = plugin.lang;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!Permission.USE.hasPermission(sender)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.PREFIX + lang.CMD_NO_PERM));
            return true;
        }
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.PREFIX + lang.CMD_ONLY_PLAYER));
                return true;
            }
            if (plugin.getPluginManager().getBlockedWorlds().contains(((Player) sender).getLocation().getWorld().getName())) {
                sender.sendMessage(lang.PREFIX + lang.CMD_DISABLED_WORLD);
                return true;
            }
            Player player = (Player) sender;
            guiManager.openMainGui(player);
            return true;
        }

        if (args[0].equals(INVITE_CLICK_COMMAND) && sender instanceof Player) {
            if (inviteClickCommand((Player) sender, args)) {
                // gui has been successfully opened
            } else {
                // either faulty arguments or missing permissions
                // error messages are send in GUIManager#openGameGui(Player whoClicked, String... args)
            }
            return true;
        }

        if (args.length == 1) {
            if (checkForSubCommandAndRunIfFound(sender, args)) return true;
            if (args[0].equalsIgnoreCase("help") || args[0].equals("?")) {
                return sendHelp(sender);
            }
            if (args[0].equalsIgnoreCase("info")) {
                return sendInformation(sender);
            }
            // not a listed sub command
        }
        for (String message : lang.CMD_WRONG_USAGE) {
            sender.sendMessage(lang.PREFIX + message);
        }
        return true;
    }

    private boolean checkForSubCommandAndRunIfFound(CommandSender sender, String[] args) {
        Module module = plugin.getGameRegistry().getModuleBySubCommand(args[0]);
        if (module != null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.PREFIX + lang.CMD_ONLY_PLAYER));
                return true;
            }

            // this will be checked again when opening the gui but checking it here
            //   removes the necessity to save and later restore the inventory of the player
            if (!Permission.OPEN_GAME_GUI.hasPermission(sender, module)) {
                sender.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);
                return true;
            }
            Player player = (Player) sender;
            String[] arguments = new String[2];
            arguments[0] = module.getModuleID();
            arguments[1] = GUIManager.MAIN_GAME_GUI;
            guiManager.openGameGui(player, arguments);
            return true;
        }
        return false;
    }

    private boolean sendHelp(CommandSender sender) {
        if (!Permission.CMD_HELP.hasPermission(sender)) {
            sender.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);
            return true;
        }
        for (String message : lang.CMD_HELP) {
            sender.sendMessage(lang.PREFIX + message);
        }
        return true;
    }

    private boolean sendInformation(CommandSender sender) {
        if (!Permission.CMD_INFO.hasPermission(sender)) {
            sender.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);
            return true;
        }
        // send info header
        for (String message : lang.CMD_INFO_HEADER) {
            sender.sendMessage(lang.PREFIX + message);
        }
        String allSubCommands;
        Game game;
        for (String gameID : plugin.getGameRegistry().getModuleIDs()) {

            game = plugin.getPluginManager().getGame(gameID);
            // check whether it's really a game module...
            if (game == null) continue;

            // get subcommands
            Set<String> subCmds = plugin.getGameRegistry().getModuleSubCommands(game.getModule());
            if (subCmds != null && !subCmds.isEmpty()) {
                allSubCommands = String.join(":", subCmds);
            } else {
                allSubCommands = " ";
            }

            // send per game info
            for (String message : lang.CMD_INFO_PER_GAME) {
                sender.sendMessage(lang.PREFIX + message
                        .replace("%name%", ChatColor.stripColor(game.getGameLang().PLAIN_NAME))
                        .replace("%shorts%", allSubCommands)
                        .replace("%playerNum%", String.valueOf(game.getSettings().getGameType().getPlayerNumber())));
            }
        }
        // send info footer
        for (String message : lang.CMD_INFO_FOOTER) {
            sender.sendMessage(lang.PREFIX + message);
        }
        return true;
    }

    /**
     * Open the invitation gui the invite is in
     *
     * @param sender  player
     * @param argsOld old command args containing the clickCommandUUID
     * @return true (permission messages are send)
     */
    private boolean inviteClickCommand(Player sender, String[] argsOld) {
        String[] args = new String[argsOld.length - 1];
        for (int i = 1; i < argsOld.length; i++) {
            args[i - 1] = argsOld[i];
        }
        return guiManager.openGameGui(sender, args);
    }
}
