package me.nikl.gamebox.commands;

/**
 * Created by niklas on 10/27/16.
 *
 * easier permission storage
 * just change the permission nodes here
 */
public enum Permissions {
	CMD_MAIN_USE("gamebox.use"), CMD_MAIN_RELOAD("gamebox.reload");
	
	public String perm;
	
	Permissions(String perm){
		this.perm = perm;
	}
	
	String getPerm(){
		return this.perm;
	}
}
