package me.nikl.gamebox.guis;

import me.nikl.gamebox.games.IGameManager;

/**
 * Created by niklas on 10/29/16.
 *
 * Buttons for game guis
 * saving the gamemanager here
 */
public abstract class AGameGUIButton extends AButton{
	private IGameManager gameManager;
	
	public AGameGUIButton(IGameManager gameManager){
		this.gameManager = gameManager;
	}
	
}
