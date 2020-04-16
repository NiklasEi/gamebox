package me.nikl.gamebox.inventory.modules.pages;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.inventory.GuiManager;
import me.nikl.gamebox.inventory.button.AButton;
import me.nikl.gamebox.inventory.button.Button;
import me.nikl.gamebox.inventory.button.ButtonFactory;
import me.nikl.gamebox.inventory.gui.AGui;
import me.nikl.nmsutilities.NmsUtility;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class ModulesPage extends AGui {
    protected int pageNum;
    private int previousPageSlot = 48;
    private int nextPageSlot = 50;
    private NmsUtility nms;

    public ModulesPage(GameBox plugin, GuiManager guiManager, int slots, String[] args, int pageNum, String title) {
        super(plugin, guiManager, slots, args, title);
        this.pageNum = pageNum;
        if (pageNum > 1) {
            setButton(ButtonFactory.createModulesPageBackButton(gameBox.lang, String.valueOf(pageNum - 2)), previousPageSlot);
        }

        Map<Integer, ItemStack> hotBarButtons = plugin.getPluginManager().getHotBarButtons();

        // set lower grid
        if (hotBarButtons.get(GameBoxSettings.exitButtonSlot) != null) {
            Button exit = new Button(hotBarButtons.get(GameBoxSettings.exitButtonSlot));
            ItemMeta meta = hotBarButtons.get(GameBoxSettings.exitButtonSlot).getItemMeta();
            exit.setItemMeta(meta);
            exit.setAction(ClickAction.CLOSE);
            setLowerButton(exit, GameBoxSettings.exitButtonSlot);
        }

        if (hotBarButtons.get(GameBoxSettings.toMainButtonSlot) != null) {
            Button main = new Button(hotBarButtons.get(GameBoxSettings.toMainButtonSlot));
            ItemMeta meta = hotBarButtons.get(GameBoxSettings.toMainButtonSlot).getItemMeta();
            main.setItemMeta(meta);
            main.setAction(ClickAction.OPEN_MAIN_GUI);
            setLowerButton(main, GameBoxSettings.toMainButtonSlot);
        }
    }

    public boolean isSlotFree(int slot) {
        return grid[slot] == null;
    }

    public boolean setButtonIfSlotLeft(AButton button) {
        int i = 0;
        while (grid[i] != null) {
            i++;
        }
        if (i >= 45) return false;
        setButton(button, i);
        return true;
    }

    public void createNextPageNavigation() {
        setButton(ButtonFactory.createModulesPageForwardButton(gameBox.lang, String.valueOf(pageNum)), nextPageSlot);
    }

    public boolean updateModule(String id, Button button) {
        for (int slot = 0; slot < grid.length; slot++) {
            if (grid[slot] != null && grid[slot].getArgs()[0].equals(id)) {
                setButton(button, slot);
                return true;
            }
        }
        return false;
    }

    public boolean removeModule(String id) {
        for (int slot = 0; slot < grid.length; slot++) {
            if (grid[slot] != null && grid[slot].getArgs()[0].equals(id)) {
                setButton(null, slot);
                return true;
            }
        }
        return false;
    }

    public boolean hasModule(String moduleId) {
        for (AButton aButton : grid) {
            if (aButton != null && aButton.getArgs()[0].equals(moduleId)) {
                return true;
            }
        }
        return false;
    }
}
