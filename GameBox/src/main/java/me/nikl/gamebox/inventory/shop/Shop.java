package me.nikl.gamebox.inventory.shop;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.PluginManager;
import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.inventory.GUIManager;
import me.nikl.gamebox.inventory.button.AButton;
import me.nikl.gamebox.inventory.gui.AGui;
import me.nikl.gamebox.util.ClickAction;
import me.nikl.gamebox.util.InventoryUtil;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Created by Niklas on 13.04.2017.
 *
 * class to extend upon for shop GUIs
 */
public class Shop extends AGui{
    FileConfiguration shop;

    ShopManager shopManager;

    protected Map<UUID, AButton> tokenButtons = new HashMap<>();

    protected int tokenButtonSlot;

    public Shop(GameBox plugin, GUIManager guiManager, int slots, ShopManager shopManager, String[] args, String title) {
        super(plugin, guiManager, slots, args, title);
        this.shopManager = shopManager;
        this.shop = shopManager.getShop();

        Map<Integer, ItemStack> hotBarButtons = plugin.getPluginManager().getHotBarButtons();


        // set lower grid
        if (hotBarButtons.get(PluginManager.exit) != null) {
            AButton exit = new AButton(hotBarButtons.get(PluginManager.exit).getData(), 1);
            ItemMeta meta = hotBarButtons.get(PluginManager.exit).getItemMeta();
            exit.setItemMeta(meta);
            exit.setAction(ClickAction.CLOSE);
            setLowerButton(exit, PluginManager.exit);
        }


        if (hotBarButtons.get(PluginManager.toMain) != null) {
            AButton main = new AButton(hotBarButtons.get(PluginManager.toMain).getData(), 1);
            ItemMeta meta = hotBarButtons.get(PluginManager.toMain).getItemMeta();
            main.setItemMeta(meta);
            main.setAction(ClickAction.OPEN_MAIN_GUI);
            setLowerButton(main, PluginManager.toMain);
        }


        tokenButtonSlot = slots - 9;

        if(GameBoxSettings.tokensEnabled) {
            // set a placeholder in the general main gui
            ItemStack tokensItem = new AButton(new MaterialData(Material.GOLD_NUGGET), 1);
            tokensItem = plugin.getNMS().addGlow(tokensItem);
            AButton tokens = new AButton(tokensItem);
            ItemMeta meta = tokens.getItemMeta();
            meta.setDisplayName("Placeholder");
            tokens.setItemMeta(meta);
            tokens.setAction(ClickAction.NOTHING);
            setButton(tokens, tokenButtonSlot);
        }
    }



    @Override
    public boolean open(Player player){
        if(!openInventories.containsKey(player.getUniqueId())){
            loadPlayerShop(pluginManager.getPlayer(player.getUniqueId()));
        }
        return super.open(player);
    }

    void loadPlayerShop(GBPlayer player){

        if(GameBoxSettings.tokensEnabled) {
            ItemStack tokensItem = new AButton(new MaterialData(Material.GOLD_NUGGET), 1);
            tokensItem = plugin.getNMS().addGlow(tokensItem);
            AButton tokens = new AButton(tokensItem);
            tokens.setAction(ClickAction.NOTHING);
            tokenButtons.put(player.getUuid(), tokens);
        }

        Inventory inventory = InventoryUtil.createInventory(null, this.inventory.getSize(), "GameBox gui");
        inventory.setContents(this.inventory.getContents().clone());

        openInventories.putIfAbsent(player.getUuid(),inventory);

        updateTokens(player);
    }

    void updateTokens(GBPlayer player) {
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
        tokenButtons.remove(uuid);
        super.removePlayer(uuid);
    }
}
