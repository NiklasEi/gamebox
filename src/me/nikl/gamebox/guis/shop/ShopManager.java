package me.nikl.gamebox.guis.shop;

import me.nikl.gamebox.ClickAction;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Language;
import me.nikl.gamebox.Permissions;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.util.*;
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

    private GameBox plugin;
    private Language lang;

    private int mainSlots = 27, pageSlots = 54, titleMessageSeconds = 3;

    public ShopManager(GameBox plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.lang = plugin.lang;
        this.guiManager = guiManager;

        categories = new HashMap<>();

        loadFile();
        if (!shop.isConfigurationSection("shop") || !shop.isConfigurationSection("shop.button") || !shop.isConfigurationSection("shop.categories")) {
            Bukkit.getLogger().log(Level.WARNING, "The shop is not correctly set up!");
            Bukkit.getLogger().log(Level.WARNING, "Disabling tokens!");
            plugin.setTokensEnabled(false);
            return;
        }


        List<String> lore;
        ItemStack mainItem = getItemStack(shop.getString("shop.button.materialData", Material.STORAGE_MINECART.toString()));
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

        mainShop = new MainShop(plugin, guiManager, mainSlots, this);
    }


    private void loadFile() {
        shopFile = new File(plugin.getDataFolder().toString() + File.separatorChar + "shop.yml");
        if(!shopFile.exists()){
            shopFile.getParentFile().mkdirs();
            plugin.saveResource("shop.yml", false);
        }
        try {
            this.shop =  YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(shopFile), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e2) {
            e2.printStackTrace();
        }
    }



    protected ItemStack getItemStack(String matDataString){
        Material mat; short data;
        String[] obj = matDataString.split(":");

        if (obj.length == 2) {
            try {
                mat = Material.matchMaterial(obj[0]);
            } catch (Exception e) {
                return null; // material name doesn't exist
            }

            try {
                data = Short.valueOf(obj[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return null; // data not a number
            }

            //noinspection deprecation
            if(mat == null) return null;
            ItemStack stack = new ItemStack(mat, 1);
            stack.setDurability(data);
            return stack;
        } else {
            try {
                mat = Material.matchMaterial(obj[0]);
            } catch (Exception e) {
                return null; // material name doesn't exist
            }
            //noinspection deprecation
            return (mat == null ? null : new ItemStack(mat, 1));
        }
    }

    public AButton getMainButton() {
        return mainButton;
    }

    public boolean openShopPage(Player whoClicked, String[] args) {
        boolean saved = false;
        if(!plugin.getPluginManager().hasSavedContents(whoClicked.getUniqueId())){
            plugin.getPluginManager().saveInventory(whoClicked);
            saved = true;
        }

        if(whoClicked.hasPermission(Permissions.OPEN_SHOP.getPermission())){
            if(args[0].equals(ShopManager.MAIN) && args[1].equals("0")) {
                GameBox.openingNewGUI = true;
                mainShop.open(whoClicked);
                GameBox.openingNewGUI = false;

                plugin.getNMS().updateInventoryTitle(whoClicked, "Our shop is closed atm, sorry");
                return true;
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
        // ToDo;
        categories.put(cat, new Category(this, guiManager, cat));
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
}
