package me.nikl.gamebox.games.tetris;

import me.nikl.gamebox.Main;
import me.nikl.gamebox.games.minesweeper.MinesweeperGameManager;
import me.nikl.gamebox.guis.AGameGUIButton;
import me.nikl.gamebox.guis.IGui;
import me.nikl.gamebox.guis.gameguis.AGameGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Created by niklas on 10/18/16.
 *
 *
 */
public class TetrisGameGUI extends AGameGUI {
	
	public TetrisGameGUI(Main plugin, TetrisGameManager gManager) {
		super(plugin, gManager);
		this.parentGui = null;
		this.childGui = null;
		
		ArrayList<String> lore = new ArrayList<>();
		lore.add(ChatColor.translateAlternateColorCodes('&',"&2Start a game"));
		lore.add(ChatColor.translateAlternateColorCodes('&',""));
		lore.add(ChatColor.translateAlternateColorCodes('&',"&4Cost: &1" + gManager.getPrice()));
		
		AGameGUIButton playButton = new AGameGUIButton(gManager) {
			
			@Override
			public void onClick(InventoryClickEvent event, IGui gui) {
				if(gameManager.startGame((Player) event.getWhoClicked())){
					gui.removePlayer(event.getWhoClicked().getUniqueId());
				}
			}
		};
		playButton.item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 2);
		ItemMeta meta = playButton.item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&1Play"));
		meta.setLore(lore);
		playButton.item.setItemMeta(meta);
		addToGrid(playButton);
	}
}
