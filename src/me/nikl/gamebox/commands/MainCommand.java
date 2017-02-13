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
			Player player = (Player) sender;
			guiManager.openMainGui(player);
			return true;
		}
		// ToDo: help message
		return true;
	}
}
