package me.nikl.gamebox.commands;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Language;
import me.nikl.gamebox.Permissions;
import me.nikl.gamebox.PluginManager;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.gui.game.GameGui;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by niklas on 10/17/16.
 *
 * Command class for the essential plugin commands
 */
public class MainCommand implements CommandExecutor{
	private GameBox plugin;
	private FileConfiguration config;
	private PluginManager pManager;
	private GUIManager guiManager;
	private Language lang;

	private Map<String, ArrayList<String>> subCommands = new HashMap<>();
	
	public MainCommand(GameBox plugin){
		this.plugin = plugin;
		this.pManager = plugin.getPluginManager();
		this.guiManager = pManager.getGuiManager();
		this.config = plugin.getConfig();
		this.lang = plugin.lang;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission(Permissions.CMD_MAIN.getPermission())){
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.PREFIX + lang.CMD_NO_PERM));
			return true;
		}
		
		// main cmd without options
		//   sender wants to open main Gui
		//   check ability then open the Gui
		if(args.length == 0) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.PREFIX + lang.CMD_ONLY_PLAYER));
				return true;
			}
			if(plugin.getPluginManager().getDisabledWorlds().contains(((Player) sender).getLocation().getWorld().getName())){
				sender.sendMessage(lang.CMD_DISABLED_WORLD);
				return true;
			}
			Player player = (Player) sender;
			guiManager.openMainGui(player);
			return true;
		} else if(args.length == 1){
			// check weather the given argument is a listed sub command
			for(String id : subCommands.keySet()){
				if(subCommands.get(id).contains(args[0].toLowerCase())){
					if (!(sender instanceof Player)) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.PREFIX + lang.CMD_ONLY_PLAYER));
						return true;
					}
					Player player = (Player) sender;
					String[] arguments = new String[2];
					arguments[0] = id; arguments[1] = GUIManager.MAIN_GAME_GUI;
					guiManager.openGameGui(player, arguments);
					return true;
				}
			}
			// help command
			if(args[0].equalsIgnoreCase("help") || args[0].equals("?")){
				if(!sender.hasPermission(Permissions.CMD_HELP.getPermission())){
					sender.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);
					return true;
				}
				for(String message : lang.CMD_HELP){
					sender.sendMessage(lang.PREFIX + message);
				}
				return true;
			}
			// info command
			if(args[0].equalsIgnoreCase("info")){
				if(!sender.hasPermission(Permissions.CMD_INFO.getPermission())){
					sender.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);
					return true;
				}
				sender.sendMessage(lang.PREFIX + " version " + plugin.getDescription().getVersion() + " by " + ChatColor.GOLD + "Nikl");
				return true;
			}
			// not a listed sub command
			// TodO: reload and other plugin commands
		}
		// wrong usage message
		for(String message : lang.CMD_WRONG_USAGE){
			sender.sendMessage(lang.PREFIX + message);
		}
		return true;
	}

	public void registerSubCommands(String gameID, String... subCommands){
		if(subCommands == null || subCommands.length < 1) return;
		for(int i = 0; i < subCommands.length;i++){
			subCommands[i] = subCommands[i].toLowerCase();
		}
		this.subCommands.put(gameID, new ArrayList<>(Arrays.asList(subCommands)));
	}
}
