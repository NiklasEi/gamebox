package me.nikl.gamebox;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Created by niklas on 10/27/16.
 *
 * easier permission storage
 * just change the permission nodes here
 */
public enum Permissions {
	SEE_GAME("see", true), PLAY_GAME("play", true), OPEN_GAME_GUI("gamegui", true), CMD_MAIN("use");
	
	
	private static ArrayList<String> gameIDs;
	private boolean perGame;
	private String perm;
	private String preNode = "gamebox";
	
	Permissions(String perm, boolean perGame){
		this.perm = preNode + "." + perm + (perGame ? ".%gameID%" : "");
		this.perGame = perGame;
		
	}
	
	Permissions(String perm){
		this(perm, false);
	}
	
	public String getPermission(String gameID){
		if(!Permissions.gameIDs.contains(gameID)) Bukkit.getLogger().log(Level.WARNING, "No such game found: " + gameID);
		if(!perGame) Bukkit.getLogger().log(Level.WARNING, "accessing a per game permission without a gameID");
		return perm.replace("%gameID%", gameID);
	}
	
	public String getPermission(){
		if(perGame) Bukkit.getLogger().log(Level.WARNING, "accessing a per game permission without a gameID");
		return perm;
	}
	
	public void addGameID(String gameID){
		if(Permissions.gameIDs == null){
			Permissions.gameIDs = new ArrayList<>();
		}
		Permissions.gameIDs.add(gameID);
	}
}
