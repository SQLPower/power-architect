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

import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * Applies and remembers the application look and feel. Two modern FlatLaf
 * themes (light and dark) are offered, plus the native system look and feel.
 */
public final class ThemeManager {

    public enum Theme {
        SYSTEM, LIGHT, DARK
    }

    private static final String PREF_KEY = "theme";

    private static Theme current;

    private ThemeManager() {
    }

    public static Theme getSavedTheme() {
        Preferences prefs = Preferences.userNodeForPackage(ArchitectFrame.class);
        String name = prefs.get(PREF_KEY, Theme.LIGHT.name());
        try {
            return Theme.valueOf(name);
        } catch (IllegalArgumentException e) {
            return Theme.LIGHT;
        }
    }

    public static void saveTheme(Theme theme) {
        Preferences prefs = Preferences.userNodeForPackage(ArchitectFrame.class);
        prefs.put(PREF_KEY, theme.name());
    }

    public static Theme getCurrentTheme() {
        return current != null ? current : getSavedTheme();
    }

    public static boolean isDark() {
        return getCurrentTheme() == Theme.DARK;
    }

    /**
     * Installs the look and feel for the given theme and repaints every open
     * window. Safe to call in a headless environment (no windows are touched).
     */
    public static void applyTheme(Theme theme) {
        current = theme;
        try {
            if (theme == Theme.DARK) {
                FlatDarkLaf.setup();
            } else if (theme == Theme.LIGHT) {
                FlatLightLaf.setup();
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            // Keep whatever look and feel is currently installed.
        }
        if (GraphicsEnvironment.isHeadless()) return;
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
            window.validate();
            window.repaint();
        }
    }
}