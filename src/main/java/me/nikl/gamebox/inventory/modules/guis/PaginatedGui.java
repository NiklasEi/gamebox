package me.nikl.gamebox.inventory.modules.guis;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.inventory.GuiManager;
import me.nikl.gamebox.inventory.button.Button;
import me.nikl.gamebox.inventory.gui.AGui;
import me.nikl.gamebox.inventory.modules.pages.ModulesPage;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PaginatedGui {
    private GameBox gameBox;
    private GuiManager guiManager;
    private List<ModulesPage> pages = new ArrayList<>();
    private int gridSize = 54;

    public PaginatedGui(GameBox gameBox, GuiManager guiManager) {
        this.gameBox = gameBox;
        this.guiManager = guiManager;
        this.pages.add(new ModulesPage(gameBox, guiManager, gridSize, new String[]{"0"}, 1, gameBox.lang.TITLE_MODULES_PAGE.replace("%page%", String.valueOf(1))));
    }

    public boolean openPage(Player whoClicked, int pageNumber) {
        if (pageNumber >= pages.size()) {
            return false;
        }
        GameBox.openingNewGUI = true;
        boolean open = pages.get(pageNumber).open(whoClicked);
        GameBox.openingNewGUI = false;
        return open;
    }

    public boolean isSlotFreeOnLastPage(int slot) {
        ModulesPage lastPage = pages.get(pages.size() - 1);
        return lastPage.isSlotFree(slot);
    }

    public ModulesPage addPage() {
        ModulesPage lastPage = pages.get(pages.size() - 1);
        lastPage.createNextPageNavigation();
        ModulesPage newPage = new ModulesPage(gameBox, guiManager, gridSize, new String[]{String.valueOf(pages.size())}, pages.size() + 1, gameBox.lang.TITLE_MODULES_PAGE.replace("%page%", String.valueOf(pages.size() + 1)));
        this.pages.add(newPage);
        return newPage;
    }

    public void setButton(Button button) {
        ModulesPage lastPage = pages.get(pages.size() - 1);
        if (!lastPage.setButtonIfSlotLeft(button)) {
            ModulesPage newPage = this.addPage();
            newPage.setButtonIfSlotLeft(button);
        }
    }

    public int getGridSize() {
        return this.gridSize;
    }

    public boolean updateModule(String id, Button button) {
        int page = 0;
        boolean done = false;
        while (!done && page < pages.size()) {
            done = pages.get(page).updateModule(id, button);
            page++;
        }
        return done;
    }

    public AGui getModuleGui(UUID uuid) {
        for (ModulesPage page : this.pages) {
            if (page.isInGui(uuid)) {
                return page;
            }
        }
        return null;
    }

    public boolean isInGui(UUID uuid) {
        for (ModulesPage page : this.pages) {
            if (page.isInGui(uuid)) {
                return true;
            }
        }
        return false;
    }

    public int getPageOfModule(String paddedModuleId) {
        String moduleId = paddedModuleId.replaceFirst("moduleId:", "");
        for (int page = 0; page < pages.size(); page++) {
            if (pages.get(page).hasModule(moduleId)) {
                return page;
            }
        }
        return 0;
    }

    public void clearPages() {
        for (ModulesPage page : this.pages) {
            page.clear();
        }
    }
}
