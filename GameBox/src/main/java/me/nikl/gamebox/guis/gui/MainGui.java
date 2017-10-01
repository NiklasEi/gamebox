package me.nikl.gamebox.guis.gui;

import me.nikl.gamebox.ClickAction;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.PluginManager;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import me.nikl.gamebox.guis.button.ToggleButton;
import me.nikl.gamebox.players.GBPlayer;
import me.nikl.gamebox.util.ItemStackUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

	public MainGui(GameBox plugin, GUIManager guiManager){
		super(plugin, guiManager, 54, new String[]{}, plugin.lang.TITLE_MAIN_GUI);



		AButton help = new AButton(plugin.getNMS().addGlow(ItemStackUtil.createBookWithText(plugin.lang.BUTTON_MAIN_MENU_INFO)));
		help.setAction(ClickAction.NOTHING);
		setButton(help, 53);


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
		setButton(soundToggle, soundToggleSlot);


		if(GameBoxSettings.tokensEnabled) {
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

		if(GameBoxSettings.tokensEnabled) {
			ItemStack tokensItem = new AButton(new MaterialData(Material.GOLD_NUGGET), 1);
			tokensItem = plugin.getNMS().addGlow(tokensItem);
			AButton tokens = new AButton(tokensItem);
			tokens.setAction(ClickAction.NOTHING);
			tokenButtons.put(player.getUuid(), tokens);
		}

		String title = this.title.replace("%player%", Bukkit.getPlayer(player.getUuid()).getName());

		if(GameBoxSettings.checkInventoryLength && title.length() > 32){
			title = "Title is too long!";
		}

		Inventory inventory = Bukkit.createInventory(null, this.inventory.getSize(), title);

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
		if(!GameBoxSettings.tokensEnabled) return;
		if(!tokenButtons.keySet().contains(player.getUuid())) return;
		if(!openInventories.keySet().contains(player.getUuid())) return;

		ItemMeta meta = tokenButtons.get(player.getUuid()).getItemMeta();
		meta.setDisplayName(plugin.lang.BUTTON_TOKENS.replace("%tokens%", String.valueOf(player.getTokens())));
		tokenButtons.get(player.getUuid()).setItemMeta(meta);

		openInventories.get(player.getUuid()).setItem(tokenButtonSlot, tokenButtons.get(player.getUuid()));
	}

	@Override
	public void removePlayer(UUID uuid) {
		soundButtons.remove(uuid);
		tokenButtons.remove(uuid);
		super.removePlayer(uuid);
	}
}
