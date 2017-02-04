package me.nikl.gamebox.commands;

import me.nikl.gamebox.EnumGames;
import org.bukkit.Bukkit;

/**
 * Created by niklas on 10/27/16.
 *
 * easier permission storage
 * just change the permission nodes here
 */
public enum Permissions {
	CMD_MAIN_USE("gamebox.use"), CMD_MAIN_RELOAD("gamebox.reload"),
	/*
	 Battleship
	 */
	GAME_BATTLESHIP_PLAY("gamebox.battleship.play"), GAME_BATTLESHIP_BYPASS("gamebox.battleship.bypass"),
	/*
	Minesweeper
	*/
	GAME_MINESWEEPER_PLAY("gamebox.minesweeper.play"), GAME_MINESWEEPER_BYPASS("gamebox.minesweeper.bypass"),
	/*
	GemCrush
	*/
	GAME_GEMCRUSH_PLAY("gamebox.gemcrush.play"), GAME_GEMCRUSH_BYPASS("gamebox.gemcrush.bypass"),
	/*
	Tetris
	*/
	GAME_TETRIS_PLAY("gamebox.tetris.play"), GAME_TETRIS_BYPASS("gamebox.tetris.bypass");
	
	public String perm;
	
	Permissions(String perm){
		this.perm = perm;
	}
	
	public static Permissions getGamePerm(EnumGames eGame, String specifyPerm){
		String gameName = eGame.toString();
		for(Permissions perm : Permissions.values()){
			String[] name = perm.name().split("_");
			if(!name[0].equalsIgnoreCase("game")) continue;
			if(!(name.length == 3)) continue;
			if(name[1].equalsIgnoreCase(gameName) && name[2].equalsIgnoreCase(specifyPerm)) return perm;
		}
		return null;
	}
	
	String getPerm(){
		return this.perm;
	}
}
