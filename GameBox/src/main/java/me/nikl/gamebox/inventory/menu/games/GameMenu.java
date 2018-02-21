package me.nikl.gamebox.inventory.menu.games;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.Module;
import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.games.Game;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.inventory.button.Button;
import me.nikl.gamebox.inventory.menu.PerPlayerMenu;
import me.nikl.gamebox.nms.NmsFactory;
import me.nikl.gamebox.utility.ItemStackUtility;
import me.nikl.gamebox.utility.StringUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

/**
 * Created by nikl on 21.02.18.
 */
public class GameMenu extends PerPlayerMenu {
    private Module module;
    public GameMenu(GameBox gameBox, Game game) {
        super(gameBox);
        size = 54;
        this.module = game.getModule();
        this.defaultTitle = StringUtility.center(game.getGameLang().NAME, 32);


        Map<Integer, ItemStack> hotBarButtons = gameBox.getPluginManager().getHotBarButtons();


        // set lower grid
        if (hotBarButtons.containsKey(GameBoxSettings.exitButtonSlot)) {
            Button exit = new Button(hotBarButtons.get(GameBoxSettings.exitButtonSlot));
            ItemMeta meta = hotBarButtons.get(GameBoxSettings.exitButtonSlot).getItemMeta();
            exit.setItemMeta(meta);
            exit.setAction(ClickAction.CLOSE);
            setLowerButton(exit, GameBoxSettings.exitButtonSlot);
        }


        if (hotBarButtons.containsKey(GameBoxSettings.toMainButtonSlot)) {
            Button main = new Button(hotBarButtons.get(GameBoxSettings.toMainButtonSlot));
            ItemMeta meta = hotBarButtons.get(GameBoxSettings.toMainButtonSlot).getItemMeta();
            main.setItemMeta(meta);
            main.setAction(ClickAction.OPEN_MAIN_GUI);
            setLowerButton(main, GameBoxSettings.toMainButtonSlot);
        }
    }

    public String getModuleID(){
        return module.getModuleID();
    }

    @Override
    public void updatePlayer(GBPlayer gbPlayer) {

    }

    @Override
    protected void onUpperClick(InventoryClickEvent event) {

    }

    @Override
    protected void onLowerClick(InventoryClickEvent event) {

    }

    @Override
    public boolean open(Player player) {
        return false;
    }

    @Override
    public String getPlayerSpezificTitle(Player player) {
        return null;
    }

    /**
     * Place a help button with the given text in the
     * lower right corner of the GUI
     *
     * @param list text that will be displayed on the button
     */
    public void setHelpButton(List<String> list) {
        Button help = new Button(NmsFactory.getNmsUtility().addGlow(ItemStackUtility.createBookWithText(list)));
        help.setAction(ClickAction.NOTHING);
        setButton(help, inventory.getSize() - 1);
    }
}
