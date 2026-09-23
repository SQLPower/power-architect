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
package ca.sqlpower.architect.swingui;

import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

/**
 * Tests for the supported languages list used by the first-run language
 * selector (added because the jpackage installer no longer offers a language
 * picker like the legacy izpack one did).
 */
public class TestSupportedLanguages extends TestCase {

    public void testSupportedLanguagesIncludeTheSixTargetLocales() {
        List<Locale> locales = SupportedLanguages.getSupportedLocales();

        List<String> tags = new java.util.ArrayList<String>();
        for (Locale l : locales) {
            tags.add(l.getLanguage());
        }

        for (String expected : new String[] { "en", "es", "pt", "zh", "ja", "de" }) {
            assertTrue("Supported languages must include " + expected
                    + " (got: " + tags + ")", tags.contains(expected));
        }
    }

    public void testSupportedLanguagesAreNonNull() {
        for (Locale l : SupportedLanguages.getSupportedLocales()) {
            assertNotNull(l.getLanguage());
            assertFalse(l.getLanguage().isEmpty());
        }
    }
}