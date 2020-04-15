/*
 * GameBox
 * Copyright (C) 2019  Niklas Eicker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.nikl.gamebox.module;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.data.database.DataBase;
import me.nikl.gamebox.events.modules.ModuleInstallEvent;
import me.nikl.gamebox.events.modules.ModuleRemoveEvent;
import me.nikl.gamebox.exceptions.module.CloudModuleVersionNotFoundException;
import me.nikl.gamebox.exceptions.module.GameBoxCloudException;
import me.nikl.gamebox.exceptions.module.InvalidModuleException;
import me.nikl.gamebox.module.cloud.CloudFacade;
import me.nikl.gamebox.module.cloud.CloudService;
import me.nikl.gamebox.module.data.VersionedCloudModule;
import me.nikl.gamebox.module.local.LocalModule;
import me.nikl.gamebox.module.local.VersionedModule;
import me.nikl.gamebox.module.settings.ModulesSettings;
import me.nikl.gamebox.utility.FileUtility;
import me.nikl.gamebox.utility.ModuleUtility;
import me.nikl.gamebox.utility.versioning.SemanticVersion;
import org.bukkit.scheduler.BukkitRunnable;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Niklas Eicker
 */
public class ModulesManager {
    private GameBox gameBox;
    private CloudService cloudService;
    private File modulesDir;
    private File modulesFile;
    private ModulesSettings modulesSettings;
    private Map<String, LocalModule> localModules = new HashMap<>();
    private Map<String, GameBoxModule> loadedModules = new HashMap<>();
    private Set<String> hasUpdateAvailable = new HashSet<>();

    public ModulesManager(GameBox gameBox) {
        this.gameBox = gameBox;
        load();
    }

    private void load() {
        prepareModulesDirectory();
        connectToCloud();
        prepareFiles();
        loadModuleSettings();
        collectLocalModules();
        checkLocalModulesDependencies();
        loadLocalModules();
    }

    private void prepareModulesDirectory() {
        this.modulesDir = new File(gameBox.getDataFolder(), "modules");
        if (!modulesDir.isDirectory()) {
            modulesDir.mkdirs();
            try {
                FileUtility.copyResource("modules/modules.yml", new File(modulesDir, "modules.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkLocalModulesDependencies() {
        ModuleUtility.DependencyReport report = ModuleUtility.checkDependencies(new HashMap<>(this.localModules));
        if (report.isNotOk()) {
            this.localModules = report.filter(this.localModules);
            gameBox.getLogger().severe("Dependency issues while loading local modules");
            logDependencyReport(report);
        }
    }

    private void logDependencyReport(ModuleUtility.DependencyReport report) {
        report.getLog().forEach(s -> gameBox.getLogger().severe(s));
        gameBox.getLogger().severe("For more information please see:");
        gameBox.getLogger().severe("  Semantic versioning: https://semver.org/");
        gameBox.getLogger().severe("  Version ranges:      https://docs.npmjs.com/misc/semver#ranges");
        gameBox.getLogger().severe("                       https://thoughtbot.com/blog/rubys-pessimistic-operator");
    }

    private void loadLocalModules() {
        Map<String, LocalModule> modulesToLoad = localModules;
        List<LocalModule> sortedModules = ModuleUtility.sortModulesByDependencies(modulesToLoad.values());
        for (LocalModule localModule : sortedModules) {
            gameBox.getLogger().fine("Loading module '" + localModule.getName() + "'...");
            if (loadedModules.containsKey(localModule.getId())) {
                gameBox.getLogger().fine("    already loaded! Skipping...");
                continue;
            }
            loadModule(localModule);
        }
    }

    private void loadModuleSettings() {
        //Yaml yaml = new Yaml(new Constructor(ModulesSettings.class));
        CustomClassLoaderConstructor constructor = new CustomClassLoaderConstructor(ModulesSettings.class.getClassLoader());
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(constructor, representer);
        try {
            this.modulesSettings = yaml.loadAs(new FileInputStream(modulesFile), ModulesSettings.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void connectToCloud() {
        this.cloudService = new CloudService(gameBox, new CloudFacade());
        BukkitRunnable hookAfterConnectingToCloud = new BukkitRunnable() {
            @Override
            public void run() {
                gameBox.hookAfterConnectingToCloud();
            }
        };
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    cloudService.updateCloudContent();
                    hookAfterConnectingToCloud.runTask(gameBox);
                } catch (GameBoxCloudException e) {
                    gameBox.getLogger().severe("Error while attempting to load cloud content");
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(gameBox);
    }

    private void prepareFiles() {
        modulesDir = new File(gameBox.getDataFolder(), "modules");
        if (modulesDir.mkdirs()) {
            gameBox.getLogger().info("Created Modules Directory");
        }
        modulesFile = new File(modulesDir, "modules.yml");
        if (!modulesFile.isFile()) {
            try {
                FileUtility.copyResource("modules/modules.yml", modulesFile);
                gameBox.getLogger().info("Copied default 'modules.yml' file");
            } catch (IOException e) {
                gameBox.getLogger().warning("Error while attempting to create a new module settings file:");
                e.printStackTrace();
            }
        }
    }

    private void collectLocalModules() {
        // ToDo: check the module settings! Ignore disabled modules
        List<File> jars = FileUtility.getAllJars(modulesDir);
        for (File jar : jars) {
            try {
                LocalModule localModule = LocalModule.fromJar(jar);
                localModules.put(localModule.getId(), localModule);
            } catch (InvalidModuleException e) {
                gameBox.getLogger().severe("Error while loading module from the jar '" + jar.getName() + "'");
                e.printStackTrace();
                gameBox.getLogger().severe("Skipping this module...");
            }
        }
    }

    public void collectLocalModuleUpdates() {
        hasUpdateAvailable.clear();
        for (String moduleId : localModules.keySet()) {
            if (cloudService.hasUpdate(localModules.get(moduleId))) {
                hasUpdateAvailable.add(moduleId);
            }
        }
    }

    private void registerAllLocalModules() {
        List<File> jars = FileUtility.getAllJars(modulesDir);

        // ToDo: read version, dependencies into module settings and save defaults in module settings file
    }

    public File getModulesDir() {
        return this.modulesDir;
    }

    public void installModule(VersionedCloudModule module) {
        if (!checkDependencies(module, true)) {
            return;
        }
        new ModuleInstallEvent(ModuleUtility.getVersionedModuleFromCloudModule(module));
        GameBox.debug("Install module '" + module.getName() +"@" + module.getVersion().toString() + "'");
        cloudService.downloadModule(module, new DataBase.Callback<LocalModule>() {
            @Override
            public void onSuccess(LocalModule module) {
                GameBox.debug("Download complete. Loading the module...");
                localModules.put(module.getId(), module);
                addModuleToSettings(module.getId());
                // ToDo: should be careful here with dependencies... check for any and if a reload is needed do it automatically, or ask the source of the installation for an OK
                loadModule(module);
            }

            @Override
            public void onFailure(Throwable exception, LocalModule defaultResult) {
                gameBox.getLogger().severe("Error while downloading module");
                if (exception != null) exception.printStackTrace();
            }
        });
    }

    private boolean checkDependencies(VersionedCloudModule module, boolean log) {
        ModuleUtility.DependencyReport report = ModuleUtility.checkDependencies(this, module);
        if (report.isNotOk()) {
            if (log) {
                logDependencyReport(report);
            }
            return false;
        }
        return true;
    }

    public void installModule(String moduleId, SemanticVersion version) throws GameBoxCloudException {
        try {
            installModule(cloudService.getVersionedCloudModule(moduleId, version));
        } catch (CloudModuleVersionNotFoundException e) {
            throw e;
        } catch (GameBoxCloudException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void loadModule(LocalModule localModule) {
        GameBoxModule gameBoxModule;
        try {
            GameBox.debug("    instantiating " + localModule.getName());
            gameBoxModule = (GameBoxModule) FileUtility.getClassesFromJar(localModule.getModuleJar(), GameBoxModule.class).get(0).getConstructor().newInstance();
            GameBox.debug("    done.");
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            gameBox.getLogger().warning("Failed to instantiate module '" + localModule.getName() + "' from the jar '" + localModule.getModuleJar().getName() + "'");
            e.printStackTrace();
            removeModule(localModule);
            return;
        }
        gameBoxModule.setGameBox(gameBox);
        gameBoxModule.setModuleData(localModule);
        loadedModules.put(localModule.getId(), gameBoxModule);
        try {
            gameBoxModule.onEnable();
        } catch (Throwable e) { // catch all and skip module if there is an exception in onEnable
            gameBox.getLogger().severe("Exception while enabling " + localModule.getName() + " @" + localModule.getVersionData().getVersion().toString());
            e.printStackTrace();
            gameBox.getLogger().severe("Skipping...");
            removeModule(localModule);
        }
    }

    public void removeModule(LocalModule localModule) {
        // ToDo: unload parent modules first!
        GameBoxModule gameBoxModule = loadedModules.get(localModule.getId());
        new ModuleRemoveEvent(localModule);
        if (gameBoxModule != null) {
            try {
                gameBoxModule.onDisable();
                gameBox.getGameRegistry().unregisterGamesForModuleId(localModule.getId());
                gameBoxModule.getModuleData().getModuleJar().delete();
            } catch (Throwable e) {
                gameBox.getLogger().severe("Exception while disabling " + localModule.getName() + " @" + localModule.getVersionData().getVersion().toString() + ":");
                e.printStackTrace();
            } finally {
                loadedModules.remove(localModule.getId());
            }
        }
    }

    private void addModuleToSettings(String moduleId) {
        Map<String, ModulesSettings.ModuleSettings> currentSettings = modulesSettings.getModules();
        currentSettings.putIfAbsent(moduleId, new ModulesSettings.ModuleSettings());
        modulesSettings.setModules(currentSettings);
        dumpModuleSettings();
    }

    private void removeModuleFromSettings(String moduleId) {
        Map<String, ModulesSettings.ModuleSettings> currentSettings = modulesSettings.getModules();
        currentSettings.remove(moduleId);
        modulesSettings.setModules(currentSettings);
        dumpModuleSettings();
    }

    private void updateModuleSettings(String moduleId, ModulesSettings.ModuleSettings settings) {
        Map<String, ModulesSettings.ModuleSettings> currentSettings = modulesSettings.getModules();
        currentSettings.put(moduleId, settings);
        modulesSettings.setModules(currentSettings);
        dumpModuleSettings();
    }

    private void dumpModuleSettings() {
        CustomClassLoaderConstructor constructor = new CustomClassLoaderConstructor(ModulesSettings.class.getClassLoader());
        Yaml yaml = new Yaml(constructor);
        try {
            yaml.dump(modulesSettings, new FileWriter(modulesFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the instance of a module by its ID
     * @param moduleID the module to get
     * @return module instance or null
     */
    public GameBoxModule getModuleInstance(String moduleID) {
        return loadedModules.get(moduleID);
    }

    public CloudService getCloudService() {
        return this.cloudService;
    }

    public void shutDown() {
        this.loadedModules.values().forEach(GameBoxModule::onDisable);
        this.loadedModules.clear();
        this.gameBox = null;
    }

    public List<VersionedModule> getLoadedVersionedModules() {
        return this.loadedModules.values().stream().map(GameBoxModule::getModuleData).collect(Collectors.toList());
    }
}
