package me.nikl.gamebox.utility;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Module;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * Created by niklas on 10/27/16.
 *
 * easier permission storage
 * just change the permission nodes here
 */
public enum Permission {
	// ToDo: remove all * perms. Support for those is added in #hasPermission
	PLAY_SPECIFIC_GAME("play", true), PLAY_ALL_GAMES("play.*")
	, OPEN_GAME_GUI("gamegui", true), OPEN_ALL_GAME_GUI("gamegui.*")
	, USE("use"), ADMIN("admin"), CMD_INFO("info"), CMD_HELP("help")
	, BYPASS_ALL("bypass"), BYPASS_GAME("bypass", true), OPEN_SHOP("shop");
	
	private static ArrayList<String> gameIDs  = new ArrayList<>();
	private boolean perGame;
	private String perm;
	private String preNode = GameBox.MODULE_GAMEBOX;
	
	Permission(String perm, boolean perGame){
		this.perm = preNode + "." + perm + (perGame ? ".%gameID%" : "");
		this.perGame = perGame;
	}
	
	Permission(String perm){
		this(perm, false);
	}

	// ToDO: make private (and remove) and change usage to #hasPermission
	public String getPermission(String gameID){
		return perm.replace("%gameID%", gameID);
	}

	// ToDO: make private (and remove) and change usage to #hasPermission
	public String getPermission(){
		return perm;
	}

	public static void addGameID(String gameID){
		Permission.gameIDs.add(gameID);
		GameBox.debug("registered permissions for: " + gameID);
	}

    public String getPermission(Module module) {
		return getPermission(module.getModuleID());
    }

    public boolean hasPermission(CommandSender sender, @Nullable String gameID){
		if(gameID == null){
			return hasPermission(sender);
		} else {
			if (!gameIDs.contains(gameID)) {
				throw new IllegalArgumentException("Unknown gameID: " + gameID);
			}
			return ( sender.hasPermission(perm.replace("%gameID%", gameID))
					|| sender.hasPermission(perm.replace("%gameID%", "*")));
		}
	}

	public boolean hasPermission(CommandSender sender){
		if(perGame) throw new IllegalArgumentException("Accessing a per-game permission without a gameID");
		return sender.hasPermission(perm);
	}
}
