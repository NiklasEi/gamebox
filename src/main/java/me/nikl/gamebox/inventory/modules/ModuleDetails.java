package me.nikl.gamebox.inventory.modules;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.inventory.GuiManager;
import me.nikl.gamebox.inventory.modules.guis.PaginatedDetailsGui;
import me.nikl.gamebox.module.data.CloudModuleData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ModuleDetails {
    private GameBox gameBox;
    private GuiManager guiManager;
    private Map<String, PaginatedDetailsGui> moduleDetailPages = new HashMap<>();

    public ModuleDetails (GameBox gameBox, GuiManager guiManager) {
        this.gameBox = gameBox;
        this.guiManager = guiManager;
    }

    public void addDetailsForModuleId(CloudModuleData data) {
        PaginatedDetailsGui preparedGui = new PaginatedDetailsGui(gameBox, guiManager, data);
        moduleDetailPages.put(data.getId(), preparedGui);
    }

    public boolean openDetailsView(Player whoClicked, String moduleId, int pageNumber) {
        if (!moduleDetailPages.containsKey(moduleId)) {
            return false;
        }
        return moduleDetailPages.get(moduleId).openPage(whoClicked, pageNumber);
    }
}
