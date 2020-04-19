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

package me.nikl.gamebox.module.settings;

import me.nikl.gamebox.GameBoxSettings;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ModulesSettings implements Serializable  {
    private final static long serialVersionUID = 8241484990220433533L;
    public Map<String, ModuleSettings> modules;

    public Map<String, ModuleSettings> getModules() {
        return modules == null ? new HashMap<>() : modules;
    }

    public ModuleSettings getModuleSettings(String moduleId) {
        return this.modules.getOrDefault(moduleId, new ModuleSettings());
    }

    public void setModules(Map<String, ModuleSettings> modules) {
        this.modules = modules;
    }

    public static class ModuleSettings implements Serializable {
        private final static long serialVersionUID = 8241484590220433533L;
        private boolean enabled = true;
        private boolean autoUpdate = GameBoxSettings.enableUpdateForNewModules;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isAutoUpdate() {
            return autoUpdate;
        }

        public void setAutoUpdate(boolean autoUpdate) {
            this.autoUpdate = autoUpdate;
        }
    }
}
