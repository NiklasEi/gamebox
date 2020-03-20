package me.nikl.gamebox.module.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import me.nikl.gamebox.utility.versioning.SemanticVersion;

import java.util.List;

public class VersionedCloudModule extends VersionData implements ModuleBasicData {
    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("authors")
    @Expose
    private List<String> authors;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("description")
    @Expose
    private String description;

    @SerializedName("sourceUrl")
    @Expose
    private String sourceUrl;

    public VersionedCloudModule() {

    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public VersionedCloudModule withId(String id) {
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

    public VersionedCloudModule withAuthors(List<String> authors) {
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

    public VersionedCloudModule withName(String name) {
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

    public VersionedCloudModule withDescription(String description) {
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

    public VersionedCloudModule withSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
        return this;
    }

    @Override
    public VersionedCloudModule withUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    @Override
    public VersionedCloudModule withDependencies(List<DependencyData> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    @Override
    public VersionedCloudModule withReleaseNotes(List<String> releaseNotes) {
        this.releaseNotes = releaseNotes;
        return this;
    }

    @Override
    public VersionedCloudModule withVersion(SemanticVersion version) {
        this.version = version;
        return this;
    }

    @Override
    public VersionedCloudModule withDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        return this;
    }
}
