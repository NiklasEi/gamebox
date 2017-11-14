package me.nikl.gamebox.util;

/**
 * Created by niklas on 2/5/17.
 *
 *
 */
public enum ClickAction {
	START_GAME,
	OPEN_GAME_GUI,
	OPEN_MAIN_GUI,
	/*
	This runs the same action as
	OPEN_GAME_GUI
	and should be replaced by it
	 */
	@Deprecated
	CHANGE_GAME_GUI,
	NOTHING,
	CLOSE,
	TOGGLE,
	SHOW_TOP_LIST,
	START_PLAYER_INPUT,
	BUY,
	OPEN_SHOP_PAGE
}
