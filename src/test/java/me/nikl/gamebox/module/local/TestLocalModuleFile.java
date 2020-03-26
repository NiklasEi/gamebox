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

package me.nikl.gamebox.module.local;

import me.nikl.gamebox.module.data.DependencyData;
import me.nikl.gamebox.utility.GameBoxYmlBuilder;
import me.nikl.gamebox.utility.versioning.SemanticVersion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestLocalModuleFile {
    private static File testLocalModuleFile;
    private static LocalModuleData manualLocalModuleData;
    private static final Yaml YAML = GameBoxYmlBuilder.buildLocalModuleDataYml();

    @BeforeAll
    public static void prepare() {
        testLocalModuleFile = new File("src/test/resources/module/local/test_local_module_1.yml");
        manuallyBuildTestModule();
    }

    private static void manuallyBuildTestModule() {
        manualLocalModuleData = new LocalModuleData()
                .withId("test-module")
                .withName("Test module")
                .withAuthors(Arrays.asList("Nikl"))
                .withDescription("This module is only for test purposes")
                .withSourceUrl("https://github.com/hygames-team/gamebox-test-module")
                .withVersion(new SemanticVersion(1, 0, 0))
                .withDependencies(Arrays.asList(
                        new DependencyData()
                                .withId("gamebox")
                                .withVersionConstrain("~> 1.0"),
                        new DependencyData()
                                .withId("lib-test-module")
                                .withVersionConstrain("~> 1.0, > 1.2")
                        )
                );
    }

    @Test
    @DisplayName("Comparing parsed and manually loaded Module")
    public void parseTestLocalModule() throws FileNotFoundException {
        LocalModuleData localModuleData = YAML.loadAs(new FileReader(testLocalModuleFile), LocalModuleData.class);
        assertEquals(localModuleData.getId(), manualLocalModuleData.getId(), "IDs do not match");
        assertEquals(localModuleData.getName(), manualLocalModuleData.getName(), "Names do not match");
        assertArrayEquals(localModuleData.getAuthors().toArray(new String[]{})
                , manualLocalModuleData.getAuthors().toArray(new String[]{}), "Authors do not match");
        assertEquals(localModuleData.getDescription(), manualLocalModuleData.getDescription(), "Descriptions do not match");
        assertEquals(localModuleData.getSourceUrl(), manualLocalModuleData.getSourceUrl(), "Source urls do not match");
        assertEquals(localModuleData.getVersion(), manualLocalModuleData.getVersion(), "Versions do not match");
        assertArrayEquals(localModuleData.getDependencies().toArray(new DependencyData[]{})
                , manualLocalModuleData.getDependencies().toArray(new DependencyData[]{})
                , "Dependencies do not match");
    }
}
