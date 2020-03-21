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

package me.nikl.gamebox.module.cloud;

import com.google.gson.Gson;
import me.nikl.gamebox.module.data.CloudModuleData;
import me.nikl.gamebox.utility.GameBoxGsonBuilder;
import me.nikl.gamebox.utility.versioning.SemanticVersion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Niklas Eicker
 */
public class TestCloudModuleDataFromJson {
    private static File testCloudModuleFile;
    private static CloudModuleData[] testCloudModules;

    @BeforeAll
    public static void prepare() {
        testCloudModuleFile = new File("src/test/resources/module/cloud/test_cloud_modules.json");
        manuallyBuildTestModule();
    }

    private static void manuallyBuildTestModule() {
        CloudModuleData testModuleOne = new CloudModuleData()
                .withId("test-module")
                .withAuthors(Collections.singletonList("Nikl"))
                .withName("Test module")
                .withLastUpdateAt(1240L)
                .withDescription("This module is only for test purposes")
                .withLatestVersion(new SemanticVersion(1, 2, 0))
                .withSourceUrl("https://github.com/hygames-team/api-hygames-co")
        ;
        CloudModuleData testModuleTwo = new CloudModuleData()
                .withId("test-module2")
                .withAuthors(Collections.singletonList("Nikl"))
                .withName("Test module")
                .withLastUpdateAt(1239L)
                .withDescription("This module is only for test purposes")
                .withLatestVersion(new SemanticVersion(1, 1, 0))
                .withSourceUrl("https://github.com/hygames-team/api-hygames-co")
                ;
        testCloudModules = new CloudModuleData[2];
        testCloudModules[0] = testModuleOne;
        testCloudModules[1] = testModuleTwo;
    }

    @Test
    @DisplayName("correctly parse CloudModuleData from a json file")
    public void parseTestCloudModule() throws FileNotFoundException {
        Gson gson = GameBoxGsonBuilder.build();
        CloudModuleData[] fileModules = gson.fromJson(new FileReader(testCloudModuleFile), CloudModuleData[].class);
        compareModules(fileModules[0], testCloudModules[0]);
        compareModules(fileModules[1], testCloudModules[1]);
    }

    private void compareModules(CloudModuleData fileModule, CloudModuleData testCloudModule) {
        assertAll(
                () -> assertEquals(fileModule.getId(), testCloudModule.getId(), "Not the same id"),
                () -> assertArrayEquals(fileModule.getAuthors().toArray(), testCloudModule.getAuthors().toArray(), "Not the same authors"),
                () -> assertEquals(fileModule.getName(), testCloudModule.getName(), "Not the same name"),
                () -> assertEquals(fileModule.getDescription(), testCloudModule.getDescription(), "Not the same description"),
                () -> assertEquals(fileModule.getLatestVersion(), testCloudModule.getLatestVersion(), "Not the same latest version"),
                () -> assertEquals(fileModule.getSourceUrl(), testCloudModule.getSourceUrl(), "Not the same source url"),
                () -> assertEquals(fileModule.getUpdatedAt(), testCloudModule.getUpdatedAt(), "Not the same last updated timestamp")
        );
    }
}
