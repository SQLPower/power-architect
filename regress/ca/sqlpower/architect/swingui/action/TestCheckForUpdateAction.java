/*
 * Copyright (c) 2026, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.sqlpower.architect.swingui.action;

import junit.framework.TestCase;

/**
 * Tests for {@link CheckForUpdateAction}'s GitHub Releases JSON parsing.
 */
public class TestCheckForUpdateAction extends TestCase {

    public void testExtractTagName() {
        String body = "{\"url\":\"https://api.github.com/repos/SQLPower/power-architect/releases/1\","
                + "\"html_url\":\"https://github.com/SQLPower/power-architect/releases/tag/2.0.11\","
                + "\"tag_name\":\"2.0.11\",\"name\":\"2.0.11\",\"draft\":false,\"prerelease\":false}";
        assertEquals("2.0.11", CheckForUpdateAction.extractTagName(body));
    }

    public void testExtractTagNameWithVPrefix() {
        String body = "{\"tag_name\":\"v1.0.9\"}";
        assertEquals("1.0.9", CheckForUpdateAction.extractTagName(body));
    }

    public void testExtractTagNameWithUppercaseVPrefix() {
        String body = "{\"tag_name\":\"V1.9.0\"}";
        assertEquals("1.9.0", CheckForUpdateAction.extractTagName(body));
    }

    public void testExtractTagNameReturnsNullWhenMissing() {
        String body = "{\"message\":\"Not Found\"}";
        assertNull(CheckForUpdateAction.extractTagName(body));
    }
}