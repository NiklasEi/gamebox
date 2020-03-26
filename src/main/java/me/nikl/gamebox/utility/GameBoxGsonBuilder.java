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

package me.nikl.gamebox.utility;

import me.nikl.gamebox.utility.versioning.SemanticVersion;
import me.nikl.gamebox.utility.versioning.SemanticVersionAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Niklas Eicker
 */
public class GameBoxGsonBuilder {
  public static Gson build() {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(SemanticVersion.class, new SemanticVersionAdapter());
    return builder.create();
  }
}
