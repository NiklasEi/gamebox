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

package me.nikl.gamebox.utility.versioning;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.ParseException;

/**
 * @author Niklas Eicker
 */
public class SemanticVersionAdapter extends TypeAdapter<SemanticVersion> {
  @Override
  public void write(JsonWriter jsonWriter, SemanticVersion semanticVersion) throws IOException {
    jsonWriter.value(semanticVersion.toString());
  }

  @Override
  public SemanticVersion read(JsonReader jsonReader) throws IOException {
    String version = jsonReader.nextString();
    try {
      return new SemanticVersion(version);
    } catch (ParseException | ArrayIndexOutOfBoundsException e) {
      System.out.print("Failed to parse '" + version + "' to SemanticVersion");
      e.printStackTrace();
      return null;
    }
  }
}
