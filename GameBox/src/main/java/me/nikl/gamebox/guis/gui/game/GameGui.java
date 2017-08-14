package me.nikl.gamebox.guis.gui.game;

import me.nikl.gamebox.ClickAction;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.PluginManager;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import me.nikl.gamebox.guis.gui.AGui;
import me.nikl.gamebox.util.ItemStackUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
     * @param gameID ID of the game
     * @param key GUI key
     * @param title proposed title of the GUI
     */
    public GameGui(GameBox plugin, GUIManager guiManager, int slots, String gameID, String key, String title) {
        super(plugin, guiManager, slots, new String[]{gameID, key}, title);


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

    /**
     * Only to be used for the main GUI of a game
     * 
     * The title is automatically set to the game-title
     * set in the GameBox language file
     * @param plugin GameBox instance
     * @param guiManager plugin manager 
     * @param slots slots of the GUI
     * @param gameID ID of the game
     * @param key GUI key
     */
    public GameGui(GameBox plugin, GUIManager guiManager, int slots, String gameID, String key){
        this(plugin, guiManager, slots, gameID, key, plugin.lang.TITLE_GAME_GUI);
    }

    /**
     * Only to be used for the main GUI of a game
     *
     * The title is automatically set to the game-title
     * set in the GameBox language file
     * @param plugin GameBox instance
     * @param guiManager plugin manager
     * @param slots slots of the GUI
     * @param gameID ID of the game
     */
    public GameGui(GameBox plugin, GUIManager guiManager, int slots, String gameID){
        this(plugin, guiManager, slots, gameID, GUIManager.MAIN_GAME_GUI, plugin.lang.TITLE_GAME_GUI);
    }

    /**
     * Place a help button with the given text in the
     * lower right corner of the GUI
     *
     * @param list text that will be displayed on the button
     */
    public void setHelpButton(List<String> list){
        AButton help = new AButton(plugin.getNMS().addGlow(ItemStackUtil.createBookWithText(list)));
        help.setAction(ClickAction.NOTHING);

        setButton(help, inventory.getSize()-1);
    }
}
