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

package me.nikl.gamebox.exceptions.module;

import me.nikl.gamebox.exceptions.GameBoxException;

/**
 * Exception for issues concerning the module cloud
 */
public class GameBoxCloudException extends GameBoxException {
    private static final long serialVersionUID = 1L;

    public GameBoxCloudException(String message) {
        super(message);
    }

    public GameBoxCloudException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public GameBoxCloudException() {
        super("There appears to be a problem with the module cloud");
    }

    public GameBoxCloudException(Throwable throwable) {
        super("There appears to be a problem with the module cloud", throwable);
    }
}
