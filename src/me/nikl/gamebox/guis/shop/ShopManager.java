package me.nikl.gamebox.guis.shop;

import me.nikl.gamebox.ClickAction;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.Language;
import me.nikl.gamebox.Permissions;
import me.nikl.gamebox.events.EnterGameBoxEvent;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import me.nikl.gamebox.guis.gui.AGui;
import me.nikl.gamebox.players.GBPlayer;
import me.nikl.gamebox.util.ItemStackUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created by Niklas on 13.04.2017.
 *
 *
 */
public class ShopManager {
    public static final String MAIN = "main" + UUID.randomUUID().toString();

    private File shopFile;
    FileConfiguration shop;

    private AButton mainButton;

    protected Map<String, Category> categories;

    protected MainShop mainShop;

    protected GUIManager guiManager;

    private boolean closed;

    private GameBox plugin;
    private Language lang;

    private int mainSlots = 27, titleMessageSeconds = 3;

    public ShopManager(GameBox plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.lang = plugin.lang;
        this.guiManager = guiManager;

        categories = new HashMap<>();

        loadFile();
        if (!shop.isConfigurationSection("shop") || !shop.isConfigurationSection("shop.button") || !shop.isConfigurationSection("shop.categories")) {
            Bukkit.getLogger().log(Level.WARNING, "The shop is not correctly set up!");
            Bukkit.getLogger().log(Level.WARNING, "Disabling tokens!");
            GameBoxSettings.tokensEnabled = false;
            return;
        }

        this.closed = !shop.getBoolean("open");

        List<String> lore;
        ItemStack mainItem = ItemStackUtil.getItemStack(shop.getString("shop.button.materialData", Material.STORAGE_MINECART.toString()));
        if (shop.getBoolean("shop.button.glow")) mainItem = plugin.getNMS().addGlow(mainItem);
        mainButton = new AButton(mainItem);
        ItemMeta meta = mainItem.getItemMeta();
        if (shop.isString("shop.button.displayName")) {
            meta.setDisplayName(plugin.chatColor(shop.getString("shop.button.displayName")));
        }


        if (shop.isList("shop.button.lore")) {
            lore = new ArrayList<>(shop.getStringList("shop.button.lore"));
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, plugin.chatColor(lore.get(i)));
            }
            meta.setLore(lore);
        }

        mainButton.setItemMeta(meta);
        mainButton.setAction(ClickAction.OPEN_SHOP_PAGE);
        mainButton.setArgs(MAIN, "0");

        mainShop = new MainShop(plugin, guiManager, mainSlots, this, new String[]{MAIN, "0"});
    }


    private void loadFile() {
        shopFile = new File(plugin.getDataFolder().toString() + File.separatorChar + "tokenShop.yml");
        if(!shopFile.exists()){
            shopFile.getParentFile().mkdirs();
            plugin.saveResource("tokenShop.yml", false);
        }
        try {
            this.shop =  YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(shopFile), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e2) {
            e2.printStackTrace();
        }
    }

    public AButton getMainButton() {
        return mainButton;
    }

    public boolean openShopPage(Player whoClicked, String[] args) {
        boolean saved = false;

        if(!plugin.getPluginManager().hasSavedContents(whoClicked.getUniqueId())){
            EnterGameBoxEvent enterEvent = new EnterGameBoxEvent(whoClicked, args[0], args[1]);
            if(!enterEvent.isCancelled()){
                plugin.getPluginManager().saveInventory(whoClicked);
                saved = true;
            } else {
                whoClicked.sendMessage(lang.PREFIX + " A game was canceled with the reason: " + enterEvent.getCancelMessage());
                return false;
            }
        }

        if(whoClicked.hasPermission(Permissions.OPEN_SHOP.getPermission())){
            if(args[0].equals(ShopManager.MAIN) && args[1].equals("0")) {
                GameBox.openingNewGUI = true;
                mainShop.open(whoClicked);
                GameBox.openingNewGUI = false;

                if(closed){
                    plugin.getNMS().updateInventoryTitle(whoClicked, plugin.lang.SHOP_IS_CLOSED);
                } else {
                    plugin.getNMS().updateInventoryTitle(whoClicked, plugin.lang.SHOP_TITLE_MAIN_SHOP.replace("%player%", whoClicked.getDisplayName()));
                }
                return true;
            } else if(categories.containsKey(args[0])) {
                int page;
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException exception){
                    Bukkit.getLogger().log(Level.SEVERE, "failed to open shop page due to corrupted args!");
                    return false;
                }
                GameBox.openingNewGUI = true;
                boolean open = categories.get(args[0]).openPage(whoClicked, page);
                GameBox.openingNewGUI = false;
                if(open) {
                    plugin.getNMS().updateInventoryTitle(whoClicked, plugin.lang.SHOP_TITLE_PAGE_SHOP.replace("%page%",String.valueOf(page+1)));
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            if (saved) plugin.getPluginManager().restoreInventory(whoClicked);
            whoClicked.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);

            if(guiManager.isInMainGUI(whoClicked.getUniqueId())) {
                String currentTitle = plugin.lang.TITLE_MAIN_GUI.replace("%player%", whoClicked.getName());
                plugin.getPluginManager().startTitleTimer(whoClicked, currentTitle, titleMessageSeconds);
                plugin.getNMS().updateInventoryTitle(whoClicked, plugin.lang.TITLE_NO_PERM);
            }

            return false;
        }
        if (saved) plugin.getPluginManager().restoreInventory(whoClicked);
        Bukkit.getLogger().log(Level.SEVERE, "trying to open a shop page failed");
        Bukkit.getLogger().log(Level.SEVERE, "args: " + Arrays.asList(args));
        whoClicked.sendMessage("Error");
        return false;
    }

    public FileConfiguration getShop() {
        return shop;
    }

    public void loadCategory(String cat) {
        categories.put(cat, new Category(plugin, this, guiManager, cat));
    }

    public boolean inShop(UUID uuid) {
        if(mainShop.isInGui(uuid)) return true;
        for(Category category : categories.values()){
            if(category.inCategory(uuid)) return true;
        }
        return false;
    }

    public void onClick(InventoryClickEvent event) {
        boolean topInv = event.getSlot() == event.getRawSlot();
        if(mainShop.isInGui(event.getWhoClicked().getUniqueId())){
            if(topInv){
                mainShop.onInvClick(event);
            } else {
                mainShop.onBottomInvClick(event);
            }
            return;
        }
        for(Category category : categories.values()){
            if(category.inCategory(event.getWhoClicked().getUniqueId())){
                if(topInv){
                    category.onInvClick(event);
                } else {
                    category.onBottomInvClick(event);
                }
            }
        }
    }

    public void onInvClose(InventoryCloseEvent event) {
        if(mainShop.isInGui(event.getPlayer().getUniqueId())){
            mainShop.onInvClose(event);
            return;
        }
        for(Category category : categories.values()){
            if(category.inCategory(event.getPlayer().getUniqueId())){
                category.onInvClose(event);
            }
        }
    }

    public void updateTokens(GBPlayer gbPlayer) {
        mainShop.updateTokens(gbPlayer);
        for (Category category : categories.values()){
            category.updateTokens(gbPlayer);
        }
    }

    public ItemStack getShopItemStack(String category, String counter){
        if(categories.get(category) != null){
            return categories.get(category).getShopItemStack(counter);
        }
        return null;
    }

    public ShopItem getShopItem(String category, String counter){
        if(categories.get(category) != null){
            return categories.get(category).getShopItem(counter);
        }
        return null;
    }

    public boolean isClosed() {
        return closed;
    }

    public AGui getShopGui(UUID uuid) {
        if(mainShop.isInGui(uuid)){
            return mainShop;
        }
        for(Category category : categories.values()){
            if(category.inCategory(uuid)){
                return category.getShopGui(uuid);
            }
        }
        return null;
    }
}
