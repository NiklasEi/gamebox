package me.nikl.gamebox.inventory.menu.games;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.games.Game;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.inventory.GUIManager;
import me.nikl.gamebox.inventory.button.Button;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

/**
 * Created by nikl on 21.02.18.
 */
public class GamePageMenu extends GameMenu{
    public GamePageMenu(GameBox gameBox, Game game) {
        super(gameBox, game);


        Map<Integer, ItemStack> hotBarButtons = gameBox.getPluginManager().getHotBarButtons();

        // set lower grid
        if (hotBarButtons.get(GameBoxSettings.toGameButtonSlot) != null) {
            Button gameGUI = new Button(hotBarButtons.get(GameBoxSettings.toGameButtonSlot));
            ItemMeta meta = hotBarButtons.get(GameBoxSettings.toGameButtonSlot).getItemMeta();
            gameGUI.setItemMeta(meta);
            gameGUI.setAction(ClickAction.OPEN_GAME_GUI);
            gameGUI.setArgs(getModuleID(), GUIManager.MAIN_GAME_GUI);
            setLowerButton(gameGUI, GameBoxSettings.toGameButtonSlot);
        }
    }
}
