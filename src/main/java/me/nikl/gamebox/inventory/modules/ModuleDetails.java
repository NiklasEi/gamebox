package me.nikl.gamebox.inventory.modules;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.inventory.GuiManager;
import me.nikl.gamebox.inventory.gui.AGui;
import me.nikl.gamebox.inventory.modules.guis.PaginatedDetailsGui;
import me.nikl.gamebox.module.data.CloudModuleData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ModuleDetails {
    private GameBox gameBox;
    private GuiManager guiManager;
    private Map<String, PaginatedDetailsGui> moduleDetailPages = new HashMap<>();

    public ModuleDetails (GameBox gameBox, GuiManager guiManager) {
        this.gameBox = gameBox;
        this.guiManager = guiManager;
    }

    public void addDetailsForModule(CloudModuleData data) {
        PaginatedDetailsGui preparedGui = new PaginatedDetailsGui(gameBox, guiManager, data);
        moduleDetailPages.put(data.getId(), preparedGui);
    }

    public void updateGuiForModule(String moduleId) {
        PaginatedDetailsGui preparedGui = moduleDetailPages.get(moduleId);
        if (preparedGui == null) {
            return;
        }
        preparedGui.updateGui();
    }

    public boolean openDetailsView(Player whoClicked, String moduleId, int pageNumber) {
        if (!moduleDetailPages.containsKey(moduleId)) {
            return false;
        }
        return moduleDetailPages.get(moduleId).openPage(whoClicked, pageNumber);
    }

    public AGui getModuleGui(UUID uuid) {
        for (PaginatedDetailsGui paginatedGui : this.moduleDetailPages.values()) {
            AGui gui = paginatedGui.getModulesGui(uuid);
            if (gui != null) {
                return gui;
            }
        }
        return null;
    }

    public boolean isInGui(UUID uuid) {
        for (PaginatedDetailsGui paginatedGui : this.moduleDetailPages.values()) {
            if (paginatedGui.isInGui(uuid)) {
                return true;
            }
        }
        return false;
    }
}
