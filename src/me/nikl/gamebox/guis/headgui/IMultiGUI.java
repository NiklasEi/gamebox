package me.nikl.gamebox.guis.headgui;

import me.nikl.gamebox.guis.IGui;

import java.util.UUID;

/**
 * Created by niklas on 10/30/16.
 *
 *
 */
public interface IMultiGUI extends IGui {
	void nextPage(UUID uuid);
	
	void lastPage(UUID uuid);
}
