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

package me.nikl.gamebox.module.data;

import me.nikl.gamebox.utilities.versioning.SemanticVersion;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * @author Niklas Eicker
 */
public class CloudModuleData implements ModuleBasicData, Serializable {
    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("authors")
    @Expose
    private List<String> authors;

    @SerializedName("subCommands")
    @Expose
    private List<String> subCommands;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("description")
    @Expose
    private String description;

    @SerializedName("sourceUrl")
    @Expose
    private String sourceUrl;

    @SerializedName("latestVersion")
    @Expose
    private SemanticVersion latestVersion;

    @SerializedName("lastUpdateAt")
    @Expose
    private Long lastUpdateAt;

    @SerializedName("versions")
    @Expose
    private List<VersionData> versions = null;

    private final static long serialVersionUID = 4719087577866667965L;

    public CloudModuleData() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CloudModuleData withId(String id) {
        this.id = id;
        return this;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public CloudModuleData withAuthors(List<String> authors) {
        this.authors = authors;
        return this;
    }

    public List<String> getSubCommands() {
        return subCommands;
    }

    public void setSubCommands(List<String> subCommands) {
        this.subCommands = subCommands;
    }

    public CloudModuleData withSubCommands(List<String> subCommands) {
        this.subCommands = subCommands;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CloudModuleData withName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CloudModuleData withDescription(String description) {
        this.description = description;
        return this;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public CloudModuleData withSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
        return this;
    }

    public Long getLastUpdateAt() {
        return lastUpdateAt;
    }

    public void setLastUpdateAt(Long lastUpdateAt) {
        this.lastUpdateAt = lastUpdateAt;
    }

    public CloudModuleData withLastUpdateAt(Long lastUpdateAt) {
        this.lastUpdateAt = lastUpdateAt;
        return this;
    }

    public List<VersionData> getVersions() {
        return versions;
    }

    public void setVersions(List<VersionData> versions) {
        this.versions = versions;
    }

    public CloudModuleData withVersions(List<VersionData> versions) {
        this.versions = versions;
        return this;
    }

    public SemanticVersion getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(SemanticVersion latestVersion) {
        this.latestVersion = latestVersion;
    }

    public CloudModuleData withLatestVersion(SemanticVersion latestVersion) {
        this.latestVersion = latestVersion;
        return this;
    }
}
