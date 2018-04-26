package me.nikl.gamebox.inventory.shop;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxLanguage;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.events.EnterGameBoxEvent;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.inventory.GUIManager;
import me.nikl.gamebox.inventory.button.Button;
import me.nikl.gamebox.inventory.gui.AGui;
import me.nikl.nmsutilities.NmsFactory;
import me.nikl.gamebox.utility.ItemStackUtility;
import me.nikl.gamebox.utility.Permission;
import me.nikl.gamebox.utility.StringUtility;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author Niklas Eicker
 */
public class ShopManager {
    public static final String MAIN = "MainShop_" + UUID.randomUUID().toString();
    protected Map<String, Category> categories;
    protected MainShop mainShop;
    protected GUIManager guiManager;
    FileConfiguration shop;
    private File shopFile;
    private Button mainButton;
    private boolean closed;
    private GameBox gameBox;
    private GameBoxLanguage lang;
    private int mainSlots = 27, titleMessageSeconds = 3;

    public ShopManager(GameBox gameBox, GUIManager guiManager) {
        this.gameBox = gameBox;
        this.lang = gameBox.lang;
        this.guiManager = guiManager;
        categories = new HashMap<>();
        loadFile();
        if (!shop.isConfigurationSection("shop")
                || !shop.isConfigurationSection("shop.button")
                || !shop.isConfigurationSection("shop.categories")) {
            Bukkit.getLogger().log(Level.WARNING, "The shop is not correctly set up!");
            Bukkit.getLogger().log(Level.WARNING, "Disabling tokens!");
            GameBoxSettings.tokensEnabled = false;
            return;
        }
        this.closed = !shop.getBoolean("open");
        loadShopButton();
        mainShop = new MainShop(gameBox, guiManager, mainSlots, this, new String[]{MAIN, "0"});
    }

    private void loadShopButton() {
        ItemStack mainItem = ItemStackUtility.getItemStack(shop.getString("shop.button.materialData", Material.STORAGE_MINECART.toString()));
        if (shop.getBoolean("shop.button.glow")) mainItem = NmsFactory.getNmsUtility().addGlow(mainItem);
        mainButton = new Button(mainItem);
        ItemMeta meta = mainItem.getItemMeta();
        if (shop.isString("shop.button.displayName")) {
            meta.setDisplayName(StringUtility.color(shop.getString("shop.button.displayName")));
        }
        if (shop.isList("shop.button.lore")) {
            meta.setLore(StringUtility.color(shop.getStringList("shop.button.lore")));
        }
        mainButton.setItemMeta(meta);
        mainButton.setAction(ClickAction.OPEN_SHOP_PAGE);
        mainButton.setArgs(MAIN, "0");
    }

    private void loadFile() {
        shopFile = new File(gameBox.getDataFolder().toString() + File.separatorChar + "tokenShop.yml");
        if (!shopFile.exists()) {
            shopFile.getParentFile().mkdirs();
            gameBox.saveResource("tokenShop.yml", false);
        }
        try {
            this.shop = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(shopFile), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e2) {
            e2.printStackTrace();
        }
    }

    public Button getMainButton() {
        return mainButton;
    }

    public boolean openShopPage(Player whoClicked, String[] args) {
        boolean saved = false;

        if (!gameBox.getPluginManager().hasSavedContents(whoClicked.getUniqueId())) {
            EnterGameBoxEvent enterEvent = new EnterGameBoxEvent(whoClicked, args[0], args[1]);
            if (!enterEvent.isCancelled()) {
                gameBox.getPluginManager().saveInventory(whoClicked);
                saved = true;
            } else {
                whoClicked.sendMessage(lang.PREFIX + " A game was canceled with the reason: " + enterEvent.getCancelMessage());
                return false;
            }
        }

        if (Permission.OPEN_SHOP.hasPermission(whoClicked)) {
            if (args[0].equals(ShopManager.MAIN) && args[1].equals("0")) {
                GameBox.openingNewGUI = true;
                mainShop.open(whoClicked);
                GameBox.openingNewGUI = false;

                if (closed) {
                    NmsFactory.getNmsUtility().updateInventoryTitle(whoClicked, gameBox.lang.SHOP_IS_CLOSED);
                } else {
                    NmsFactory.getNmsUtility().updateInventoryTitle(whoClicked, gameBox.lang.SHOP_TITLE_MAIN_SHOP.replace("%player%", whoClicked.getDisplayName()));
                }
                return true;
            } else if (categories.containsKey(args[0])) {
                int page;
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException exception) {
                    Bukkit.getLogger().log(Level.SEVERE, "failed to open shop page due to corrupted args!");
                    return false;
                }
                GameBox.openingNewGUI = true;
                boolean open = categories.get(args[0]).openPage(whoClicked, page);
                GameBox.openingNewGUI = false;
                if (open) {
                    NmsFactory.getNmsUtility().updateInventoryTitle(whoClicked, gameBox.lang.SHOP_TITLE_PAGE_SHOP.replace("%page%", String.valueOf(page + 1)));
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            if (saved) gameBox.getPluginManager().leaveGameBox(whoClicked);
            whoClicked.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);

            if (guiManager.isInMainGUI(whoClicked.getUniqueId())) {
                String currentTitle = gameBox.lang.TITLE_MAIN_GUI.replace("%player%", whoClicked.getName());
                gameBox.getInventoryTitleMessenger().sendInventoryTitle(whoClicked, gameBox.lang.TITLE_NO_PERM, currentTitle, titleMessageSeconds);
            }

            return false;
        }
        if (saved) gameBox.getPluginManager().leaveGameBox(whoClicked);
        Bukkit.getLogger().log(Level.SEVERE, "trying to open a shop page failed");
        Bukkit.getLogger().log(Level.SEVERE, "args: " + Arrays.asList(args));
        whoClicked.sendMessage("Error");
        return false;
    }

    public FileConfiguration getShop() {
        return shop;
    }

    public void loadCategory(String cat) {
        categories.put(cat, new Category(gameBox, this, guiManager, cat));
    }

    public boolean inShop(UUID uuid) {
        if (mainShop.isInGui(uuid)) return true;
        for (Category category : categories.values()) {
            if (category.inCategory(uuid)) return true;
        }
        return false;
    }

    public ItemStack getShopItemStack(String category, String counter) {
        if (categories.get(category) != null) {
            return categories.get(category).getShopItemStack(counter);
        }
        return null;
    }

    public ShopItem getShopItem(String category, String counter) {
        if (categories.get(category) != null) {
            return categories.get(category).getShopItem(counter);
        }
        return null;
    }

    public boolean isClosed() {
        return closed;
    }

    public AGui getShopGui(UUID uuid) {
        if (mainShop.isInGui(uuid)) {
            return mainShop;
        }
        for (Category category : categories.values()) {
            if (category.inCategory(uuid)) {
                return category.getShopGui(uuid);
            }
        }
        return null;
    }
}
