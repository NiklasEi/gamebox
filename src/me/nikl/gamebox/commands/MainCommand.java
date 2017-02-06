package me.nikl.gamebox.commands;

import me.nikl.gamebox.Language;
import me.nikl.gamebox.Main;
import me.nikl.gamebox.Permissions;
import me.nikl.gamebox.PluginManager;
import me.nikl.gamebox.guis.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * Created by niklas on 10/17/16.
 *
 * Command class for the essential plugin commands
 */
public class MainCommand implements CommandExecutor{
	private Main plugin;
	private FileConfiguration config;
	private PluginManager pManager;
	private GUIManager guiManager;
	private Language lang;
	
	public MainCommand(Main plugin){
		this.plugin = plugin;
		this.pManager = plugin.getPluginManager();
		this.guiManager = plugin.getGuiManager();
		this.config = plugin.getConfig();
		this.lang = plugin.lang;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission(Permissions.CMD_MAIN.getPermission())){
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + lang.CMD_NO_PERM));
			return true;
		}
		
		// main cmd without options
		//   sender wants to open main Gui
		//   check ability then open the Gui
		if(args.length == 0){
			if(!(sender instanceof Player)){
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + lang.CMD_ONLY_PLAYER));
				return true;
			}
			Player player = (Player) sender;
			guiManager.openMainGui(player);
			return true;
		// CMD with options
		}/* else if(args.length == 1){
			
			// option reload
			if(args[0].equalsIgnoreCase("reload")){
				if(!sender.hasPermission(Permissions.CMD_MAIN_RELOAD.getPerm())){
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + lang.CMD_NO_PERM));
					return true;
				}
				plugin.reload();
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + lang.CMD_RELOADED));
				return true;
				
				
			// option help
			} else if(args[0].equalsIgnoreCase("help")){
				for(String message : lang.CMD_HELP){
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + message));
				}
				return true;
			}
		}
		// wrong usage... send messages and return true
		for(String message : lang.CMD_WRONG_USAGE){
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + message));
		}*/
		return true;
	}
}
