package me.nikl.gamebox.inventory.modules.guis;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.exceptions.module.GameBoxCloudException;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.inventory.GuiManager;
import me.nikl.gamebox.inventory.button.Button;
import me.nikl.gamebox.inventory.modules.pages.ModuleDetailsPage;
import me.nikl.gamebox.module.cloud.CloudService;
import me.nikl.gamebox.module.data.CloudModuleData;
import me.nikl.gamebox.module.data.CloudModuleDataWithVersions;
import me.nikl.gamebox.module.data.VersionData;
import me.nikl.nmsutilities.NmsFactory;
import me.nikl.nmsutilities.NmsUtility;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PaginatedDetailsGui {
    private GameBox gameBox;
    private GuiManager guiManager;
    private List<ModuleDetailsPage> pages = new ArrayList<>();
    private int gridSize = 54;
    private NmsUtility nms = NmsFactory.getNmsUtility();
    private CloudModuleDataWithVersions data;
    private CloudService cloudService;

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
        this.pages.add(new ModuleDetailsPage(gameBox, guiManager, gridSize, new String[]{"0"}, data.getId(), 1));
    }

    public boolean openPage(Player whoClicked, int pageNumber) {
        if (pageNumber >= pages.size()) {
            return false;
        }
        GameBox.openingNewGUI = true;
        boolean open = pages.get(pageNumber).open(whoClicked);
        GameBox.openingNewGUI = false;
        if (open) {
            this.updateModuleData();
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

    private void updateGui() {
        clearPages();
        SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
        for (VersionData version : data.getVersions()) {
            ItemStack book = new ItemStack(Material.BOOK);
            Button versionButton = new Button(book);
            ItemMeta meta = versionButton.getItemMeta();
            meta.setDisplayName(version.getVersion().toString());
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("Released: " + format.format(new Date(version.getUpdatedAt())));
            lore.add("");
            lore.add("Click to download");
            meta.setLore(lore);
            versionButton.setItemMeta(meta);
            versionButton.setActionAndArgs(ClickAction.DISPATCH_PLAYER_COMMAND, String.format("/gba module i %s %s", data.getId(), version.getVersion().toString()));
            setButton(versionButton);
        }
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
        ModuleDetailsPage newPage = new ModuleDetailsPage(gameBox, guiManager, gridSize, new String[]{String.valueOf(pages.size())}, data.getId(), pages.size() + 1);
        this.pages.add(newPage);
        return newPage;
    }

    public void setButton(Button button) {
        ModuleDetailsPage lastPage = pages.get(pages.size() - 1);
        if (!lastPage.setButtonIfSlotLeft(button)) {
            ModuleDetailsPage newPage = this.addPage();
            newPage.setButtonIfSlotLeft(button);
        }
    }
}
