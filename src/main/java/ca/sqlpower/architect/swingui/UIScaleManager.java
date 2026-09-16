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

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 * Scales the entire Swing UI by re-deriving every {@link UIManager} font and
 * re-painting all open windows. This complements the {@link PlayPen} zoom,
 * which only scales the diagram canvas; this manager scales fonts, menus,
 * dialogs and everything else.
 */
public class UIScaleManager {

    public static final double MIN_SCALE = 0.6;
    public static final double MAX_SCALE = 2.5;

    private double scale = 1.0;
    private Map<Object, Font> baseFonts;

    public double getScale() {
        return scale;
    }

    public void setScale(double newScale) {
        double clamped = Math.max(MIN_SCALE, Math.min(MAX_SCALE, newScale));
        if (clamped == scale) return;
        scale = clamped;
        apply();
    }

    public void reset() {
        setScale(1.0);
    }

    /**
     * Discards the cached base fonts. Call this after the look and feel changes
     * so the next {@link #apply()} rescales the new theme's fonts instead of the
     * previous theme's.
     */
    public void invalidate() {
        baseFonts = null;
    }

    private void apply() {
        if (GraphicsEnvironment.isHeadless()) return;

        if (baseFonts == null) {
            baseFonts = new HashMap<Object, Font>();
            UIDefaults defaults = UIManager.getDefaults();
            for (Enumeration<Object> keys = defaults.keys(); keys.hasMoreElements();) {
                Object key = keys.nextElement();
                Object value = defaults.get(key);
                if (value instanceof Font) {
                    baseFonts.put(key, (Font) value);
                }
            }
        }

        UIDefaults defaults = UIManager.getDefaults();
        for (Map.Entry<Object, Font> entry : baseFonts.entrySet()) {
            Font base = entry.getValue();
            defaults.put(entry.getKey(), base.deriveFont((float) (base.getSize2D() * scale)));
        }

        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
            window.validate();
            window.repaint();
        }
    }
}