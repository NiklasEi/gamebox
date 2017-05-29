package me.nikl.gamebox.guis.gui.game;

import me.nikl.gamebox.ClickAction;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.PluginManager;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import me.nikl.gamebox.guis.gui.AGui;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Niklas on 13.02.2017.
 *
 */
public class GameGui extends AGui {

    /**
     * Constructor for a gamegui
     *
     * Don't forget to register it with the GUIManager
     * @param plugin     plugin instance
     * @param guiManager GUIManager instance
     * @param slots      number of slots in the inventory
     */
    public GameGui(GameBox plugin, GUIManager guiManager, int slots, String gameID, String key) {
        super(plugin, guiManager, slots, new String[]{gameID, key});


        Map<Integer, ItemStack> hotBarButtons = plugin.getPluginManager().getHotBarButtons();


        // set lower grid
        if(hotBarButtons.containsKey(PluginManager.exit)) {
            AButton exit = new AButton(hotBarButtons.get(PluginManager.exit).getData(), 1);
            ItemMeta meta = hotBarButtons.get(PluginManager.exit).getItemMeta();
            exit.setItemMeta(meta);
            exit.setAction(ClickAction.CLOSE);
            setLowerButton(exit, PluginManager.exit);
        }


        if(hotBarButtons.containsKey(PluginManager.toMain)) {
            AButton main = new AButton(hotBarButtons.get(PluginManager.toMain).getData(), 1);
            ItemMeta meta = hotBarButtons.get(PluginManager.toMain).getItemMeta();
            main.setItemMeta(meta);
            main.setAction(ClickAction.OPEN_MAIN_GUI);
            setLowerButton(main, PluginManager.toMain);
        }
    }

    @Deprecated
    public String getGameID(){
        return this.args[0];
    }

    @Deprecated
    public String getKey(){
        return this.args[1];
    }

    public void setHelpButton(List<String> list){

        ItemStack helpItem = new ItemStack(Material.BOOK_AND_QUILL, 1);
        // test glow on buttons
        helpItem = plugin.getNMS().addGlow(helpItem);
        AButton help = new AButton(helpItem);
        ItemMeta meta = help.getItemMeta();
        if(list != null) {
            if(list.size() > 0)meta.setDisplayName(list.get(0));
            if(list.size() > 1){
                ArrayList<String> lore = new ArrayList<>(list);
                lore.remove(0);
                meta.setLore(lore);
            }
        }
        help.setItemMeta(meta);
        help.setAction(ClickAction.NOTHING);
        setButton(help, inventory.getSize()-1);
    }
}
