package me.nikl.gamebox.guis.gui.game;

import me.nikl.gamebox.ClickAction;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.PluginManager;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

/**
 * Created by Niklas on 14.02.2017.
 *
 *
 */
public class GameGuiPage extends GameGui{
    private String title;

    public GameGuiPage(GameBox plugin, GUIManager guiManager, int slots, String gameID, String key, String title) {
        super(plugin, guiManager, slots, gameID, key);
        this.gameID = gameID;
        this.key = key;
        this.title = title;

        Map<Integer, ItemStack> hotBarButtons = plugin.getPluginManager().getHotBarButtons();

        // set lower grid
        AButton gameGUI = new AButton(hotBarButtons.get(PluginManager.toGame).getData(), 1);
        ItemMeta meta = hotBarButtons.get(PluginManager.toGame).getItemMeta();
        gameGUI.setItemMeta(meta);
        gameGUI.setAction(ClickAction.OPEN_GAME_GUI);
        gameGUI.setArgs(gameID, GUIManager.MAIN_GAME_GUI);
        setLowerButton(gameGUI, PluginManager.toGame);
    }

    public String getTitle() {
        return title;
    }
}
