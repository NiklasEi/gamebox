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
import me.nikl.gamebox.exceptions.module.GameBoxCloudException;
import me.nikl.gamebox.exceptions.module.InvalidModuleException;
import me.nikl.gamebox.exceptions.module.ModuleVersionException;
import me.nikl.gamebox.module.cloud.CloudFacade;
import me.nikl.gamebox.module.cloud.CloudManager;
import me.nikl.gamebox.module.data.CloudModuleData;
import me.nikl.gamebox.module.data.ModuleBasicData;
import me.nikl.gamebox.module.data.VersionData;
import me.nikl.gamebox.module.local.LocalModule;
import me.nikl.gamebox.module.settings.ModulesSettings;
import me.nikl.gamebox.utility.FileUtility;
import me.nikl.gamebox.utility.ModuleUtility;
import me.nikl.gamebox.utility.versioning.SemanticVersion;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.*;

/**
 * @author Niklas Eicker
 */
public class ModulesManager {
    private GameBox gameBox;
    private CloudManager cloudManager;
    private File modulesDir;
    private File modulesFile;
    private ModulesSettings modulesSettings;
    private Map<String, LocalModule> localModules = new HashMap<>();
    private Map<String, GameBoxModule> loadedModules = new HashMap<>();
    private Set<String> hasUpdateAvailable = new HashSet<>();

    public ModulesManager(GameBox gameBox) {
        this.gameBox = gameBox;
        connectToCloud();
        prepareFiles();
        loadModuleSettings();
        collectLocalModules();
        checkDependencies();
        //collectLocalModuleUpdates();
        loadLocalModules();
    }

    private void checkDependencies() {
        ModuleUtility.DependencyReport report = ModuleUtility.checkDependencies(this.localModules);
        if (!report.isOk()) {
            report.getLog().forEach(s -> gameBox.getLogger().severe(s));
            // ToDo: info about version range? Link to docs
        }
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
        Constructor constructor = new Constructor(ModulesSettings.class);
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(constructor, representer);
        try {
            this.modulesSettings = yaml.loadAs(new FileInputStream(modulesFile), ModulesSettings.class);
            // prevent NPE for empty modules file
            modulesSettings.setModules(modulesSettings.getModules() == null ? new HashMap<>() : modulesSettings.getModules());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void connectToCloud() {
        this.cloudManager = new CloudManager(gameBox, new CloudFacade());
        try {
            cloudManager.updateCloudContent();
        } catch (GameBoxCloudException e) {
            gameBox.getLogger().severe("Error while attempting to load cloud content");
            e.printStackTrace();
        }
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
                gameBox.getLogger().severe("Skipping...");
            }
        }
    }

    private void collectLocalModuleUpdates() {
        hasUpdateAvailable.clear();
        for (String moduleId : localModules.keySet()) {
            if (cloudManager.hasUpdate(localModules.get(moduleId))) {
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

    public void installModule(String moduleId) {
        gameBox.getLogger().fine("Install module '" + moduleId +"'...");
        try {
            CloudModuleData cloudModule = cloudManager.getModuleData(moduleId);
            if (localModules.containsKey(moduleId) && localModules.get(moduleId).getVersionData().getVersion().equals(cloudModule.getLatestVersion())) {
                // module already installed!
                gameBox.getLogger().fine("Attempted to install already installed module '" + moduleId +"' @" + cloudModule.getLatestVersion());
                return;
            }
            installModule(cloudModule, cloudModule.getLatestVersion());
        } catch (ModuleVersionException | GameBoxCloudException e) {
            e.printStackTrace();
        }
    }

    public void installModule(String moduleId, SemanticVersion version) throws ModuleVersionException {
        try {
            installModule(cloudManager.getModuleData(moduleId), version);
        } catch (GameBoxCloudException e) {
            e.printStackTrace();
        }
    }

    public void installModule(CloudModuleData cloudModule, SemanticVersion version) throws ModuleVersionException {
        VersionData matchingVersion = null;
        for (VersionData versionData : cloudModule.getVersions()) {
            if (versionData.getVersion().equals(version)) {
                matchingVersion = versionData;
                break;
            }
        }
        if (matchingVersion == null) {
            throw new ModuleVersionException("Version '" + version.toString() + "' of the module '" + cloudModule.getId() + "' cannot be found");
        }
        cloudManager.downloadModule(cloudModule, version, new DataBase.Callback<ModuleBasicData>() {
            @Override
            public void onSuccess(ModuleBasicData result) {
                if(!(result instanceof LocalModule)) {
                    throw new IllegalArgumentException("A successfully downloaded Module should result in a LocalModule instance");
                }
                LocalModule module = (LocalModule) result;
                gameBox.getLogger().info("Download complete. Loading the module...");
                localModules.put(module.getId(), module);
                addModuleToSettings(module.getId());
                // ToDo: should be careful here with dependencies... check for any and if a reload is needed do it automatically, or ask the source of the installation for an OK
                loadModule(module);
            }

            @Override
            public void onFailure(Throwable exception, ModuleBasicData defaultResult) {
                gameBox.getLogger().severe("Error while downloading module '" + defaultResult.getName() + "' version " + version);
                if (exception != null) exception.printStackTrace();
            }
        });
    }

    private void loadModule(LocalModule localModule) {
        GameBoxModule instance;
        try {
            gameBox.getLogger().info("    instantiating");
            instance = (GameBoxModule) FileUtility.getClassesFromJar(localModule.getModuleJar(), GameBoxModule.class).get(0).newInstance();
            gameBox.getLogger().info("    done.");
        } catch (InstantiationException | IllegalAccessException e) {
            gameBox.getLogger().warning("Failed to instantiate module '" + localModule.getName() + "' from the jar '" + localModule.getModuleJar().getName() + "'");
            e.printStackTrace();
            unloadModule(localModule);
            return;
        }
        instance.setGameBox(gameBox);
        instance.setModuleData(localModule);
        loadedModules.put(localModule.getId(), instance);
        try {
            instance.onEnable();
        } catch (Exception e) { // catch all and skip module if there is an exception in onEnable
            gameBox.getLogger().severe("Exception while enabling " + localModule.getName() + " @" + localModule.getVersionData().getVersion().toString() + ":");
            e.printStackTrace();
            unloadModule(localModule);
        }
    }

    private void unloadModule(LocalModule localModule) {
        // ToDo: unload parent modules first!
        GameBoxModule instance = loadedModules.get(localModule.getId());
        if (instance != null) {
            try {
                instance.onDisable();
            } catch (Exception e) {
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
        Constructor constructor = new Constructor(ModulesSettings.class);
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
}
