package me.nikl.gamebox.guis.gui;

import me.nikl.gamebox.ClickAction;
import me.nikl.gamebox.Main;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.Arrays;

/**
 * Created by niklas on 2/5/17.
 *
 *
 */
public class MainGui extends AGui{
	 
	public MainGui(Main plugin, GUIManager guiManager){
		super(plugin, guiManager);
		this.inventory = Bukkit.createInventory(null, 54);
		
		AButton help = new AButton(new MaterialData(Material.IRON_BLOCK), 1);
		ItemMeta meta = help.getItemMeta();
		meta.setDisplayName("Help");
		meta.setLore(Arrays.asList("Click here to get Help"));
		help.setItemMeta(meta);
		
		help.setAction(ClickAction.NOTHING);
		
		this.inventory.setItem(53, help);
	}
}
