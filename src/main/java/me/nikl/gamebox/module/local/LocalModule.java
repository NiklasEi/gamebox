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

package me.nikl.gamebox.module.local;

import me.nikl.gamebox.exceptions.module.InvalidModuleException;
import me.nikl.gamebox.module.NewGameBoxModule;
import me.nikl.gamebox.module.data.*;
import me.nikl.gamebox.utilities.FileUtility;
import me.nikl.gamebox.utilities.GameBoxYmlBuilder;
import me.nikl.gamebox.utilities.ModuleUtility;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * @author Niklas Eicker
 */
public class LocalModule extends VersionedModule {
    private static final Yaml YAML = GameBoxYmlBuilder.buildLocalModuleDataYml();
    private String moduleId;
    private String name;
    private String description;
    private String sourceUrl;
    private List<String> authors;
    private List<String> subCommands;
    private VersionData versionData;
    private File moduleJar;

    // These sets are filled with dependencies and dependent modules during loading
    //    soft dependencies will be listed if they are loaded
    private Set<String> childModules = new HashSet<>();
    private Set<String> parentModules = new HashSet<>();

    public LocalModule(LocalModuleData localModuleData) {
        this.moduleId = localModuleData.getId();
        this.name = localModuleData.getName();
        this.description = localModuleData.getDescription();
        this.sourceUrl = localModuleData.getSourceUrl();
        this.authors = localModuleData.getAuthors();
        this.versionData = localModuleData.getVersionData();
        this.subCommands = localModuleData.getSubCommands();
    }

    public static LocalModule fromJar(File jar) throws InvalidModuleException {
        JarFile jarFile;
        LocalModule localModule = null;
        try {
            jarFile = new JarFile(jar);
            ZipEntry moduleYml = jarFile.getEntry("module.yml");
            if (moduleYml == null) {
                throw new InvalidModuleException("No 'module.yml' found for " + jar.getName());
            }
            InputStream moduleFile = jarFile.getInputStream(moduleYml);
            LocalModuleData moduleData = YAML.loadAs(new InputStreamReader(moduleFile), LocalModuleData.class);
            ModuleUtility.validateLocalModuleData(moduleData);
            ModuleUtility.fillDefaults(moduleData);
            jarFile.close();
            localModule = new LocalModule(moduleData);
            localModule.setModuleJar(jar);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return localModule;
    }

    @Override
    public VersionData getVersionData() {
        return this.versionData;
    }

    public File getModuleJar() {
        return moduleJar;
    }

    public void setModuleJar(File moduleJar) throws InvalidModuleException {
        List<Class<?>> clazzes = FileUtility.getClassesFromJar(moduleJar, NewGameBoxModule.class);
        if (clazzes.size() < 1) throw new InvalidModuleException("No class extending GameBoxModule was found in '" + getName() + "'");
        if (clazzes.size() > 1) throw new InvalidModuleException("More then one class extending GameBoxModule was found in '" + getName() + "'");
        this.moduleJar = moduleJar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getSourceUrl() {
        return this.sourceUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getId() {
        return this.moduleId;
    }

    @Override
    public List<String> getSubCommands() {
        return subCommands;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public boolean isChildModule(String moduleId) {
        return this.childModules.contains(moduleId);
    }

    public boolean isParentModule(String moduleId) {
        return this.parentModules.contains(moduleId);
    }

    public void addChildModule(String moduleId) {
        this.childModules.add(moduleId);
    }

    public void addParentModule(String moduleId) {
        this.parentModules.add(moduleId);
    }

    public void removeChildModule(String moduleId) {
        this.childModules.remove(moduleId);
    }

    public void removeParentModule(String moduleId) {
        this.parentModules.remove(moduleId);
    }

    public Set<String> getChildModules() {
        return Collections.unmodifiableSet(this.childModules);
    }

    public Set<String> getParentModules() {
        return Collections.unmodifiableSet(this.parentModules);
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    @Override
    public boolean equals(Object compareTo) {
        if (!(compareTo instanceof LocalModule)) return false;
        LocalModule compareToModule = (LocalModule) compareTo;
        return compareToModule.getId().equals(this.getId()) && compareToModule.getVersionData().getVersion().equals(this.getVersionData().getVersion());
    }
}
