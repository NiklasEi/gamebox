package me.nikl.gamebox.games.tetris;

import me.nikl.gamebox.Main;
import me.nikl.gamebox.games.minesweeper.MinesweeperGameManager;
import me.nikl.gamebox.guis.gameguis.AGameGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

/**
 * Created by niklas on 10/18/16.
 *
 *
 */
public class TetrisGameGUI extends AGameGUI {
	private Inventory gui;
	
	
	public TetrisGameGUI(Main plugin, TetrisGameManager gManager) {
		super(plugin, gManager);
		this.parentGui = null;
		this.childGui = null;
		
		this.gameManager = gManager;
		
		ArrayList<String> lore = new ArrayList<>();
		lore.add(ChatColor.translateAlternateColorCodes('&',"&2Start a game"));
		lore.add(ChatColor.translateAlternateColorCodes('&',""));
		lore.add(ChatColor.translateAlternateColorCodes('&',"&4Cost: &1" + gManager.getPrice()));
		
		//setPlayButton(Material.SAND, "&1Play Tetris", lore);
	}
}
