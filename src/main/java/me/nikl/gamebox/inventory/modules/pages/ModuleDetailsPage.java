package me.nikl.gamebox.inventory.modules.pages;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.inventory.GuiManager;
import me.nikl.gamebox.inventory.button.AButton;
import me.nikl.gamebox.inventory.button.Button;
import me.nikl.gamebox.inventory.button.ButtonFactory;
import me.nikl.gamebox.inventory.gui.AGui;
import me.nikl.nmsutilities.NmsFactory;
import me.nikl.nmsutilities.NmsUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class ModuleDetailsPage extends AGui {
    private int pageNum;
    private int previousPageSlot = 21;
    private int nextPageSlot = 23;
    private String moduleId;
    private NmsUtility nms = NmsFactory.getNmsUtility();

    public ModuleDetailsPage(GameBox plugin, GuiManager guiManager, int slots, String[] args, String moduleId, int pageNum) {
        super(plugin, guiManager, slots, args, "Page");
        this.pageNum = pageNum;
        this.moduleId = moduleId;
        if (pageNum > 1) {
            setButton(ButtonFactory.createModuleDetailsPageBackButton(gameBox.lang, moduleId, String.valueOf(pageNum - 2)), previousPageSlot);
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

    public void updateTitle(String title) {
        for(UUID uuid : super.inGui) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                continue;
            }
            nms.updateInventoryTitle(player, title.replaceAll("%page%", String.valueOf(pageNum)));
        }
    }

    public boolean setButtonIfSlotLeft(AButton button) {
        int i = 0;
        while (grid[i] != null) {
            i++;
        }
        if (i > 45) return false;
        setButton(button, i);
        return true;
    }

    public void createNextPageNavigation() {
        setButton(ButtonFactory.createModuleDetailsPageForwardButton(gameBox.lang, moduleId, String.valueOf(pageNum)), nextPageSlot);
    }

    public void clearPage() {
        Arrays.fill(grid, null);
    }
}
