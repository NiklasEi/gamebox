package me.nikl.gamebox.inventory.gui.game;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.PluginManager;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.inventory.GUIManager;
import me.nikl.gamebox.inventory.button.AButton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

/**
 * @author Niklas Eicker
 */
public class GameGuiPage extends GameGui {

    public GameGuiPage(GameBox plugin, GUIManager guiManager, int slots, String gameID, String key, String title) {
        super(plugin, guiManager, slots, gameID, key, title);

        Map<Integer, ItemStack> hotBarButtons = plugin.getPluginManager().getHotBarButtons();

        // set lower grid
        if (hotBarButtons.get(PluginManager.toGameButtonSlot) != null) {
            AButton gameGUI = new AButton(hotBarButtons.get(PluginManager.toGameButtonSlot));
            ItemMeta meta = hotBarButtons.get(PluginManager.toGameButtonSlot).getItemMeta();
            gameGUI.setItemMeta(meta);
            gameGUI.setAction(ClickAction.OPEN_GAME_GUI);
            gameGUI.setArgs(gameID, GUIManager.MAIN_GAME_GUI);
            setLowerButton(gameGUI, PluginManager.toGameButtonSlot);
        }
    }
}
