package me.nikl.gamebox.commands;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxLanguage;
import me.nikl.gamebox.PluginManager;
import me.nikl.gamebox.games.Game;
import me.nikl.gamebox.inventory.GUIManager;
import me.nikl.gamebox.util.Permission;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
	private PluginManager pManager;
	private GUIManager guiManager;
	private GameBoxLanguage lang;

	public static String inviteClickCommand = "clickableMessageWasClicked";

	private Map<String, ArrayList<String>> subCommands = new HashMap<>();
	
	public MainCommand(GameBox plugin){
		this.plugin = plugin;
		this.pManager = plugin.getPluginManager();
		this.guiManager = pManager.getGuiManager();
		this.lang = plugin.lang;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission(Permission.USE.getPermission())){
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
			if (plugin.getPluginManager().getDisabledWorlds().contains(((Player) sender).getLocation().getWorld().getName())) {
				sender.sendMessage(lang.PREFIX + lang.CMD_DISABLED_WORLD);
				return true;
			}
			Player player = (Player) sender;

			guiManager.openMainGui(player);
			return true;


		}

		/*
				At least one argument
		 */

		// click command
		if (args[0].equalsIgnoreCase(inviteClickCommand) && sender instanceof Player){
			// handle click commands from clickable invite messages
			if(inviteClickCommand((Player) sender, args)){
				// gui has been successfully opened
			} else {
				// either faulty arguments or missing permissions
				// error messages are send in GUIManager#openGameGui(Player whoClicked, String... args)
			}
			return true;
		}

		// sub commands
		if(args.length == 1){
			// check whether the given argument is a listed sub command
			for(String id : subCommands.keySet()){
				if(subCommands.get(id) == null) continue;
				if(subCommands.get(id).contains(args[0].toLowerCase())){
					if (!(sender instanceof Player)) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.PREFIX + lang.CMD_ONLY_PLAYER));
						return true;
					}

					// this will be checked again when opening the gui but checking it here
					//   removes the necessity to save and later restore the inventory of the player
					if(!sender.hasPermission(Permission.OPEN_ALL_GAME_GUI.getPermission()) && !sender.hasPermission(Permission.OPEN_GAME_GUI.getPermission(id))){
						sender.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);
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
				if(!sender.hasPermission(Permission.CMD_HELP.getPermission())){
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
				if(!sender.hasPermission(Permission.CMD_INFO.getPermission())){
					sender.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);
					return true;
				}
				sender.sendMessage(lang.PREFIX + " version " + plugin.getDescription().getVersion() + " by " + ChatColor.GOLD + "Nikl");
				sender.sendMessage(lang.PREFIX);

				// send info header
				for(String message : lang.CMD_INFO_HEADER){
					sender.sendMessage(lang.PREFIX + message);
				}

				String allSubCommands;
				Game game;
				for(String gameID : subCommands.keySet()) {

					game = plugin.getPluginManager().getGame(gameID);
					if (game == null) continue;

					// get subcommands
					if(subCommands.get(gameID) != null && !subCommands.get(gameID).isEmpty()){
						allSubCommands = "";
						for(String sub : subCommands.get(gameID)){
							if(allSubCommands.length() != 0) allSubCommands += ":";
							allSubCommands += sub;
						}
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
				for(String message : lang.CMD_INFO_FOOTER){
					sender.sendMessage(lang.PREFIX + message);
				}
				return true;
			}
			// not a listed sub command
		}
		// wrong usage message
		for(String message : lang.CMD_WRONG_USAGE){
			sender.sendMessage(lang.PREFIX + message);
		}
		return true;
	}

	/**
	 * Open the invitation gui the invite is in
	 * @param sender player
	 * @param argsOld old command args containing the clickCommandUUID
	 * @return true (permission messages are send)
	 */
	private boolean inviteClickCommand(Player sender, String[] argsOld) {
		String[] args = new String[argsOld.length - 1];
		for(int i = 1; i < argsOld.length; i++){
			args[i-1] = argsOld[i];
		}
		return guiManager.openGameGui(sender, args);
	}

	public void registerSubCommands(String gameID, String... subCommands){
		if(subCommands == null || subCommands.length < 1){
			this.subCommands.put(gameID, null);
			return;
		}
		for(int i = 0; i < subCommands.length;i++){
			subCommands[i] = subCommands[i].toLowerCase();
		}
		this.subCommands.put(gameID, new ArrayList<>(Arrays.asList(subCommands)));
	}
}
