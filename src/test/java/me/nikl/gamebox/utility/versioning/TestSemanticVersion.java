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

package me.nikl.gamebox.utility.versioning;

import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class TestSemanticVersion {

    @Test
    public void testEquality() throws ParseException {
        SemanticVersion v1 = new SemanticVersion("1.2.3-alpha.1+build.2");
        SemanticVersion v2 = new SemanticVersion("1.2.3-alpha.1+build.2");
        assertEquals(v1, v2);
    }

    @Test
    public void testWontParse() {
        assertThrows(ParseException.class, () -> new SemanticVersion("won't parse"));
    }

    @Test
    public void testBroken() {
        assertThrows(ParseException.class, () -> new SemanticVersion("1..2"));
    }

    @Test
    public void testShort() {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new SemanticVersion("1.2"));
    }

    @Test
    public void testIdentifiers() {
        String[] ids = {
                "ok", "not_ok"
        };
        assertThrows(IllegalArgumentException.class, () -> new SemanticVersion(0, 0, 0, ids, ids));
    }

    @Test
    public void testOutOfBounds() {
        assertThrows(IllegalArgumentException.class, () -> new SemanticVersion(-1, 0, 0));
    }

    @Test
    public void testParsePlain() throws ParseException {
        final SemanticVersion v = new SemanticVersion("1.2.3");
        assertAll(
                () -> assertEquals(1, v.major),
                () -> assertEquals(2, v.minor),
                () -> assertEquals(3, v.patch),
                () -> assertEquals("1.2.3", v.toString())
        );

        final SemanticVersion v2 = new SemanticVersion("11.22.33");
        assertAll(
                () -> assertEquals(11, v2.major),
                () -> assertEquals(22, v2.minor),
                () -> assertEquals(33, v2.patch),
                () -> assertEquals("11.22.33", v2.toString())
        );
    }

    @Test
    public void testParseRelease() throws ParseException {
        SemanticVersion v = new SemanticVersion("1.2.3-alpha.1");
        assertEquals(1, v.major);
        assertEquals(2, v.minor);
        assertEquals(3, v.patch);
        assertEquals("alpha", v.preRelase[0]);
        assertEquals("1.2.3-alpha.1", v.toString());
    }

    @Test
    public void testParseMeta() throws ParseException {
        SemanticVersion v = new SemanticVersion("1.2.3+build.1");
        assertEquals(1, v.major);
        assertEquals(2, v.minor);
        assertEquals(3, v.patch);
        assertEquals("build", v.buildMeta[0]);
        assertEquals("1.2.3+build.1", v.toString());
    }

    @Test
    public void testParseReleaseMeta() throws ParseException {
        SemanticVersion v = new SemanticVersion("1.2.3-alpha.1+build.1");
        assertEquals(1, v.major);
        assertEquals(2, v.minor);
        assertEquals(3, v.patch);
        assertEquals("alpha", v.preRelase[0]);
        assertEquals("build", v.buildMeta[0]);
        assertEquals("1.2.3-alpha.1+build.1", v.toString());
    }

    @Test
    public void testNewer() {
        SemanticVersion[] inorder = {
                new SemanticVersion(0, 1, 4), new SemanticVersion(1, 1, 1),
                new SemanticVersion(1, 2, 1), new SemanticVersion(1, 2, 3)
        };

        SemanticVersion[] wrongorder = {
                inorder[0], inorder[3], inorder[1], inorder[2]
        };

        Arrays.sort(wrongorder);
        assertArrayEquals(inorder, wrongorder);
    }

    @Test
    public void testUpdate() {
        assertTrue(new SemanticVersion(1, 1, 1).isUpdateFor(new SemanticVersion(1,
                1, 0)));
        assertFalse(new SemanticVersion(1, 1, 1).isUpdateFor(new SemanticVersion(1,
                1, 2)));
        assertFalse(new SemanticVersion(1, 1, 1).isUpdateFor(new SemanticVersion(1,
                1, 1)));

        assertTrue(new SemanticVersion(1, 1, 2)
                .isCompatibleUpdateFor(new SemanticVersion(1, 1, 1)));
        assertFalse(new SemanticVersion(2, 1, 1)
                .isCompatibleUpdateFor(new SemanticVersion(1, 1, 0)));
    }

    @Test
    public void testPreRelease() throws ParseException {
        assertTrue(new SemanticVersion(1, 1, 1).isUpdateFor(new SemanticVersion(
                "1.1.1-alpha")));
        assertFalse(new SemanticVersion("1.1.1-alpha")
                .isUpdateFor(new SemanticVersion("1.1.1-alpha")));
        assertFalse(new SemanticVersion("1.1.1-alpha.1")
                .isUpdateFor(new SemanticVersion("1.1.1-alpha.1.1")));
        assertTrue(new SemanticVersion("1.1.1-alpha.1.one")
                .isUpdateFor(new SemanticVersion("1.1.1-alpha.1.1")));
        assertTrue(new SemanticVersion("1.1.1-alpha.1.one")
                .isUpdateFor(new SemanticVersion("1.1.1-alpha.1.1.1")));
    }
}
