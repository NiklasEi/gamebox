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

import me.nikl.gamebox.exceptions.module.InvalidModuleException;
import me.nikl.gamebox.module.local.LocalModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestModuleUtility {
    private static final Map<String, LocalModule> modules = new HashMap<>();
    private static final Yaml YAML = GameBoxYmlBuilder.buildLocalModuleDataYml();

    @BeforeAll
    public static void prepare() {
        for (int i = 1; i < 6; i++) {
            try {
                LocalModule module = LocalModule.fromJar(new File("src/test/resources/module/local/test_local_module_" + i + ".jar"));
                modules.put(module.getId(), module);
            } catch (InvalidModuleException e) {
                e.printStackTrace();
            }
        }
    }

    @Disabled("Needs injection and preparation of gamebox module info")
    @Test
    @DisplayName("Check dependent modules - no missing dependencies")
    public void checkDependencies() {
        Map<String, LocalModule> modules = getModules();
        ModuleUtility.DependencyReport report = ModuleUtility.checkDependencies(new HashMap<>(modules));
        assertEquals(0, report.getRemovedModules().size());
    }

    @Disabled("Needs injection and preparation of gamebox module info")
    @Test
    @DisplayName("Check dependent modules - missing soft dependency")
    public void checkSoftDependencies() {
        Map<String, LocalModule> modules = new HashMap<>(getModules());
        modules.remove("soft-lib-test-module");
        ModuleUtility.DependencyReport report = ModuleUtility.checkDependencies(new HashMap<>(modules));
        assertEquals(0, report.getRemovedModules().size());
    }

    @Disabled("Needs injection and preparation of gamebox module info")
    @Test
    @DisplayName("Check dependent modules - missing dependency")
    public void checkMissingDependencies() {
        Map<String, LocalModule> modules = new HashMap<>(getModules());
        modules.remove("lib-test-module");
        ModuleUtility.DependencyReport report = ModuleUtility.checkDependencies(new HashMap<>(modules));
        assertEquals(1, report.getRemovedModules().size());
        assertTrue(report.getRemovedModules().contains("test-module"));
    }

    private Map<String, LocalModule> getModules() {
        return Collections.unmodifiableMap(modules);
    }
}
