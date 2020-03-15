package me.nikl.gamebox.cloud;

import com.google.gson.Gson;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.cloud.data.CloudModuleData;
import me.nikl.gamebox.data.database.DataBase;
import me.nikl.gamebox.exceptions.module.ModuleCloudException;
import me.nikl.gamebox.module.LocalModule;
import me.nikl.gamebox.utilities.versioning.SemanticVersion;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Niklas Eicker
 */
public class CloudManager {
    private static final String API_BASE_URL = "https://api.hygames.co/gamebox/";
    private static final Gson GSON = new Gson();

    private GameBox gameBox;
    private Map<String, CloudModuleData> cloudContent = new HashMap<>();
    private Map<String, DataBase.Callback<LocalModule>> downlodingModules = new HashMap<>();

    public CloudManager(GameBox gameBox) {
        this.gameBox = gameBox;
    }

    public void updateCloudContent() throws ModuleCloudException {
        cloudContent.clear();
        try {
            CloudModuleData[] modulesData = GSON.fromJson(new InputStreamReader(new URL(API_BASE_URL + "modules").openStream()), CloudModuleData[].class);
            for (CloudModuleData moduleData : modulesData) {
                cloudContent.put(String.valueOf(moduleData.getId()), moduleData);
            }
        } catch (IOException e) {
            throw new ModuleCloudException(e);
        }
    }

    public void updateCloudModule(String moduleId) throws ModuleCloudException {
        try {
            CloudModuleData moduleData = GSON.fromJson(new InputStreamReader(new URL(API_BASE_URL + "modules/" + moduleId).openStream()), CloudModuleData.class);
            cloudContent.put(String.valueOf(moduleData.getId()), moduleData);
        } catch (IOException e) {
            throw new ModuleCloudException(e);
        }
    }

    public CloudModuleData getModuleData(String moduleID) {
        return cloudContent.get(moduleID);
    }

    private boolean hasUpdate(LocalModule localModule) {
        CloudModuleData cloudModule = cloudContent.get(localModule.getModuleId());
        if (cloudModule == null) {
            // might be local module
            return false;
        }
        SemanticVersion localVersion = localModule.getVersionData().getVersion();
        SemanticVersion newestCloudVersion = cloudModule.getLatestVersion();
        return newestCloudVersion.isUpdateFor(localVersion);
    }
}
