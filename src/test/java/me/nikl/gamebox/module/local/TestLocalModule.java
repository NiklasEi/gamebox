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

import me.nikl.gamebox.exceptions.module.InvalidModuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestLocalModule {

    @Test
    @DisplayName("Test loading module from jar file without module.yml")
    public void testModuleFromInvalidFile() {
        File invalidJarFile = new File("src/test/resources/module/local/test_local_module_no_module-yml.jar");
        assertThrows(InvalidModuleException.class, () -> LocalModule.fromJar(invalidJarFile));
    }
}
