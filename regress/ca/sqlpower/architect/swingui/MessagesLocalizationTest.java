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

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.TestCase;

/**
 * Verifies that every supported language has a loadable ResourceBundle with at
 * least the same keys as the base (English) bundle for the main UI packages.
 */
public class MessagesLocalizationTest extends TestCase {

    private static final String[] BUNDLES = {
        "ca.sqlpower.architect.swingui.messages",
        "ca.sqlpower.architect.swingui.action.messages",
        "ca.sqlpower.architect.messages",
    };

    private static final String[] LIBRARY_BUNDLES = {
        "ca.sqlpower.swingui.messages",
        "ca.sqlpower.swingui.query.messages",
        "ca.sqlpower.sqlobject.messages",
    };

    private static final Locale[] LOCALES = {
        new Locale("en"),
        new Locale("es"),
        new Locale("pt"),
        new Locale("zh"),
        new Locale("ja"),
        new Locale("de"),
    };

    private static java.util.Set<String> keysOf(ResourceBundle bundle) {
        java.util.Set<String> keys = new java.util.HashSet<String>();
        for (Enumeration<String> e = bundle.getKeys(); e.hasMoreElements();) {
            keys.add(e.nextElement());
        }
        return keys;
    }

    public void testAppBundlesLoadAndContainBaseKeys() {
        for (String bundleName : BUNDLES) {
            ResourceBundle base = ResourceBundle.getBundle(bundleName, new Locale("en"));
            java.util.Set<String> baseKeys = keysOf(base);
            for (Locale locale : LOCALES) {
                ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
                java.util.Set<String> keys = keysOf(bundle);
                java.util.Set<String> missing = new java.util.HashSet<String>(baseKeys);
                missing.removeAll(keys);
                assertTrue("Bundle " + bundleName + " for " + locale
                        + " is missing base keys: " + missing, missing.isEmpty());
            }
        }
    }

    public void testLibraryBundlesLoadAndContainBaseKeys() {
        for (String bundleName : LIBRARY_BUNDLES) {
            ResourceBundle base = ResourceBundle.getBundle(bundleName, new Locale("en"));
            java.util.Set<String> baseKeys = keysOf(base);
            for (Locale locale : LOCALES) {
                ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
                java.util.Set<String> keys = keysOf(bundle);
                java.util.Set<String> missing = new java.util.HashSet<String>(baseKeys);
                missing.removeAll(keys);
                assertTrue("Bundle " + bundleName + " for " + locale
                        + " is missing base keys: " + missing, missing.isEmpty());
            }
        }
    }

    public void testEveryKeyResolvesInEveryLanguage() {
        for (String bundleName : BUNDLES) {
            ResourceBundle base = ResourceBundle.getBundle(bundleName, new Locale("en"));
            for (Locale locale : LOCALES) {
                ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
                for (Enumeration<String> e = base.getKeys(); e.hasMoreElements();) {
                    String key = e.nextElement();
                    try {
                        assertNotNull("Key " + key + " must have a value in " + locale,
                                bundle.getString(key));
                    } catch (MissingResourceException ex) {
                        fail("Key " + key + " missing in bundle " + bundleName + " for " + locale);
                    }
                }
            }
        }
    }
}