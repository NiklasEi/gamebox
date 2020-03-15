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

package me.nikl.gamebox.module.cloud;

import me.nikl.gamebox.exceptions.module.GameBoxCloudException;
import me.nikl.gamebox.module.data.CloudModuleData;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;

public class CloudFacade {
    //private static final String API_BASE_URL = "https://api.hygames.co/gamebox/";
    private static final String API_BASE_URL = "http://127.0.0.1:4000/gamebox/";
    private static final Gson GSON = new Gson();

    public ApiResponse<CloudModuleData[]> getCloudModuleData() {
        try {
            CloudModuleData[] modulesData = GSON.fromJson(new InputStreamReader(new URL(API_BASE_URL + "modules").openStream()), CloudModuleData[].class);
            return new ApiResponse<>(modulesData, null);
        } catch (UnknownHostException e) {
            return new ApiResponse<>(null, new GameBoxCloudException("Connection problem to the cloud. Please make sure that you are connected to the internet.", e));
        } catch (IOException e) {
            return new ApiResponse<>(null, new GameBoxCloudException(e));
        }
    }
}

