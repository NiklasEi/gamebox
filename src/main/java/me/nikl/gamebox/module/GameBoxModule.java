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
import me.nikl.gamebox.module.local.LocalModule;

import java.io.File;

/**
 * Module base class
 *
 * @author Niklas Eicker
 */
public abstract class GameBoxModule {
    private LocalModule moduleData;
    private GameBox gameBox;
    private File languageFolder;
    private File moduleFolder;

    protected GameBoxModule() {}

    public abstract void onEnable();

    public abstract void onDisable();

    public GameBox getGameBox() {
        return this.gameBox;
    }

    public LocalModule getModuleData() {
        return this.moduleData;
    }

    public String getIdentifier() {
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
}
