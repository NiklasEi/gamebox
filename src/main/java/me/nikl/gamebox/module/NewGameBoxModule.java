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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.nikl.gamebox.module;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.game.GameLanguage;
import me.nikl.gamebox.module.local.LocalModule;

import java.io.File;
import java.util.List;

/**
 * Module base class
 *
 * @author Niklas Eicker
 */
public abstract class NewGameBoxModule {
    private LocalModule moduleData;
    private GameBox gameBox;
    private File languageFolder;
    private File moduleFolder;
    protected GameLanguage moduleLanguage;

    protected NewGameBoxModule() {}

    public abstract void onEnable();

    public abstract void onDisable();

    public File getModuleFolder() {
        if (moduleFolder != null) return moduleFolder;
        moduleFolder = new File(gameBox.getModulesManager().getModulesDir(), getModuleID());
        if (!moduleFolder.isDirectory()) moduleFolder.mkdirs();
        return moduleFolder;
    }

    public File getLanguageFolder() {
        if (languageFolder != null) return languageFolder;
        languageFolder = new File(gameBox.getLanguageDir(), getModuleID());
        if (!languageFolder.isDirectory()) languageFolder.mkdirs();
        return languageFolder;
    }

    public GameBox getGameBox() {
        return this.gameBox;
    }

    public LocalModule getModuleData() {
        return this.moduleData;
    }

    public String getModuleID() {
        return this.moduleData.getId();
    }

    void setGameBox(GameBox gameBox) throws UnsupportedOperationException {
        if (this.gameBox != null) throw new UnsupportedOperationException("Cannot change the GameBox instance in a module");
        this.gameBox = gameBox;
    }

    void setModuleData(LocalModule moduleData) throws UnsupportedOperationException {
        if (this.moduleData != null) throw new UnsupportedOperationException("Cannot change the module data");
        this.moduleData = moduleData;
    }

    public List<String> getSubCommands() {
        return moduleData.getSubCommands();
    }

    @Override
    public boolean equals(Object module) {
        if (!(module instanceof NewGameBoxModule)) {
            return false;
        }
        return getModuleID().equalsIgnoreCase(((NewGameBoxModule) module).getModuleID());
    }

    @Override
    public int hashCode() {
        return getModuleID().hashCode();
    }
}
