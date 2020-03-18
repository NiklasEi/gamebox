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
import me.nikl.gamebox.exceptions.module.GameBoxCloudException;
import me.nikl.gamebox.exceptions.module.InvalidModuleException;
import me.nikl.gamebox.module.data.CloudModuleData;
import me.nikl.gamebox.module.data.ModuleBasicData;
import me.nikl.gamebox.module.local.LocalModule;
import com.google.gson.Gson;
import me.nikl.gamebox.utility.versioning.SemanticVersion;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Niklas Eicker
 */
public class CloudManager {
    private static final String API_BASE_URL = "https://api.hygames.co/gamebox/";
    //private static final String API_BASE_URL = "http://127.0.0.1:4000/gamebox/";
    private static final Gson GSON = new Gson();

    private GameBox gameBox;
    private CloudFacade facade;
    private Map<String, CloudModuleData> cloudContent = new HashMap<>();
    private Map<String, Thread> downloadingModules = new HashMap<>();

    public CloudManager(GameBox gameBox, CloudFacade facade) {
        this.gameBox = gameBox;
        this.facade = facade;
    }

    public void updateCloudContent() throws GameBoxCloudException {
        ApiResponse<CloudModuleData[]> response = this.facade.getCloudModuleData();
        if (response.getError() != null) {
            throw response.getError();
        }
        cloudContent.clear();
        for (CloudModuleData moduleData : response.getData()) {
            cloudContent.put(moduleData.getId(), moduleData);
            gameBox.getLogger().info("got moduledata for id:'" + moduleData.getId() + "'");
        }
    }

//    public void updateCloudModule(String moduleId) throws GameBoxCloudException {
//        try {
//            CloudModuleData moduleData = GSON.fromJson(new InputStreamReader(new URL(API_BASE_URL + "modules/" + moduleId).openStream()), CloudModuleData.class);
//            cloudContent.put(String.valueOf(moduleData.getId()), moduleData);
//        } catch (IOException e) {
//            throw new GameBoxCloudException(e);
//        }
//    }

    public CloudModuleData getModuleData(String moduleID) throws GameBoxCloudException {
        CloudModuleData cloudModuleData = cloudContent.get(moduleID);
        if (cloudModuleData == null) throw new GameBoxCloudException("No moduledata found for ID '" + moduleID + "'");
        return cloudModuleData;
    }

    public boolean hasUpdate(LocalModule localModule) {
        CloudModuleData cloudModule = cloudContent.get(localModule.getId());
        if (cloudModule == null) {
            // might be local module
            return false;
        }
        SemanticVersion localVersion = localModule.getVersionData().getVersion();
        SemanticVersion newestCloudVersion = cloudModule.getLatestVersion();
        return newestCloudVersion.isUpdateFor(localVersion);
    }

    public void downloadModule(CloudModuleData cloudModule, SemanticVersion version, DataBase.Callback<ModuleBasicData> callback) {
        final String fileName = cloudModule.getId() + "@" + version.toString() + ".jar";
        try {
            final File outputFile = new File(gameBox.getModulesManager().getModulesDir(), fileName);
            if (outputFile.isFile()) {
                gameBox.getLogger().info("Module " + cloudModule.getName() + " @" + version.toString() + " already exists...");
                gameBox.getLogger().info("   skipping download of '" + fileName + "'");
                try {
                    LocalModule localModule = LocalModule.fromJar(outputFile);
                    callback.onSuccess(localModule);
                } catch (InvalidModuleException e) {
                    callback.onFailure(e, null);
                }
                return;
            }
            final URL fileUrl = new URL(API_BASE_URL + "assets/modules/" + fileName);

            // download
            downloadingModules.put(fileName, new Thread(() -> {
                try (BufferedInputStream in = new BufferedInputStream(fileUrl.openStream());
                     FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                    byte dataBuffer[] = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                    }
                    LocalModule localModule = LocalModule.fromJar(outputFile);
                    callback.onSuccess(localModule);
                } catch (IOException | InvalidModuleException exception) {
                    callback.onFailure(exception, null);
                } finally {
                    downloadingModules.remove(fileName);
                }
            }));
            downloadingModules.get(fileName).start();
        } catch (MalformedURLException e) {
            callback.onFailure(e, null);
        }
    }

    public boolean isDownloading() {
        return !downloadingModules.isEmpty();
    }
}
