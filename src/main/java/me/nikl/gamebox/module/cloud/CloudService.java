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

package me.nikl.gamebox.module.cloud;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.data.database.DataBase;
import me.nikl.gamebox.exceptions.module.CloudModuleNotFoundException;
import me.nikl.gamebox.exceptions.module.GameBoxCloudException;
import me.nikl.gamebox.exceptions.module.InvalidModuleException;
import me.nikl.gamebox.module.data.CloudModuleData;
import me.nikl.gamebox.module.data.CloudModuleDataWithVersions;
import me.nikl.gamebox.module.data.VersionedCloudModule;
import me.nikl.gamebox.module.local.LocalModule;
import me.nikl.gamebox.utility.versioning.SemanticVersion;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niklas Eicker
 */
public class CloudService {
    private GameBox gameBox;
    private CloudFacade facade;
    private long lastCloudContentUpdate = 0L;
    private Map<String, CloudModuleData> cloudContent = new HashMap<>();
    private Map<String, Thread> downloadingModules = new HashMap<>();

    public CloudService(GameBox gameBox, CloudFacade facade) {
        this.gameBox = gameBox;
        this.facade = facade;
    }

    /**
     * Cache the modules currently offered by the GameBox API
     *
     * Do not call this function on the main thread
     */
    public void cacheCloudContent() throws GameBoxCloudException {
        ApiResponse<CloudModuleData[]> response = this.facade.getCloudModuleData();
        if (response.getError() != null) {
            throw response.getError();
        }
        lastCloudContentUpdate = System.currentTimeMillis();
        cloudContent.clear();
        for (CloudModuleData moduleData : response.getData()) {
            cloudContent.put(moduleData.getId(), moduleData);
            GameBox.debug("got module data for id:'" + moduleData.getId() + "'");
        }
    }

    public CloudModuleData getCachedModuleData(String moduleID) throws GameBoxCloudException {
        CloudModuleData cloudModuleData = cloudContent.get(moduleID);
        if (cloudModuleData == null) throw new CloudModuleNotFoundException("No moduledata found for ID '" + moduleID + "'");
        return cloudModuleData;
    }

    /**
     * Cache the modules currently offered by the GameBox API
     *
     * Do not call this function on the main thread
     */
    public CloudModuleDataWithVersions getCloudModuleDataWithVersions(String moduleId) throws GameBoxCloudException {
        ApiResponse<CloudModuleDataWithVersions> response = this.facade.getCloudModuleDataWithVersions(moduleId);
        if (response.getError() != null) {
            throw response.getError();
        }
        return response.getData();
    }

    public boolean hasCachedUpdate(LocalModule localModule) {
        CloudModuleData cloudModule = cloudContent.get(localModule.getId());
        if (cloudModule == null) {
            // might be local module
            return false;
        }
        SemanticVersion localVersion = localModule.getVersionData().getVersion();
        SemanticVersion latestCloudVersion = cloudModule.getLatestVersion();
        return latestCloudVersion.isUpdateFor(localVersion);
    }

    public void downloadModule(VersionedCloudModule module, DataBase.Callback<LocalModule> callback) {
        final String fileName = module.getId() + "@" + module.getVersion().toString() + ".jar";
        try {
            final File outputFile = new File(gameBox.getModulesManager().getModulesDir(), fileName);
            if (outputFile.isFile()) {
                gameBox.getLogger().info("Module " + module.getName() + " @" + module.getVersion().toString() + " already exists...");
                gameBox.getLogger().info("   skipping download of '" + fileName + "'");
                try {
                    LocalModule localModule = LocalModule.fromJar(outputFile);
                    if (localModule == null) {
                        gameBox.getModulesManager().softDeleteJarFile(outputFile);
                        throw new InvalidModuleException("Failed to load local module from jar file");
                    }
                    callback.onSuccess(localModule);
                } catch (InvalidModuleException | IOException e) {
                    callback.onFailure(e, null);
                }
                return;
            }
            final URL fileUrl = new URL(module.getDownloadUrl());
            downloadModule(fileUrl, fileName, callback);
        } catch (MalformedURLException e) {
            callback.onFailure(e, null);
        }
    }

    public void downloadModule(URL fileUrl, String fileName, DataBase.Callback<LocalModule> callback) {
        final File outputFile = new File(gameBox.getModulesManager().getModulesDir(), fileName);
        downloadingModules.put(fileName, new Thread(() -> {
            try (BufferedInputStream in = new BufferedInputStream(fileUrl.openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                byte[] dataBuffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
                LocalModule localModule = LocalModule.fromJar(outputFile);
                if (localModule == null) {
                    if (outputFile.exists()) {
                        Files.delete(outputFile.toPath());
                    }
                    throw new InvalidModuleException("Failed to load LocalModule from jar file");
                }
                callback.onSuccess(localModule);
            } catch (IOException | InvalidModuleException exception) {
                callback.onFailure(exception, null);
            } finally {
                downloadingModules.remove(fileName);
            }
        }));
        downloadingModules.get(fileName).start();
    }

    /**
     * Get the versioned cloud module data from the GameBox API
     *
     * Do not call this function on the main thread
      */
    public VersionedCloudModule getVersionedCloudModule(String moduleId, SemanticVersion version) throws GameBoxCloudException {
        ApiResponse<VersionedCloudModule> response = this.facade.getVersionedCloudModuleData(moduleId, version);
        if (response.getError() != null) {
            throw response.getError();
        }
        return response.getData();
    }

    public List<CloudModuleData> getCachedCloudContent() {
        return new ArrayList<>(this.cloudContent.values());
    }

    public long secondsSinceLastCloudContentCacheUpdate() {
        return Math.round((System.currentTimeMillis() - lastCloudContentUpdate)/1000.);
    }
}
