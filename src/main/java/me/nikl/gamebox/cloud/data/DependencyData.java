package me.nikl.gamebox.cloud.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author Niklas Eicker
 */
public class DependencyData implements Serializable {
    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("versionRange")
    @Expose
    private String versionRange;

    private final static long serialVersionUID = 3080774369300795773L;

    public DependencyData() {
    }

    public DependencyData(String id, String versionRange) {
        super();
        this.id = id;
        this.versionRange = versionRange;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DependencyData withId(String id) {
        this.id = id;
        return this;
    }

    public String getVersionRange() {
        return versionRange;
    }

    public void setVersionRange(String versionRange) {
        this.versionRange = versionRange;
    }

    public DependencyData withVersionRange(String versionRange) {
        this.versionRange = versionRange;
        return this;
    }
}
