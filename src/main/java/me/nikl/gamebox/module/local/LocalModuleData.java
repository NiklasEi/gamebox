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

import me.nikl.gamebox.module.data.*;
import me.nikl.gamebox.utility.versioning.SemanticVersion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LocalModuleData implements ModuleBasicData, VersionedModuleData, ModuleWithVersionData, Serializable {
    private String id;
    private List<String> authors = new ArrayList<>();
    private String name;
    private String description;
    private String sourceUrl;
    private Long updatedAt;
    private List<String> releaseNotes = new ArrayList<>();
    private SemanticVersion version;
    private List<DependencyData> dependencies = new ArrayList<>();

    private final static long serialVersionUID = 8241484990221433533L;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalModuleData withId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public LocalModuleData withAuthors(List<String> authors) {
        this.authors = authors;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalModuleData withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalModuleData withDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public LocalModuleData withSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
        return this;
    }

    @Override
    public SemanticVersion getVersion() {
        return version;
    }

    public void setVersion(SemanticVersion version) {
        this.version = version;
    }

    public LocalModuleData withVersion(SemanticVersion version) {
        this.version = version;
        return this;
    }

    @Override
    public Long getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalModuleData withUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    @Override
    public List<DependencyData> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<DependencyData> dependencies) {
        this.dependencies = dependencies;
    }

    public LocalModuleData withDependencies(List<DependencyData> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    @Override
    public List<String> getReleaseNotes() {
        return this.releaseNotes;
    }

    @Override
    public VersionData getVersionData() {
        return new VersionData()
                .withUpdatedAt(getUpdatedAt())
                .withReleaseNotes(getReleaseNotes())
                .withDependencies(getDependencies())
                .withVersion(getVersion());
    }
}
