package me.nikl.gamebox.util;

import me.nikl.gamebox.GameBox;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Created by niklas on 10/27/16.
 *
 * easier permission storage
 * just change the permission nodes here
 */
public enum Permission {
	PLAY_SPECIFIC_GAME("play", true), PLAY_ALL_GAMES("play.*")
	, OPEN_GAME_GUI("gamegui", true), OPEN_ALL_GAME_GUI("gamegui.*")
	, USE("use"), ADMIN("admin"), CMD_INFO("info"), CMD_HELP("help")
	, BYPASS_ALL("bypass"), BYPASS_GAME("bypass", true), OPEN_SHOP("shop");
	
	
	private static ArrayList<String> gameIDs  = new ArrayList<>();
	private boolean perGame;
	private String perm;
	private String preNode = "gamebox";
	
	Permission(String perm, boolean perGame){
		this.perm = preNode + "." + perm + (perGame ? ".%gameID%" : "");
		this.perGame = perGame;
	}
	
	Permission(String perm){
		this(perm, false);
	}

	/**
	 * Get the permission corresponding to the enum for the
	 * specified game.
	 * @param gameID ID of the game
	 * @return Permission
	 */
	public String getPermission(String gameID){
		if(!gameIDs.contains(gameID)) Bukkit.getLogger().log(Level.WARNING, "Permission could not find the game: " + gameID);
		if(!perGame) Bukkit.getLogger().log(Level.WARNING, "accessing a per game permission without a gameID");
		return perm.replace("%gameID%", gameID);
	}
	
	public String getPermission(){
		if(perGame) Bukkit.getLogger().log(Level.WARNING, "accessing a per game permission without a gameID");
		return perm;
	}

	public static void addGameID(String gameID){
		Permission.gameIDs.add(gameID);
		GameBox.debug("registered permissions for: " + gameID);
	}
}
