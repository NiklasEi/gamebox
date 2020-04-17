package me.nikl.gamebox.inventory.modules.guis;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.exceptions.module.GameBoxCloudException;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.inventory.GuiManager;
import me.nikl.gamebox.inventory.button.AButton;
import me.nikl.gamebox.inventory.button.Button;
import me.nikl.gamebox.inventory.gui.AGui;
import me.nikl.gamebox.inventory.modules.pages.ModuleDetailsPage;
import me.nikl.gamebox.module.GameBoxModule;
import me.nikl.gamebox.module.cloud.CloudService;
import me.nikl.gamebox.module.data.CloudModuleData;
import me.nikl.gamebox.module.data.CloudModuleDataWithVersions;
import me.nikl.gamebox.module.data.VersionData;
import me.nikl.gamebox.utility.versioning.SemanticVersion;
import me.nikl.nmsutilities.NmsFactory;
import me.nikl.nmsutilities.NmsUtility;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PaginatedDetailsGui {
    private GameBox gameBox;
    private GuiManager guiManager;
    private List<ModuleDetailsPage> pages = new ArrayList<>();
    private int gridSize = 54;
    private NmsUtility nms = NmsFactory.getNmsUtility();
    private CloudModuleDataWithVersions data;
    private CloudService cloudService;
    private long lastApiCall = 0L;

    public PaginatedDetailsGui(GameBox gameBox, GuiManager guiManager, CloudModuleData data) {
        this.gameBox = gameBox;
        this.guiManager = guiManager;
        this.data = new CloudModuleDataWithVersions()
                .withId(data.getId())
                .withDescription(data.getDescription())
                .withLastUpdateAt(data.getUpdatedAt())
                .withLatestVersion(data.getLatestVersion())
                .withName(data.getName())
                .withSourceUrl(data.getSourceUrl())
                .withVersions(new ArrayList<>())
                .withAuthors(data.getAuthors());
        this.cloudService = gameBox.getModulesManager().getCloudService();
        this.pages.add(new ModuleDetailsPage(gameBox, guiManager, gridSize, new String[]{"0"}, data.getId(), 1, gameBox.lang.TITLE_MODULE_DETAILS_PAGE
                .replaceAll("%moduleName%", data.getName())));
    }

    public boolean openPage(Player whoClicked, int pageNumber) {
        if (pageNumber >= pages.size()) {
            return false;
        }
        GameBox.openingNewGUI = true;
        boolean open = pages.get(pageNumber).open(whoClicked);
        GameBox.openingNewGUI = false;
        if (open) {
            long current = System.currentTimeMillis();
            if (pageNumber == 0 && current - lastApiCall > 300_000) {
                this.lastApiCall = current;
                GameBox.debug("Reloading module details for " + data.getId());
                this.updateModuleData();
            }
            return true;
        }
        return false;
    }

    private void updateModuleData() {
        updateTitle(gameBox.lang.TITLE_MODULE_DETAILS_PAGE_LOADING
                .replaceAll("%moduleName%", data.getName())
        );
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    data = cloudService.getCloudModuleDataWithVersions(data.getId());
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            updateGui();
                            updateTitle(gameBox.lang.TITLE_MODULE_DETAILS_PAGE
                                    .replaceAll("%moduleName%", data.getName())
                            );
                        }
                    }.runTask(gameBox);
                } catch (GameBoxCloudException e) {
                    e.printStackTrace();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            updateTitle("Error: Please try again later :(");
                        }
                    }.runTask(gameBox);
                }
            }
        }.runTaskAsynchronously(gameBox);
    }

    public void updateGui() {
        GameBoxModule installedModule = gameBox.getModulesManager().getModuleInstance(data.getId());
        SemanticVersion installedVersion = null;
        if (installedModule != null) {
            installedVersion = installedModule.getModuleData().getVersionData().getVersion();
        }
        clearPages();
        List<VersionData> sortedByDate = data.getVersions();
        sortedByDate.sort(Comparator.comparing(VersionData::getUpdatedAt).reversed());
        for (VersionData version : sortedByDate) {
            Map<String, String> context = getVersionContext(version);
            ItemStack book = new ItemStack(Material.BOOK);
            if (version.getVersion().equals(installedVersion)) {
                book = nms.addGlow(book);
            }
            Button versionButton = new Button(book);
            ItemMeta meta = versionButton.getItemMeta();
            if (installedVersion == null) {
                meta.setDisplayName(gameBox.lang.replaceContext(gameBox.lang.MODULE_VERSION_BUTTON_NAME, context));
                meta.setLore(gameBox.lang.replaceContext(gameBox.lang.MODULE_VERSION_BUTTON_LORE, context));
                versionButton.setAction(ClickAction.DISPATCH_PLAYER_COMMAND);
                versionButton.setArgs(String.format("/gba module i %s %s", data.getId(), version.getVersion().toString()));
            } else {
                if (installedVersion.equals(version.getVersion())) {
                    meta.setDisplayName(gameBox.lang.replaceContext(gameBox.lang.MODULE_VERSION_INSTALLED_BUTTON_NAME, context));
                    meta.setLore(gameBox.lang.replaceContext(gameBox.lang.MODULE_VERSION_INSTALLED_BUTTON_LORE, context));
                    AButton.ButtonAction removeAction = new AButton.ButtonAction(ClickAction.DISPATCH_PLAYER_COMMAND, String.format("/gba module rm %s", data.getId()));
                    versionButton.addConditionalAction(InventoryAction.MOVE_TO_OTHER_INVENTORY, removeAction);
                    versionButton.setAction(ClickAction.NOTHING);
                } else if (version.getVersion().isUpdateFor(installedVersion)) {
                    meta.setDisplayName(gameBox.lang.replaceContext(gameBox.lang.MODULE_VERSION_UPDATE_BUTTON_NAME, context));
                    meta.setLore(gameBox.lang.replaceContext(gameBox.lang.MODULE_VERSION_UPDATE_BUTTON_LORE, context));
                    versionButton.setAction(ClickAction.DISPATCH_PLAYER_COMMAND);
                    versionButton.setArgs(String.format("/gba module u %s %s", data.getId(), version.getVersion().toString()));
                } else {
                    meta.setDisplayName(gameBox.lang.replaceContext(gameBox.lang.MODULE_VERSION_OLDER_BUTTON_NAME, context));
                    meta.setLore(gameBox.lang.replaceContext(gameBox.lang.MODULE_VERSION_OLDER_BUTTON_LORE, context));
                    versionButton.setAction(ClickAction.NOTHING);
                }
            }
            versionButton.setItemMeta(meta);
            setButton(versionButton);
        }
    }

    private Map<String, String> getVersionContext(VersionData version) {
        Map<String, String> context = new HashMap<>();
        context.put("moduleName", data.getName());
        context.put("versionReleaseDate", gameBox.lang.dateFormat.format(new Date(version.getUpdatedAt())));
        context.put("version", version.getVersion().toString());
        return context;
    }

    private void clearPages() {
        for (ModuleDetailsPage page: pages) {
            page.clearPage();
        }
    }

    private void updateTitle(String title) {
        for (ModuleDetailsPage page: pages) {
            page.updateTitle(title);
        }
    }

    public ModuleDetailsPage addPage() {
        ModuleDetailsPage lastPage = pages.get(pages.size() - 1);
        lastPage.createNextPageNavigation();
        ModuleDetailsPage newPage = new ModuleDetailsPage(gameBox, guiManager, gridSize, new String[]{String.valueOf(pages.size())}, data.getId(), pages.size() + 1, gameBox.lang.TITLE_MODULE_DETAILS_PAGE
                .replaceAll("%moduleName%", data.getName()));
        this.pages.add(newPage);
        return newPage;
    }

    public void setButton(Button button) {
        for (ModuleDetailsPage page : this.pages) {
            if (!page.setButtonIfSlotLeft(button)) {
                continue;
            }
            return;
        }
        ModuleDetailsPage newPage = this.addPage();
        newPage.setButtonIfSlotLeft(button);
    }

    public AGui getModulesGui(UUID uuid) {
        for (ModuleDetailsPage page : this.pages) {
            if (page.isInGui(uuid)) {
                return page;
            }
        }
        return null;
    }

    public boolean isInGui(UUID uuid) {
        for (ModuleDetailsPage page : this.pages) {
            if (page.isInGui(uuid)) {
                return true;
            }
        }
        return false;
    }
}
