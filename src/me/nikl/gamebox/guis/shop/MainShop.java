package me.nikl.gamebox.guis.shop;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by Niklas on 13.04.2017.
 */
public class MainShop extends Shop {


    public MainShop(GameBox plugin, GUIManager guiManager, int slots, ShopManager shopManager) {
        super(plugin, guiManager, slots, shopManager);

        loadCategories();
    }



    private void loadCategories() {
        List<String> lore;
        for(String cat : shop.getConfigurationSection("shop.categories").getKeys(false)){
            ConfigurationSection category = shop.getConfigurationSection("shop.categories." + cat);
            ItemStack mat = getItemStack(category.getString("materialData"));

            if(mat == null){
                Bukkit.getLogger().log(Level.WARNING, " error loading:   shop.categories." + cat);
                Bukkit.getLogger().log(Level.WARNING, "     invalid material data");
                continue;
            }


            AButton button =  new AButton(mat.getData(), 1);
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

            shopManager.loadCategory(cat);
        }
    }
}
