package me.nikl.gamebox.guis.gui;

import me.nikl.gamebox.ClickAction;
import me.nikl.gamebox.Main;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
		super(plugin, guiManager, 54);
		this.inventory = Bukkit.createInventory(null, 54, "Main gui");
		
		AButton help = new AButton(new MaterialData(Material.IRON_BLOCK), 1);
		// test glow on buttons
		help = (AButton) plugin.getNMS().addGlow(help);
		ItemMeta meta = help.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + "Help");
		meta.setLore(Arrays.asList("Click here to get Help"));
		help.setItemMeta(meta);
		
		help.setAction(ClickAction.NOTHING);

		setButton(help, 53);
	}

	@Override
	public boolean open(Player player){
		if(super.open(player)){
			plugin.getNMS().updateInventoryTitle(player, plugin.lang.TITLE_MAIN_GUI.replace("%player%", player.getName()));
			return true;
		}
		return false;
	}
}
