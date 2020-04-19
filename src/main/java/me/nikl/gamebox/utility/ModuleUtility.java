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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package me.nikl.gamebox.utility;

import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.exceptions.module.InvalidModuleException;
import me.nikl.gamebox.module.ModulesManager;
import me.nikl.gamebox.module.data.DependencyData;
import me.nikl.gamebox.module.data.VersionedCloudModule;
import me.nikl.gamebox.module.local.LocalModule;
import me.nikl.gamebox.module.local.LocalModuleData;
import me.nikl.gamebox.module.local.VersionedModule;
import me.nikl.gamebox.utility.versioning.VersionRangeUtility;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModuleUtility {

    public static void validateLocalModuleData(LocalModuleData localModuleData) throws InvalidModuleException {
        if (localModuleData.getId() == null || localModuleData.getId().replaceAll("\\s", "").isEmpty()) {
            throw new InvalidModuleException("No valid module id found");
        }
    }

    public static void fillDefaults(LocalModuleData localModuleData) {
        if (localModuleData.getName() == null || localModuleData.getName().replaceAll("\\s", "").isEmpty()) {
            localModuleData.setName(localModuleData.getId());
        }
    }

    public static List<LocalModule> sortModulesByDependencies(Collection<LocalModule> modules) {
        List<LocalModule> sortedModules = new ArrayList<>(modules);
        sortedModules.sort((localModule1, localModule2) -> {
            List<DependencyData> dependencyData1 = localModule1.getVersionData().getDependencies();
            List<DependencyData> dependencyData2 = localModule2.getVersionData().getDependencies();
            if (dependencyData1.isEmpty() && dependencyData2.isEmpty()) {
                return 0;
            }
            if (dependencyData1.isEmpty()) {
                return 1;
            }
            if (dependencyData2.isEmpty()) {
                return -1;
            }
            if (dependencyData1.stream().anyMatch(dep -> dep.getId().equals(localModule2.getId()))) {
                return -1;
            }
            if (dependencyData2.stream().anyMatch(dep -> dep.getId().equals(localModule1.getId()))) {
                return 1;
            }
            return 0;
        });
        return sortedModules;
    }

    public static DependencyReport checkDependencies(ModulesManager modulesManager, VersionedCloudModule module) {
        Map<String, VersionedModule> versionedModules = new HashMap<>();
        modulesManager.getLoadedVersionedModules().forEach(versionedModule -> versionedModules.put(versionedModule.getId(), versionedModule));
        versionedModules.put(module.getId(), getVersionedModuleFromCloudModule(module)); // this might overwrite a currently installed version of the same module
        return checkDependencies(versionedModules);
    }

    @NotNull
    public static DependencyReport checkDependencies(Map<String, VersionedModule> versionedModules) {
        Map<String, VersionedModule> previous = new HashMap<>(versionedModules);
        VersionedModule gameBoxModule = GameBoxSettings.getGameBoxData();
        versionedModules.put(gameBoxModule.getId(), gameBoxModule);
        List<String> log = new ArrayList<>();
        boolean foundIssue = true;
        while (foundIssue && !versionedModules.isEmpty()) {
            foundIssue = false;
            Iterator<VersionedModule> versionedModule = versionedModules.values().iterator();
            while (versionedModule.hasNext()) {
                VersionedModule currentModule = versionedModule.next();
                for (DependencyData dependencyData : currentModule.getVersionData().getDependencies()) {
                    VersionedModule dependency = versionedModules.get(dependencyData.getId());
                    if (dependency == null) {
                        if (dependencyData.isSoftDependency()) {
                            continue;
                        }
                        log.add("The dependency '" + dependencyData.getId()
                                + "' is missing for the module '" + currentModule.getId() + "'");
                        log.add("   " + currentModule.getId() + " asks for a version in the range '"
                                + dependencyData.getVersionConstrain() + "'");
                        foundIssue = true;
                        versionedModule.remove();
                        break;
                    }
                    if (dependencyData.getVersionConstrain() == null || dependencyData.getVersionConstrain().isEmpty()) {
                        continue;
                    }
                    try {
                        if (!VersionRangeUtility.isInVersionRange(dependency.getVersionData().getVersion(), dependencyData.getVersionConstrain())) {
                            if (dependencyData.isSoftDependency()) {
                                continue;
                            }
                            log.add("'" + currentModule.getId() + "' asks for '"
                                    + dependency.getId() + "' with the version constrain '"
                                    + dependencyData.getVersionConstrain() + "'");
                            log.add("   The installed version is '" + dependency.getVersionData().getVersion().toString() + "'");
                            foundIssue = true;
                            versionedModule.remove();
                            break;
                        }
                    } catch (ParseException e) {
                        // can be ignored, since the version ranges are parsed before
                    }
                }
            }
        }
        return new DependencyReport(previous, versionedModules, log);
    }

    public static VersionedModule getVersionedModuleFromCloudModule(VersionedCloudModule module) {
        return new LocalModule(new LocalModuleData()
                .withId(module.getId())
                .withAuthors(module.getAuthors())
                .withDependencies(module.getDependencies())
                .withDescription(module.getDescription())
                .withName(module.getName())
                .withSourceUrl(module.getSourceUrl())
                .withUpdatedAt(module.getUpdatedAt())
                .withVersion(module.getVersion())
        );
    }

    public static class DependencyReport {
        private List<String> log;
        private List<String> removedModules = new ArrayList<>();

        public DependencyReport(Map<String, VersionedModule> previous, Map<String, VersionedModule> afterwards, List<String> log) {
            this.log = log;
            for (String id : previous.keySet()) {
                if (!afterwards.containsKey(id)) {
                    this.removedModules.add(id);
                }
            }
        }

        public List<String> getLog() {
            return log;
        }

        public List<String> getRemovedModules() {
            return removedModules;
        }

        public boolean isNotOk() {
            return !removedModules.isEmpty();
        }

        public Map<String, LocalModule> filter(Map<String, LocalModule> modules) {
            return modules.entrySet().stream().filter(entry -> !removedModules.contains(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }
}
