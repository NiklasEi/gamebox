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
import me.nikl.gamebox.module.data.DependencyData;
import me.nikl.gamebox.module.local.LocalModule;
import me.nikl.gamebox.module.local.LocalModuleData;
import me.nikl.gamebox.module.local.VersionedModule;
import me.nikl.gamebox.utility.versioning.VersionRangeUtility;

import java.text.ParseException;
import java.util.*;

public class ModuleUtility {

    public static void validateLocalModuleData(LocalModuleData localModuleData) throws InvalidModuleException {
        if (localModuleData.getId() == null || localModuleData.getId().replaceAll("\\s","").isEmpty()) {
            throw new InvalidModuleException("No valid module id found");
        }
    }

    public static void fillDefaults(LocalModuleData localModuleData) {
        if (localModuleData.getName() == null || localModuleData.getName().replaceAll("\\s","").isEmpty()) {
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

    public static DependencyReport checkDependencies(Map<String, LocalModule> modules) {
        Map<String, VersionedModule> versionedModules = new HashMap<>(modules);
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
                                + dependencyData.getVersionConstrain() + "'" );
                        foundIssue = true;
                        versionedModule.remove();
                        break;
                    }
                    if (dependencyData.getVersionConstrain() == null || dependencyData.getVersionConstrain().isEmpty()) continue;
                    try {
                        if (!VersionRangeUtility.isInVersionRange(dependency.getVersionData().getVersion(), dependencyData.getVersionConstrain())) {
                            if (dependencyData.isSoftDependency()) {
                                continue;
                            }
                            log.add("'" + currentModule.getId() + "' asks for '"
                                    + dependency.getId() + "' with the version constrain '"
                                    + dependencyData.getVersionConstrain() + "'");
                            log.add("   The installed version is '" + dependency.getVersionData().getVersion().toString() + "'" );
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
        return new DependencyReport(modules, versionedModules, log);
    }

    public static class DependencyReport {
        private List<String> log;
        private List<String> removedModules = new ArrayList<>();

        public DependencyReport(Map<String, LocalModule> previous, Map<String, VersionedModule> afterwards, List<String> log) {
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

        public boolean isOk() {
            return removedModules.isEmpty();
        }
    }
}
