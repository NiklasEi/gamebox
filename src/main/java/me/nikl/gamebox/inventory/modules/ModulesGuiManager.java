package me.nikl.gamebox.inventory.modules;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxLanguage;
import me.nikl.gamebox.events.EnterGameBoxEvent;
import me.nikl.gamebox.events.modules.ModuleInstalledEvent;
import me.nikl.gamebox.events.modules.ModuleRemovedEvent;
import me.nikl.gamebox.exceptions.module.GameBoxCloudException;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.inventory.GuiManager;
import me.nikl.gamebox.inventory.button.Button;
import me.nikl.gamebox.inventory.gui.AGui;
import me.nikl.gamebox.inventory.modules.guis.PaginatedGui;
import me.nikl.gamebox.inventory.shop.Category;
import me.nikl.gamebox.module.data.CloudModuleData;
import me.nikl.gamebox.module.local.VersionedModule;
import me.nikl.gamebox.utility.Permission;
import me.nikl.gamebox.utility.versioning.SemanticVersion;
import me.nikl.nmsutilities.NmsFactory;
import me.nikl.nmsutilities.NmsUtility;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.logging.Level;

public class ModulesGuiManager implements Listener {
    private NmsUtility nms = NmsFactory.getNmsUtility();
    private GameBox gameBox;
    private Button mainButton;
    private GuiManager guiManager;
    private PaginatedGui modulesListGui;
    private GameBoxLanguage lang;
    private ModuleDetails moduleDetails;
    private int titleMessageSeconds = 3;
    private boolean guiLoaded = false;

    public ModulesGuiManager(GameBox gameBox, GuiManager guiManager) {
        this.gameBox = gameBox;
        this.lang = gameBox.lang;
        this.guiManager = guiManager;
        this.modulesListGui = new PaginatedGui(gameBox, guiManager);
        this.moduleDetails = new ModuleDetails(gameBox, guiManager);
        this.loadButton();
        gameBox.getServer().getPluginManager().registerEvents(this, gameBox);
    }

    public void loadGui() {
        List<CloudModuleData> cloudModuleData = gameBox.getModulesManager().getCloudService().getCloudContent();
        for (CloudModuleData data : cloudModuleData) {
            SemanticVersion installedVersion = null;
            if(gameBox.getModulesManager().getModuleInstance(data.getId()) != null) {
                installedVersion = gameBox.getModulesManager().getModuleInstance(data.getId()).getModuleData().getVersionData().getVersion();
            }
            this.moduleDetails.addDetailsForModule(data);
            this.modulesListGui.setButton(buildModuleButton(data, installedVersion));
        }
        this.guiLoaded = true;
    }

    private void removeModule(VersionedModule module) {
        try {
            CloudModuleData data = gameBox.getModulesManager().getCloudService().getModuleData(module.getId());
            Button updatedButton = buildModuleButton(data, null);
            if (this.modulesListGui.updateModule(module.getId(), updatedButton)) {
                this.moduleDetails.updateGuiForModule(module.getId());
            }
        } catch (GameBoxCloudException e) {
            this.modulesListGui.removeModule(module.getId());
        }
    }

    private void installModule(VersionedModule module) {
        try {
            CloudModuleData data = gameBox.getModulesManager().getCloudService().getModuleData(module.getId());
            Button updatedButton = buildModuleButton(data, module.getVersionData().getVersion());
            if(this.modulesListGui.updateModule(module.getId(), updatedButton)) {
                this.moduleDetails.updateGuiForModule(module.getId());
            } else {
                this.moduleDetails.addDetailsForModule(data);
                this.modulesListGui.setButton(updatedButton);
            }
        } catch (GameBoxCloudException e) {
            Button updatedButton = buildPrivateModuleButton(module, module.getVersionData().getVersion());
            if(!this.modulesListGui.updateModule(module.getId(), updatedButton)) {
                this.modulesListGui.setButton(updatedButton);
            }
        }
    }

    private Button buildPrivateModuleButton(VersionedModule data, SemanticVersion installedVersion) {
        Map<String, String> context = getModuleContext(data);
        return buildPrivateModuleButton(context, data.getId(), installedVersion);
    }

    private Button buildModuleButton(CloudModuleData data, SemanticVersion installedVersion) {
        Map<String, String> context = getModuleContext(data);
        return buildModuleButton(context, data.getId(), installedVersion);
    }

    private Button buildModuleButton(Map<String, String> context, String id, SemanticVersion installedVersion) {
        ItemStack icon = new ItemStack(Material.BOOK);
        if (installedVersion != null) {
            context.put("moduleInstalledVersion", installedVersion.toString());
            icon = nms.addGlow(icon);
        }
        Button button = new Button(icon);
        ItemMeta meta = button.getItemMeta();
        if (installedVersion == null) {
            meta.setDisplayName(gameBox.lang.replaceContext(gameBox.lang.MODULE_BUTTON_NAME, context));
            meta.setLore(gameBox.lang.replaceContext(gameBox.lang.MODULE_BUTTON_LORE, context));
        } else {
            meta.setDisplayName(gameBox.lang.replaceContext(gameBox.lang.MODULE_INSTALLED_BUTTON_NAME, context));
            meta.setLore(gameBox.lang.replaceContext(gameBox.lang.MODULE_INSTALLED_BUTTON_LORE, context));
        }
        button.setItemMeta(meta);
        button.setAction(ClickAction.OPEN_MODULE_DETAILS);
        button.setArgs(id);
        return button;
    }

    private Button buildPrivateModuleButton(Map<String, String> context, String id, SemanticVersion installedVersion) {
        ItemStack icon = new ItemStack(Material.BOOK);
        context.put("moduleInstalledVersion", installedVersion.toString());
        icon = nms.addGlow(icon);
        Button button = new Button(icon);
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName(gameBox.lang.replaceContext(gameBox.lang.MODULE_PRIVATE_BUTTON_NAME, context));
        meta.setLore(gameBox.lang.replaceContext(gameBox.lang.MODULE_PRIVATE_BUTTON_LORE, context));
        button.setItemMeta(meta);
        button.setAction(ClickAction.NOTHING);
        return button;
    }

    private Map<String, String> getModuleContext(VersionedModule data) {
        Map<String, String> context = new HashMap<>();
        context.put("moduleName", data.getName());
        context.put("moduleLastReleaseDate", gameBox.lang.dateFormat.format(new Date(data.getVersionData().getUpdatedAt())));
        context.put("moduleLastReleaseVersion", gameBox.lang.MODULE_VERSION_UNKNOWN);
        context.put("moduleAuthors", String.join(", ", data.getAuthors()));
        context.put("moduleDescription", data.getDescription());
        context.put("moduleId", data.getId());
        context.put("moduleSourceUrl", data.getSourceUrl());
        return context;
    }

    private Map<String, String> getModuleContext(CloudModuleData data) {
        Map<String, String> context = new HashMap<>();
        context.put("moduleName", data.getName());
        context.put("moduleLastReleaseDate", gameBox.lang.dateFormat.format(new Date(data.getUpdatedAt())));
        context.put("moduleLastReleaseVersion", data.getLatestVersion().toString());
        context.put("moduleAuthors", String.join(", ", data.getAuthors()));
        context.put("moduleDescription", data.getDescription());
        context.put("moduleId", data.getId());
        context.put("moduleSourceUrl", data.getSourceUrl());
        return context;
    }

    public boolean openModulesPage(Player whoClicked, String[] args) {
        boolean saved = false;

        if (gameBox.getPluginManager().doesNotHaveSavedContents(whoClicked.getUniqueId())) {
            EnterGameBoxEvent enterEvent = new EnterGameBoxEvent(whoClicked, args[0], args[1]);
            if (!enterEvent.isCancelled()) {
                gameBox.getPluginManager().saveInventory(whoClicked);
                saved = true;
            } else {
                whoClicked.sendMessage(lang.PREFIX + " Opening the GUI was canceled with the reason: " + enterEvent.getCancelMessage());
                return false;
            }
        }

        if (!Permission.ADMIN_MODULES.hasPermission(whoClicked)) {
            if (saved) gameBox.getPluginManager().leaveGameBox(whoClicked);
            whoClicked.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);
            if (guiManager.isInMainGUI(whoClicked.getUniqueId())) {
                String currentTitle = gameBox.lang.TITLE_MAIN_GUI.replace("%player%", whoClicked.getName());
                gameBox.getInventoryTitleMessenger().sendInventoryTitle(whoClicked, gameBox.lang.TITLE_NO_PERM, currentTitle, titleMessageSeconds);
            }
            return false;
        }

        int pageNumber;
        try {
            pageNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            pageNumber = this.modulesListGui.getPageOfModule(args[0]);
        }

        if (!modulesListGui.openPage(whoClicked, pageNumber)) {
            if (saved) gameBox.getPluginManager().leaveGameBox(whoClicked);
            Bukkit.getLogger().log(Level.SEVERE, "trying to open a modules page failed");
            Bukkit.getLogger().log(Level.SEVERE, "args: " + Arrays.asList(args));
            whoClicked.sendMessage("Error");
            return false;
        }
        return true;
    }

    private void loadButton() {
        mainButton = new Button(new ItemStack(Material.CHEST));
        ItemMeta meta = mainButton.getItemMeta();
        meta.setDisplayName(gameBox.lang.BUTTON_MODULES_GUI_NAME);
        meta.setLore(gameBox.lang.BUTTON_MODULES_GUI_LORE);
        mainButton.setItemMeta(meta);
        mainButton.setAction(ClickAction.OPEN_MODULES_PAGE);
        mainButton.setArgs("0");
    }

    public Button getMainButton() {
        return this.mainButton;
    }

    public boolean openModuleDetails(Player whoClicked, String[] args) {
        boolean saved = false;

        if (gameBox.getPluginManager().doesNotHaveSavedContents(whoClicked.getUniqueId())) {
            EnterGameBoxEvent enterEvent = new EnterGameBoxEvent(whoClicked, args[0], args[1]);
            if (!enterEvent.isCancelled()) {
                gameBox.getPluginManager().saveInventory(whoClicked);
                saved = true;
            } else {
                whoClicked.sendMessage(lang.PREFIX + " Opening the GUI was canceled with the reason: " + enterEvent.getCancelMessage());
                return false;
            }
        }

        if (!Permission.ADMIN_MODULES.hasPermission(whoClicked)) {
            if (saved) gameBox.getPluginManager().leaveGameBox(whoClicked);
            whoClicked.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);
            if (guiManager.isInMainGUI(whoClicked.getUniqueId())) {
                String currentTitle = gameBox.lang.TITLE_MAIN_GUI.replace("%player%", whoClicked.getName());
                gameBox.getInventoryTitleMessenger().sendInventoryTitle(whoClicked, gameBox.lang.TITLE_NO_PERM, currentTitle, titleMessageSeconds);
            }
            return false;
        }

        int pageNumber;
        try {
            pageNumber = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            Bukkit.getLogger().log(Level.SEVERE, "failed to open module detail page due to corrupted args!");
            return false;
        }

        if (!moduleDetails.openDetailsView(whoClicked, args[0],pageNumber)) {
            if (saved) gameBox.getPluginManager().leaveGameBox(whoClicked);
            Bukkit.getLogger().log(Level.SEVERE, "trying to open a modules detail view failed");
            Bukkit.getLogger().log(Level.SEVERE, "args: " + Arrays.asList(args));
            whoClicked.sendMessage("Error");
            return false;
        }
        return true;
    }

    @EventHandler
    public void onModuleInstallEvent(ModuleInstalledEvent event) {
        VersionedModule module = event.getModule();
        if (guiLoaded) {
            installModule(module);
        }
        Bukkit.getLogger().info("installing " + event.getModule().getName() + "@" + event.getModule().getVersionData().getVersion().toString());
    }

    @EventHandler
    public void onModuleRemoveEvent(ModuleRemovedEvent event) {
        VersionedModule module = event.getModule();
        if (guiLoaded) {
            removeModule(module);
        }
        Bukkit.getLogger().info("removing " + event.getModule().getName() + "@" + event.getModule().getVersionData().getVersion().toString());
    }

    public AGui getModuleGui(UUID uuid) {
        AGui modulesGui = this.modulesListGui.getModuleGui(uuid);
        if (modulesGui != null) {
            return modulesGui;
        }
        return this.moduleDetails.getModuleGui(uuid);
    }

    public boolean isInGui(UUID uuid) {
        if (this.modulesListGui.isInGui(uuid)) {
            return true;
        }
        return this.moduleDetails.isInGui(uuid);
    }
}
