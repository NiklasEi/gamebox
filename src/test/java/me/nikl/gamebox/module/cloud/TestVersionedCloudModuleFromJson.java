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
import me.nikl.gamebox.module.data.DependencyData;
import me.nikl.gamebox.module.data.VersionData;
import me.nikl.gamebox.module.data.VersionedCloudModule;
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
public class TestVersionedCloudModuleFromJson {
    private static File testVersionedCloudModuleFile;
    private static VersionedCloudModule testVersionedCloudModule;

    @BeforeAll
    public static void prepare() {
        testVersionedCloudModuleFile = new File("src/test/resources/module/cloud/test_versioned_cloud_module.json");
        manuallyBuildTestModule();
    }

    private static void manuallyBuildTestModule() {
        DependencyData dependency = new DependencyData().withId("gamebox").withVersionConstrain(">= 3.0.0-beta.2");
        List<DependencyData> dependencies = new ArrayList<>();
        dependencies.add(dependency);
        List<VersionData> versions = new ArrayList<>();
        versions.add(new VersionData()
        );
        String[] pre = new String[1];
        pre[0] = "beta";
        testVersionedCloudModule = new VersionedCloudModule()
                .withId("cookieclicker")
                .withAuthors(Collections.singletonList("Nikl"))
                .withName("CookieClicker")
                .withDescription("The super addictive backing game")
                .withSourceUrl("https://github.com/NiklasEi/cookieclicker-for-gamebox")
                .withVersion(new SemanticVersion(3, 0, 0, pre, new String[0]))
                .withDependencies(dependencies)
                .withUpdatedAt(1584707798040L)
                .withReleaseNotes(Collections.singletonList("This is the first release for GameBox v3 and is compatible with MC 1.14+"))
                .withDownloadUrl("http://example.com/download/test-module@1.0.0.jar");
    }

    @Test
    @DisplayName("correctly parse VersionedCloudModule from a json file")
    public void parseTestCloudModule() throws FileNotFoundException {
        Gson gson = GameBoxGsonBuilder.build();
        VersionedCloudModule fileModule = gson.fromJson(new FileReader(testVersionedCloudModuleFile), VersionedCloudModule.class);
        assertAll(
                () -> assertEquals(fileModule.getId(), testVersionedCloudModule.getId(),"Not the same id"),
                () -> assertArrayEquals(fileModule.getAuthors().toArray(), testVersionedCloudModule.getAuthors().toArray(),"Not the same authors"),
                () -> assertEquals(fileModule.getName(), testVersionedCloudModule.getName(),"Not the same name"),
                () -> assertEquals(fileModule.getDescription(), testVersionedCloudModule.getDescription(),"Not the same description"),
                () -> assertEquals(fileModule.getSourceUrl(), testVersionedCloudModule.getSourceUrl(),"Not the same source URL"),
                () -> assertEquals(fileModule.getVersion(), testVersionedCloudModule.getVersion(),"Not the same version"),
                () -> assertEquals(fileModule.getUpdatedAt(), testVersionedCloudModule.getUpdatedAt(),"Not the same last updated timestamp"),
                () -> assertIterableEquals(fileModule.getReleaseNotes(), testVersionedCloudModule.getReleaseNotes(), "Not the same release notes"),
                () -> assertEquals(fileModule.getDependencies().size(), testVersionedCloudModule.getDependencies().size(),"Not the same number of dependencies"),
                () -> assertEquals(fileModule.getDownloadUrl(), testVersionedCloudModule.getDownloadUrl(),"Not the same downloadUrl")
        );

        Iterator<DependencyData> itManualDependency = fileModule.getDependencies().iterator();
        Iterator<DependencyData> itFileDependency = testVersionedCloudModule.getDependencies().iterator();
        while (itManualDependency.hasNext() && itFileDependency.hasNext()) {
            DependencyData dependency1 = itManualDependency.next();
            DependencyData dependency2 = itFileDependency.next();
            assertEquals(dependency1.getId(), dependency2.getId(),"Dependencies: Not the same id");
            assertEquals(dependency1.getVersionConstrain(), dependency2.getVersionConstrain(),"Dependencies: Not the same version constrain");
        }
    }
}
