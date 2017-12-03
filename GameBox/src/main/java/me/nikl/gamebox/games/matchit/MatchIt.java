package me.nikl.gamebox.games.matchit;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.games.Game;
import me.nikl.gamebox.games.GameSettings;
import me.nikl.gamebox.util.ItemStackUtil;
import me.nikl.gamebox.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by nikl on 02.12.17.
 *
 */
public class MatchIt extends Game{
    private List<ItemStack> pairItems;

    public MatchIt(GameBox gameBox) {
        super(gameBox, GameBox.MODULE_MATCHIT
                , new String[]{GameBox.MODULE_MATCHIT, "mi"});
    }

    private void setDefaultItems() {
        // load default language
        try {
            String defaultConfigName = "games/matchit/config.yml";
            FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(gameBox.getResource(defaultConfigName)
                            , "UTF-8"));

            // load default items from file

            ConfigurationSection itemsSec = defaultConfig.getConfigurationSection("items");
            ItemStack itemStack;
            ItemMeta meta;
            for(String key : itemsSec.getKeys(false)){
                if(!itemsSec.isConfigurationSection(key))
                    continue;

                itemStack = loadItem(itemsSec.getConfigurationSection(key));
                if(itemStack != null) pairItems.add(itemStack.clone());
            }
        } catch (UnsupportedEncodingException e2) {
            gameBox.getLogger().warning("Failed to load default config file for: " + module.getModuleID());
            e2.printStackTrace();
        }
    }

    private ItemStack loadItem(ConfigurationSection itemSection){
        ItemStack toReturn;
        ItemMeta meta;

        if(!itemSection.isString("matData")){
            gameBox.warning(" missing 'matData' in " + gameLang.PLAIN_NAME + " config. Key: " + itemSection.getName());
            return null;
        }
        toReturn = ItemStackUtil.getItemStack(itemSection.getString( "matData"));
        if(toReturn == null){
            gameBox.warning(" invalid 'matData' in " + gameLang.PLAIN_NAME + " config. Key: " + itemSection.getName());
            return null;
        }
        meta = toReturn.getItemMeta();
        if(itemSection.isString("displayName")){
            meta.setDisplayName(StringUtil.color(itemSection.getString("displayName")));
        }
        if(itemSection.isList("lore")){
            meta.setLore(StringUtil.color(itemSection.getStringList("lore")));
        }
        toReturn.setItemMeta(meta);

        return toReturn;
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void init() {
        pairItems = new ArrayList<>();

        if(!config.isConfigurationSection("items")){
            gameBox.warning(" there are no items defined for this game...");
            gameBox.warning(" falling back to default...");
            setDefaultItems();
            // ToDo or deregister the game!
        } else {
            ConfigurationSection itemsSec = config.getConfigurationSection("items");
            ItemStack itemStack;
            for(String key : itemsSec.getKeys(false)){
                if(!itemsSec.isConfigurationSection(key))
                    continue;

                itemStack = loadItem(itemsSec.getConfigurationSection(key));

                if(itemStack != null) pairItems.add(itemStack.clone());
            }
            if(getPairItems().isEmpty()){
                gameBox.warning(" there are no items defined for the game " + gameLang.PLAIN_NAME);
                gameBox.warning(" falling back to default...");
                setDefaultItems();
                // ToDo or deregister the game!
            }
        }
    }

    @Override
    public void loadSettings() {
        gameSettings.setGameType(GameSettings.GameType.SINGLE_PLAYER);
        gameSettings.setGameGuiSize(54);
        gameSettings.setHandleClicksOnHotbar(false);
    }

    @Override
    public void loadLanguage() {
        this.gameLang = new MILanguage(gameBox);
    }

    @Override
    public void loadGameManager() {
        this.gameManager = new MIGameManager(this);
    }

    public List<ItemStack> getPairItems() {
        return pairItems;
    }

    public enum GridSize{
        FULL(54), MIDDLE(4*7), SMALL(2*5);

        private int size;
        GridSize(int size){
            this.size = size;
        }

        int getSize(){
            return size;
        }
    }
}
