package me.nikl.gamebox.guis.gui;

import com.sun.org.apache.xpath.internal.operations.Bool;
import me.nikl.gamebox.ClickAction;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import me.nikl.gamebox.guis.button.ToggleButton;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.*;

/**
 * Created by niklas on 2/5/17.
 *
 *
 */
public class MainGui extends AGui{
	private Map<UUID, ToggleButton> soundButtons = new HashMap<>();

	private int soundToggleSlot = 52;
	 
	public MainGui(GameBox plugin, GUIManager guiManager){
		super(plugin, guiManager, 54);
		this.inventory = Bukkit.createInventory(null, 54, "GameBox gui");
		
		AButton help = new AButton(new MaterialData(Material.IRON_BLOCK), 1);
		// test glow on buttons
		help = (AButton) plugin.getNMS().addGlow(help);
		ItemMeta meta = help.getItemMeta();
		if(plugin.lang.BUTTON_MAIN_MENU_INFO != null) {
			if(plugin.lang.BUTTON_MAIN_MENU_INFO.size() > 0)meta.setDisplayName(plugin.lang.BUTTON_MAIN_MENU_INFO.get(0));
			if(plugin.lang.BUTTON_MAIN_MENU_INFO.size() > 1){
				ArrayList<String> lore = new ArrayList<>(plugin.lang.BUTTON_MAIN_MENU_INFO);
				lore.remove(0);
				meta.setLore(lore);
			}
		}
		help.setItemMeta(meta);
		help.setAction(ClickAction.NOTHING);
		setButton(help, 53);


		AButton soundToggle = new AButton(new MaterialData(Material.RECORD_6), 1);
		meta = soundToggle.getItemMeta();
		meta.addItemFlags(ItemFlag.values());
		meta.setDisplayName(ChatColor.BLUE+"Sound on");
		meta.setLore(Arrays.asList(" ", ChatColor.BLUE+"Click to turn sounds off"));
		soundToggle.setItemMeta(meta);
		soundToggle.setAction(ClickAction.TOGGLE);
		soundToggle.setArgs("sound");
		setButton(soundToggle, soundToggleSlot);




		Map<Integer, ItemStack> hotBarButtons = plugin.getPluginManager().getHotBarButtons();

		// set lower grid
		AButton exit = new AButton(hotBarButtons.get(4).getData(), 1);
		meta = hotBarButtons.get(4).getItemMeta();
		exit.setItemMeta(meta);
		exit.setAction(ClickAction.CLOSE);
		setLowerButton(exit, 4);
	}


	@Override
	public boolean open(Player player){

		ToggleButton soundToggle = new ToggleButton(new MaterialData(Material.RECORD_6), 1, new MaterialData(Material.RECORD_4));
		ItemMeta meta = soundToggle.getItemMeta();
		meta.addItemFlags(ItemFlag.values());
		meta.setDisplayName(ChatColor.BLUE+"Sound on");
		meta.setLore(Arrays.asList(" ", ChatColor.BLUE+"Click to turn sounds off"));
		soundToggle.setItemMeta(meta);
		soundToggle.setToggleDisplayName(ChatColor.RED+"Sound off");
		soundToggle.setToggleLore(Arrays.asList(" ", ChatColor.BLUE+"Click to turn sounds on"));
		soundToggle.setAction(ClickAction.TOGGLE);
		soundToggle.setArgs("sound");
		soundButtons.put(player.getUniqueId(), soundToggle);


		Inventory inventory = Bukkit.createInventory(null, 54, "GameBox gui");
		inventory.setContents(this.inventory.getContents().clone());
		toggle(inventory, player.getUniqueId());
		openInventories.putIfAbsent(player.getUniqueId(),inventory);

		if(super.open(player)){

			player.getOpenInventory().setItem(soundToggleSlot, soundToggle);
			plugin.getNMS().updateInventoryTitle(player, plugin.lang.TITLE_MAIN_GUI.replace("%player%", player.getName()));
			return true;
		}
		return false;
	}

	private void toggle(Inventory inventory, UUID uniqueId) {
		if(!pluginManager.getPlayer(uniqueId).isPlaySounds()) inventory.setItem(soundToggleSlot, this.getSoundToggleButton(uniqueId).toggle());
	}

	public ToggleButton getSoundToggleButton(UUID uuid){
		return soundButtons.get(uuid);
	}
}
