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

    @SerializedName("versionConstrain")
    @Expose
    private String versionConstrain;

    @SerializedName("softDependency")
    @Expose
    private boolean softDependency = false;

    private final static long serialVersionUID = 3080774369300795773L;

    public DependencyData() {
    }

    public DependencyData(String id, String versionConstrain) {
        super();
        this.id = id;
        this.versionConstrain = versionConstrain;
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

    public String getVersionConstrain() {
        return versionConstrain;
    }

    public void setVersionConstrain(String versionConstrain) {
        this.versionConstrain = versionConstrain;
    }

    public DependencyData withVersionConstrain(String versionConstrain) {
        this.versionConstrain = versionConstrain;
        return this;
    }

    public boolean isSoftDependency() {
        return softDependency;
    }

    public void setSoftDependency(boolean softDependency) {
        this.softDependency = softDependency;
    }

    public DependencyData withSoftDependency(boolean softDependency) {
        this.softDependency = softDependency;
        return this;
    }

    @Override
    public boolean equals(Object compareObj) {
        if (compareObj == this) return true;
        if (!(compareObj instanceof DependencyData)) return false;
        DependencyData compare = (DependencyData) compareObj;
        return this.id.equals(compare.id) && this.versionConstrain.equals(compare.versionConstrain)
                && this.softDependency == compare.softDependency;
    }
}
