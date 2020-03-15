package me.nikl.gamebox.cloud.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import me.nikl.gamebox.utilities.versioning.SemanticVersion;

import java.io.Serializable;
import java.util.List;

/**
 * @author Niklas Eicker
 */
public class VersionData implements Serializable {
    @SerializedName("version")
    @Expose
    private SemanticVersion version;

    @SerializedName("updatedAt")
    @Expose
    private Long updatedAt;

    @SerializedName("dependencies")
    @Expose
    private List<DependencyData> dependencies = null;

    @SerializedName("releaseNotes")
    @Expose
    private List<String> releaseNotes = null;

    private final static long serialVersionUID = -2433806999627043447L;

    public VersionData() {
    }

    public VersionData(SemanticVersion version, List<DependencyData> dependencies, List<String> releaseNotes) {
        this.version = version;
        this.dependencies = dependencies;
        this.releaseNotes = releaseNotes;
    }

    public SemanticVersion getVersion() {
        return version;
    }

    public void setVersion(SemanticVersion version) {
        this.version = version;
    }

    public VersionData withVersion(SemanticVersion version) {
        this.version = version;
        return this;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public VersionData withUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public List<DependencyData> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<DependencyData> dependencies) {
        this.dependencies = dependencies;
    }

    public VersionData withDependencies(List<DependencyData> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public List<String> getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(List<String> releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    public VersionData withReleaseNotes(List<String> releaseNotes) {
        this.releaseNotes = releaseNotes;
        return this;
    }
}
