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

import java.text.ParseException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionRangeUtility {
    private static final String CONSTRAIN_OPERATOR_EQUAL = "=";
    private static final String CONSTRAIN_OPERATOR_LARGER = ">";
    private static final String CONSTRAIN_OPERATOR_SMALLER = "<";
    private static final String CONSTRAIN_OPERATOR_EQUAL_OR_LARGER = ">=";
    private static final String CONSTRAIN_OPERATOR_EQUAL_OR_SMALLER = "<=";
    private static final String CONSTRAIN_OPERATOR_TWIDDLE_WAKKA = "~>";

    private static final Pattern OPERATOR_PATTERN = Pattern.compile("^[^0-9]+");
    private static final Pattern WHITE_SPACE = Pattern.compile("\\s+");

    public static boolean isInVersionRange(SemanticVersion version, String versionRange) throws ParseException {
        String[] constrains = versionRange.split(",");
        for (String constrain : constrains) {
            constrain = WHITE_SPACE.matcher(constrain).replaceAll("");
            String constrainedVersion;
            String constrainOperator;
            Matcher operatorMatcher = OPERATOR_PATTERN.matcher(constrain);
            constrainOperator = operatorMatcher.find()?operatorMatcher.group():"";
            constrainedVersion = constrain.replaceFirst(constrainOperator, "");
            if (OPERATOR_PATTERN.matcher(constrainedVersion).find()) {
                throw new IllegalArgumentException("The constrain '" + constrain + "' is not valid!");
            }
            if (constrainOperator.isEmpty()) {
                constrainOperator = CONSTRAIN_OPERATOR_EQUAL;
            }
            if (isInVersionRange(version, constrainedVersion, constrainOperator)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    public static boolean isInVersionRange(SemanticVersion version, String constrainedVersion, String constrainOperator) throws ParseException {
        // get rid of eventual pre-release and build metadata in both versions
        version = version.withoutMetaData();
        String fullConstrainedVersion = constrainedVersion.split("-")[0].split("\\+")[0];
        int versionNumbers = fullConstrainedVersion.split("\\.").length;
        if (versionNumbers != 3) {
            if (versionNumbers == 1) {
                fullConstrainedVersion = String.join(".", Arrays.asList(fullConstrainedVersion.split("\\.")[0], "0", "0"));
            } else if (versionNumbers == 2) {
                fullConstrainedVersion = String.join(".", Arrays.asList(fullConstrainedVersion.split("\\.")[0], fullConstrainedVersion.split("\\.")[1], "0"));
            } else {
                throw new IllegalArgumentException("Version '" + constrainedVersion + "' has an illegal number of dots");
            }
        }
        switch (constrainOperator) {
            case CONSTRAIN_OPERATOR_EQUAL:
                return version.equals(new SemanticVersion(fullConstrainedVersion));

            case CONSTRAIN_OPERATOR_LARGER:
                return version.isUpdateFor(new SemanticVersion(fullConstrainedVersion));

            case CONSTRAIN_OPERATOR_SMALLER:
                return version.compareTo(new SemanticVersion(fullConstrainedVersion)) < 0;

            case CONSTRAIN_OPERATOR_EQUAL_OR_LARGER:
                return version.compareTo(new SemanticVersion(fullConstrainedVersion)) >= 0;

            case CONSTRAIN_OPERATOR_EQUAL_OR_SMALLER:
                return !version.isUpdateFor(new SemanticVersion(fullConstrainedVersion));

            case CONSTRAIN_OPERATOR_TWIDDLE_WAKKA:
                String[] fullConstrainedVersionSplit = fullConstrainedVersion.split("\\.");
                fullConstrainedVersionSplit[versionNumbers>1?versionNumbers-2:versionNumbers-1] = String.valueOf(Integer.parseInt(fullConstrainedVersionSplit[versionNumbers>1?versionNumbers-2:versionNumbers-1]) + 1);
                for (int i = versionNumbers>1?versionNumbers-1:versionNumbers; i < 3; i++) {
                    fullConstrainedVersionSplit[i] = "0";
                }
                String maxVersion = String.join(".", fullConstrainedVersionSplit);
                return (new SemanticVersion(maxVersion).isUpdateFor(version) && (version.equals(new SemanticVersion(fullConstrainedVersion)) || version.isUpdateFor(new SemanticVersion(fullConstrainedVersion))));

            default:
                throw new IllegalArgumentException("Unknown version range operator: " + constrainOperator);
        }
    }
}
