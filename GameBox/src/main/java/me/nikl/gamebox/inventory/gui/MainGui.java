package me.nikl.gamebox.inventory.gui;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.inventory.GUIManager;
import me.nikl.gamebox.inventory.button.Button;
import me.nikl.gamebox.inventory.button.ButtonFactory;
import me.nikl.gamebox.inventory.button.DisplayButton;
import me.nikl.gamebox.inventory.button.ToggleButton;
import me.nikl.gamebox.nms.NmsFactory;
import me.nikl.gamebox.utility.InventoryUtility;
import me.nikl.gamebox.utility.ItemStackUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by niklas on 2/5/17.
 */
public class MainGui extends AGui {
    private Map<UUID, ToggleButton> soundButtons = new HashMap<>();
    private Map<UUID, DisplayButton> tokenButtons = new HashMap<>();


    private int soundToggleSlot = 52;
    private int tokenButtonSlot = 45;
    private int shopSlot = 46;

    public MainGui(GameBox plugin, GUIManager guiManager) {
        super(plugin, guiManager, 54, new String[]{}, plugin.lang.TITLE_MAIN_GUI);

        Button help = new Button(NmsFactory.getNmsUtility().addGlow(ItemStackUtility.createBookWithText(plugin.lang.BUTTON_MAIN_MENU_INFO)));
        help.setAction(ClickAction.NOTHING);
        setButton(help, 53);


        ToggleButton soundToggle = ButtonFactory.createToggleButton(plugin.lang);
        setButton(soundToggle, soundToggleSlot);


        if (GameBoxSettings.tokensEnabled) {
            DisplayButton tokens = ButtonFactory.createTokenButton(plugin.lang, 0);
            setButton(tokens, tokenButtonSlot);
        }


        Map<Integer, ItemStack> hotBarButtons = plugin.getPluginManager().getHotBarButtons();

        // set lower grid
        if (hotBarButtons.containsKey(GameBoxSettings.exitButtonSlot)) {
            Button exit = new Button(hotBarButtons.get(GameBoxSettings.exitButtonSlot));
            ItemMeta meta = hotBarButtons.get(GameBoxSettings.exitButtonSlot).getItemMeta();
            exit.setItemMeta(meta);
            exit.setAction(ClickAction.CLOSE);
            setLowerButton(exit, GameBoxSettings.exitButtonSlot);
        }
    }

    public void registerShop() {
        setButton(guiManager.getShopManager().getMainButton(), shopSlot);
    }


    @Override
    public boolean open(Player player) {
        if (!openInventories.containsKey(player.getUniqueId())) {
            loadMainGui(pluginManager.getPlayer(player.getUniqueId()));
        }
        if (super.open(player)) {
            if (pluginManager.getGames().isEmpty()) {
                NmsFactory.getNmsUtility().updateInventoryTitle(player, ChatColor.translateAlternateColorCodes('&', "&c&l %player% you should get some games on Spigot ;)".replace("%player%", player.getName())));
            }
            return true;
        }
        return false;
    }

    public ToggleButton getSoundToggleButton(UUID uuid) {
        return soundButtons.get(uuid);
    }

    public void loadMainGui(GBPlayer player) {
        ToggleButton soundToggle = ButtonFactory.createToggleButton(gameBox.lang);
        soundButtons.put(player.getUuid(), player.isPlaySounds() ? soundToggle : soundToggle.toggle());

        if (GameBoxSettings.tokensEnabled) {
            DisplayButton tokens = ButtonFactory.createTokenButton(gameBox.lang, player.getTokens());
            tokenButtons.put(player.getUuid(), tokens);
        }
        String title = this.title.replace("%player%", Bukkit.getPlayer(player.getUuid()).getName());
        Inventory inventory = InventoryUtility.createInventory(this, this.inventory.getSize(), title);
        inventory.setContents(this.inventory.getContents().clone());
        openInventories.putIfAbsent(player.getUuid(), inventory);
    }

    public void updateButtons(GBPlayer player) {
        if (openInventories.get(player.getUuid()) == null) return;
        if (!player.isPlaySounds())
            openInventories.get(player.getUuid()).setItem(soundToggleSlot, this.getSoundToggleButton(player.getUuid()).toggle());

        updateTokens(player);
    }

    public void updateTokens(GBPlayer player) {
        if (!GameBoxSettings.tokensEnabled) return;
        openInventories.get(player.getUuid()).setItem(tokenButtonSlot, tokenButtons.get(player.getUuid()).update("%tokens%", player.getTokens()));
    }

    @Override
    public void removePlayer(UUID uuid) {
        soundButtons.remove(uuid);
        tokenButtons.remove(uuid);
        super.removePlayer(uuid);
    }
}
