package me.nikl.gamebox.guis.gui;

import me.nikl.gamebox.ClickAction;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.PluginManager;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import me.nikl.gamebox.guis.button.ToggleButton;
import me.nikl.gamebox.guis.shop.Shop;
import me.nikl.gamebox.players.GBPlayer;
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
	private Map<UUID, AButton> tokenButtons = new HashMap<>();


	private int soundToggleSlot = 52;
	private int tokenButtonSlot = 45;
	private int shopSlot = 46;

	public MainGui(GameBox plugin, GUIManager guiManager, String gameID){
		super(plugin, guiManager, 54, new String[]{gameID, GUIManager.MAIN_GAME_GUI});
		this.inventory = Bukkit.createInventory(null, 54, "GameBox gui");
		ItemStack helpItem = new ItemStack(Material.BOOK_AND_QUILL, 1);
		// test glow on buttons
		helpItem = plugin.getNMS().addGlow(helpItem);
		AButton help = new AButton(helpItem);
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


		if(plugin.isTokensEnabled()) {
			// set a placeholder in the general main gui
			ItemStack tokensItem = new AButton(new MaterialData(Material.GOLD_NUGGET), 1);
			tokensItem = plugin.getNMS().addGlow(tokensItem);
			AButton tokens = new AButton(tokensItem);
			meta = tokens.getItemMeta();
			meta.setDisplayName("Placeholder");
			tokens.setItemMeta(meta);
			tokens.setAction(ClickAction.NOTHING);
			setButton(tokens, tokenButtonSlot);
		}




		Map<Integer, ItemStack> hotBarButtons = plugin.getPluginManager().getHotBarButtons();

		// set lower grid
		if(hotBarButtons.containsKey(PluginManager.exit)) {
			AButton exit = new AButton(hotBarButtons.get(PluginManager.exit).getData(), 1);
			meta = hotBarButtons.get(PluginManager.exit).getItemMeta();
			exit.setItemMeta(meta);
			exit.setAction(ClickAction.CLOSE);
			setLowerButton(exit, PluginManager.exit);
		}
	}

	@Deprecated
	public MainGui(GameBox plugin, GUIManager guiManager){
		super(plugin, guiManager, 54);
		this.inventory = Bukkit.createInventory(null, 54, "GameBox gui");
		ItemStack helpItem = new ItemStack(Material.BOOK_AND_QUILL, 1);
		// test glow on buttons
		helpItem = plugin.getNMS().addGlow(helpItem);
		AButton help = new AButton(helpItem);
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


		if(plugin.isTokensEnabled()) {
			// set a placeholder in the general main gui
			ItemStack tokensItem = new AButton(new MaterialData(Material.GOLD_NUGGET), 1);
			tokensItem = plugin.getNMS().addGlow(tokensItem);
			AButton tokens = new AButton(tokensItem);
			meta = tokens.getItemMeta();
			meta.setDisplayName("Placeholder");
			tokens.setItemMeta(meta);
			tokens.setAction(ClickAction.NOTHING);
			setButton(tokens, tokenButtonSlot);
		}




		Map<Integer, ItemStack> hotBarButtons = plugin.getPluginManager().getHotBarButtons();

		// set lower grid
		if(hotBarButtons.containsKey(PluginManager.exit)) {
			AButton exit = new AButton(hotBarButtons.get(PluginManager.exit).getData(), 1);
			meta = hotBarButtons.get(PluginManager.exit).getItemMeta();
			exit.setItemMeta(meta);
			exit.setAction(ClickAction.CLOSE);
			setLowerButton(exit, PluginManager.exit);
		}
	}

	public void registerShop(){
		setButton(guiManager.getShopManager().getMainButton(), shopSlot);
	}


	@Override
	public boolean open(Player player){
		if(!openInventories.containsKey(player.getUniqueId())){
			loadMainGui(pluginManager.getPlayer(player.getUniqueId()));
		}
		if(super.open(player)){
			plugin.getNMS().updateInventoryTitle(player, plugin.lang.TITLE_MAIN_GUI.replace("%player%", player.getName()));
			if(pluginManager.getGames().isEmpty()){
				plugin.getNMS().updateInventoryTitle(player, ChatColor.translateAlternateColorCodes('&', "&c&l %player% you should get some games on Spigot ;)".replace("%player%", player.getName())));
			}
			return true;
		}
		return false;
	}

	public ToggleButton getSoundToggleButton(UUID uuid){
		return soundButtons.get(uuid);
	}

	public void loadMainGui(GBPlayer player){
		ToggleButton soundToggle = new ToggleButton(new MaterialData(Material.RECORD_6), 1, new MaterialData(Material.RECORD_4));
		ItemMeta meta = soundToggle.getItemMeta();
		meta.addItemFlags(ItemFlag.values());
		meta.setDisplayName(plugin.lang.BUTTON_SOUND_ON_NAME);
		meta.setLore(plugin.lang.BUTTON_SOUND_ON_LORE);
		soundToggle.setItemMeta(meta);
		soundToggle.setToggleDisplayName(plugin.lang.BUTTON_SOUND_OFF_NAME);
		soundToggle.setToggleLore(plugin.lang.BUTTON_SOUND_OFF_LORE);
		soundToggle.setAction(ClickAction.TOGGLE);
		soundToggle.setArgs("sound");
		soundButtons.put(player.getUuid(), soundToggle);

		if(plugin.isTokensEnabled()) {
			ItemStack tokensItem = new AButton(new MaterialData(Material.GOLD_NUGGET), 1);
			tokensItem = plugin.getNMS().addGlow(tokensItem);
			AButton tokens = new AButton(tokensItem);
			tokens.setAction(ClickAction.NOTHING);
			tokenButtons.put(player.getUuid(), tokens);
		}

		Inventory inventory = Bukkit.createInventory(null, this.inventory.getSize(), "GameBox gui");
		inventory.setContents(this.inventory.getContents().clone());

		openInventories.putIfAbsent(player.getUuid(),inventory);

		updateButtons(player);
	}

	public void updateButtons(GBPlayer player){
		if(openInventories.get(player.getUuid()) == null) return;
		if(!player.isPlaySounds()) openInventories.get(player.getUuid()).setItem(soundToggleSlot, this.getSoundToggleButton(player.getUuid()).toggle());

		updateTokens(player);
	}

	public void updateTokens(GBPlayer player) {
		if(!plugin.isTokensEnabled()) return;
		if(!tokenButtons.keySet().contains(player.getUuid())) return;

		ItemMeta meta = tokenButtons.get(player.getUuid()).getItemMeta();
		meta.setDisplayName(plugin.lang.BUTTON_TOKENS.replace("%tokens%", String.valueOf(player.getTokens())));
		tokenButtons.get(player.getUuid()).setItemMeta(meta);

		openInventories.get(player.getUuid()).setItem(tokenButtonSlot, tokenButtons.get(player.getUuid()));
	}

	@Override
	public void removePlayer(UUID uuid) {
		soundButtons.remove(uuid);
		super.removePlayer(uuid);
	}
}
