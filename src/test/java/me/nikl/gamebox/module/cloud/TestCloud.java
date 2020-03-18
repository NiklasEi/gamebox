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

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

// ToDo: mock API response and test the cloud manager
public class TestCloud {
    private static String modules;

    @BeforeAll
    public static void beforeClass() {
        try {
            modules = (new JSONParser()).parse(new FileReader(new File("src/test/resources/module/cloud/test_cloud_modules.json"))).toString();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void postSimpleBody() {
        //System.out.println(modules);
    }
}
