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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * The locales the application UI is translated to. Kept as a single source of
 * truth so the first-run language dialog and the Preferences {@link
 * ca.sqlpower.architect.LocaleChooser} offer the same set.
 */
public final class SupportedLanguages {

    /**
     * The supported UI languages. Each must have a matching
     * {@code messages_<lang>.properties} bundle in the app/library packages.
     */
    private static final List<Locale> SUPPORTED = Collections.unmodifiableList(Arrays.asList(
            new Locale("en"),
            new Locale("de"),
            new Locale("ko"),
            new Locale("ru"),
            new Locale("pt"),
            new Locale("es"),
            new Locale("zh"),
            new Locale("ja")));

    private SupportedLanguages() {
        // utility class
    }

    /**
     * Returns the immutable list of UI locales the application supports.
     */
    public static List<Locale> getSupportedLocales() {
        return SUPPORTED;
    }
}