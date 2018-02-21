package me.nikl.gamebox.inventory.menu.main;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.PluginManager;
import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.inventory.GUIManager;
import me.nikl.gamebox.inventory.button.AButton;
import me.nikl.gamebox.inventory.button.Button;
import me.nikl.gamebox.inventory.button.ButtonFactory;
import me.nikl.gamebox.inventory.menu.PerPlayerMenu;
import me.nikl.gamebox.utility.ItemStackUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

/**
 * Created by nikl on 21.02.18.
 */
public class MainMenu extends PerPlayerMenu {
    private GUIManager guiManager;
    private PluginManager pluginManager;

    private int soundToggleSlot = 52;
    private int tokenButtonSlot = 45;
    private int shopSlot = 46;
    
    public MainMenu(GameBox gameBox) {
        super(gameBox);
        this.pluginManager = gameBox.getPluginManager();
        this.guiManager = pluginManager.getGuiManager();

        Button help = new Button(gameBox.getNMS().addGlow(ItemStackUtility.createBookWithText(gameBox.lang.BUTTON_MAIN_MENU_INFO)));
        help.setAction(ClickAction.NOTHING);
        setButton(help, 53);

        setButton(ButtonFactory.createToggleButton(gameBox.lang), soundToggleSlot);

        Map<Integer, ItemStack> hotBarButtons = pluginManager.getHotBarButtons();

        // set lower grid
        if (hotBarButtons.containsKey(pluginManager.exitButtonSlot)) {
            Button exit = new Button(hotBarButtons.get(pluginManager.exitButtonSlot));
            ItemMeta meta = hotBarButtons.get(pluginManager.exitButtonSlot).getItemMeta();
            exit.setItemMeta(meta);
            exit.setAction(ClickAction.CLOSE);
            setLowerButton(exit, pluginManager.exitButtonSlot);
        }
    }

    @Override
    public void updatePlayer(GBPlayer gbPlayer) {

    }

    @Override
    protected void onUpperClick(InventoryClickEvent event) {

    }

    @Override
    protected void onLowerClick(InventoryClickEvent event) {

    }

    @Override
    public boolean open(Player player) {
        if (!playerInventories.containsKey(player.getUniqueId())) {
            preparePlayerInventory(player);
        }
        players.add(player.getUniqueId());
        try {
            player.openInventory(playerInventories.get(player.getUniqueId()));
        } catch (Exception exception) {
            sentInventoryTitleMessage(player, "Error");
            players.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    private void preparePlayerInventory(Player player) {
        AButton[] buttons = upperGrid.clone();
        GBPlayer gbPlayer = pluginManager.getPlayer(player.getUniqueId());
        if(gbPlayer == null){
            playerInventories.put(player.getUniqueId(), getNewInventory(player));
            return;
        }
        buttons[tokenButtonSlot] = ButtonFactory.createTokenButton(gameBox.lang, gbPlayer.getTokens());
        buttons[soundToggleSlot] = gbPlayer.isPlaySounds()?ButtonFactory.createToggleButton(gameBox.lang):ButtonFactory.createToggleButton(gameBox.lang).toggle();
        playerButtons.put(player.getUniqueId(), buttons);
        Inventory playerInventory = getNewInventory(player);
        playerInventory.setContents(buttons.clone());
    }

    // ToDo: improve... this is copy-paste from old class
    public void registerShop() {
        setButton(guiManager.getShopManager().getMainButton(), shopSlot);
    }


    @Override
    public String getPlayerSpezificTitle(Player player) {
        return defaultTitle.replace("%player%", player.getName());
    }
}
