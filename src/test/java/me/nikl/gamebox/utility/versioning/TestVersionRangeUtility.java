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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Niklas Eicker
 */
public class TestVersionRangeUtility {

    @Test
    @DisplayName("It should throw IllegalArgumentException if the range contains a plus")
    public void testInvalidPlusOperator() {
        assertThrows(IllegalArgumentException.class, () -> VersionRangeUtility.isInVersionRange(new SemanticVersion("1.0.0"), "+ 2.1.3"));
    }

    @Test
    @DisplayName("It should throw IllegalArgumentException if the range contains a minus")
    public void testInvalidMinusOperator() {
        assertThrows(IllegalArgumentException.class, () -> VersionRangeUtility.isInVersionRange(new SemanticVersion("1.0.0"), "- 2.1.3"));
    }

    @Test
    @DisplayName("It should correctly interpret the < operator")
    public void testSmaller() {
        assertAll(
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.4.2"), "< 2.")),
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.4.2"), "< 1.4.3")),
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.6.2"), "< 1.7")),
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("2.1.9"), "< 2.1.10")),
                () -> assertFalse(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.4.2"), "< 1")),
                () -> assertFalse(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.4.4"), "< 1.4.3")),
                () -> assertFalse(VersionRangeUtility.isInVersionRange(new SemanticVersion("2.1.3"), "< 2.1.3"))
        );
    }

    @Test
    @DisplayName("It should correctly interpret the <= operator")
    public void testSmallerOrEqual() {
        assertAll(
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.4.2"), "<= 2.")),
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.4.2"), "<= 1.4.3")),
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.6.2"), "<= 1.7")),
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("2.1.9"), "<= 2.1.10")),
                () -> assertFalse(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.4.2"), "<= 1")),
                () -> assertFalse(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.4.4"), "<= 1.4.3")),

                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("2.1.3"), "<= 2.1.3")),
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.0.0"), "<= 1")),
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.0.0"), "<= 1.0"))
        );
    }

    @Test
    @DisplayName("It should correctly interpret the > operator")
    public void testLarger() {
        assertAll(
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.4.2"), "> 1")),
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.4.2"), "> 1.0")),
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.4.2"), "> 1.4")),
                () -> assertFalse(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.3.2"), "> 1.4")),
                () -> assertFalse(VersionRangeUtility.isInVersionRange(new SemanticVersion("2.1.3"), "> 2.1.3"))
        );
    }

    @Test
    @DisplayName("It should correctly interpret the >= operator")
    public void testLargerOrEqual() {
        assertAll(
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.4.2"), ">= 1")),
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.4.2"), ">= 1.0")),
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.4.2"), ">= 1.4")),
                () -> assertFalse(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.3.2"), ">= 1.4")),

                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("2.1.3"), ">= 2.1.3"))
        );
    }

    @Test
    @DisplayName("It should correctly interpret the ~> (Twiddle wakka) operator")
    public void testTwiddleWakka() {
        assertAll(
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.4.2"), "~> 1")),
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.4.2"), "~> 1.")),
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.4.2"), "~> 1.4")),
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.6.2"), "~> 1.4")),
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("2.1.3"), "~> 2.1.3")),
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("2.1.9"), "~> 2.1.3")),

                () -> assertFalse(VersionRangeUtility.isInVersionRange(new SemanticVersion("2.0.0"), "~> 1")),
                () -> assertFalse(VersionRangeUtility.isInVersionRange(new SemanticVersion("2.0.0-1.3.7"), "~> 1")),
                () -> assertFalse(VersionRangeUtility.isInVersionRange(new SemanticVersion("2.0.0+build.4.258884"), "~> 1")),
                () -> assertFalse(VersionRangeUtility.isInVersionRange(new SemanticVersion("2.0.0-alpha.1+build.4.258884"), "~> 1")),
                () -> assertFalse(VersionRangeUtility.isInVersionRange(new SemanticVersion("2.1.2"), "~> 2.1.3")),
                () -> assertFalse(VersionRangeUtility.isInVersionRange(new SemanticVersion("2.2.3"), "~> 2.1.3"))
        );
    }

    @Test
    @DisplayName("It should correctly interpret multiple constraints")
    public void testMultipleConstrains() throws ParseException {
        assertAll(
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.4.2"), "    > 1  .,        < 2")),
                () -> assertTrue(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.3.5"), "~> 1., >= 1.3.4")),
                () -> assertFalse(VersionRangeUtility.isInVersionRange(new SemanticVersion("1.3.5"), "~> 1., >= 1.3.6"))
        );
    }
}
