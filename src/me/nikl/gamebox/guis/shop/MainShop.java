package me.nikl.gamebox.guis.shop;

import me.nikl.gamebox.ClickAction;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import me.nikl.gamebox.players.GBPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.*;
import java.util.logging.Level;

/**
 * Created by Niklas on 13.04.2017.
 */
public class MainShop extends Shop {

    private Map<UUID, AButton> tokenButtons = new HashMap<>();

    private int tokenButtonSlot;

    public MainShop(GameBox plugin, GUIManager guiManager, int slots, ShopManager shopManager) {
        super(plugin, guiManager, slots, shopManager);

        tokenButtonSlot = slots - 9;

        if(plugin.isTokensEnabled()) {
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

        loadCategories();
    }



    private void loadCategories() {
        List<String> lore;
        ItemStack buttonItem;
        for(String cat : shop.getConfigurationSection("shop.categories").getKeys(false)){
            ConfigurationSection category = shop.getConfigurationSection("shop.categories." + cat);
            buttonItem = getItemStack(category.getString("materialData"));

            if(buttonItem == null){
                Bukkit.getLogger().log(Level.WARNING, " error loading:   shop.categories." + cat);
                Bukkit.getLogger().log(Level.WARNING, "     invalid material data");
                continue;
            }
            if(category.getBoolean("glow")){
                buttonItem = plugin.getNMS().addGlow(buttonItem);
            }
            AButton button =  new AButton(buttonItem);
            ItemMeta meta = button.getItemMeta();

            if(category.isString("displayName")){
                meta.setDisplayName(plugin.chatColor(category.getString("displayName")));
            }

            if(category.isList("lore")){
                lore = new ArrayList<>(category.getStringList("lore"));
                for(int i = 0; i < lore.size();i++){
                    lore.set(i, plugin.chatColor(lore.get(i)));
                }
                meta.setLore(lore);
            }

            button.setItemMeta(meta);
            button.setAction(ClickAction.OPEN_SHOP_PAGE);
            button.setArgs(cat, "0");

            setButton(button);

            shopManager.loadCategory(cat);
        }
    }

    @Override
    public boolean open(Player player){
        if(!openInventories.containsKey(player.getUniqueId())){
            loadMainGui(pluginManager.getPlayer(player.getUniqueId()));
        }
        if(super.open(player)){
            plugin.getNMS().updateInventoryTitle(player, plugin.lang.SHOP_TITLE_MAIN_SHOP.replace("%player%", player.getName()));
            return true;
        }
        return false;
    }

    public void loadMainGui(GBPlayer player){

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
}
