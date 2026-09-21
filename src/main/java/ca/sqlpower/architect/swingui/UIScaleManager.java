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
import java.awt.Image;
import java.awt.Window;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
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

    /**
     * The ORIGINAL (unscaled) icon of every toolbar/menu/label component that
     * has been rescaled. Keyed weakly so components can be garbage collected.
     * Scaling always derives from these originals so that zooming in and
     * resetting to 100% is idempotent (icons never compound).
     */
    private static final Map<java.awt.Component, ImageIcon> baseIcons =
            new WeakHashMap<java.awt.Component, ImageIcon>();

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
     * Re-applies the current scale factor. Useful after the look and feel has
     * changed: call {@link #invalidate()} first so the new theme's base fonts
     * are used.
     */
    public void refresh() {
        apply();
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
            rescaleIcons(window);
            window.validate();
            window.repaint();
        }
    }

    /**
     * Returns a copy of the given icon whose dimensions are multiplied by the
     * given factor. Used to scale toolbar/menu icons along with the fonts.
     */
    public static ImageIcon scaledIcon(ImageIcon icon, double factor) {
        int newWidth = Math.max(1, (int) Math.round(icon.getIconWidth() * factor));
        int newHeight = Math.max(1, (int) Math.round(icon.getIconHeight() * factor));
        Image scaled = icon.getImage().getScaledInstance(
                newWidth, newHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    /**
     * Returns the ORIGINAL (unscaled) icon of the given component, remembering
     * it the first time it is seen. Used so icon rescaling always starts from
     * the base icon and is therefore idempotent (see {@link #baseIcons}).
     */
    private static ImageIcon originalIconOf(java.awt.Component component, Icon current) {
        ImageIcon remembered = baseIcons.get(component);
        if (remembered != null) {
            return remembered;
        }
        if (current instanceof ImageIcon) {
            ImageIcon original = (ImageIcon) current;
            baseIcons.put(component, original);
            return original;
        }
        return null;
    }

    /**
     * Re-scales the icon of a single button, always starting from the button's
     * ORIGINAL icon. Exposed (package-private) for testing: zooming in and then
     * resetting must restore the original icon size, never compound.
     */
    static void rescaleButtonIcon(AbstractButton button, double factor) {
        ImageIcon original = originalIconOf(button, button.getIcon());
        if (original != null) {
            button.setIcon(scaledIcon(original, factor));
        }
    }

    /**
     * Re-scales the icons of every button, menu item and label under the given
     * component so the global UI scale applies to them too (not just fonts).
     * Scaling always derives from each component's ORIGINAL icon so repeated
     * zoom in/out and reset to 100% never degrade or grow the icons.
     */
    private void rescaleIcons(java.awt.Component component) {
        if (component instanceof JMenu) {
            JMenu menu = (JMenu) component;
            ImageIcon original = originalIconOf(component, menu.getIcon());
            if (original != null) {
                menu.setIcon(scaledIcon(original, scale));
            }
            for (int i = 0; i < menu.getItemCount(); i++) {
                JMenuItem item = menu.getItem(i);
                if (item != null) {
                    rescaleIcons(item);
                }
            }
        } else if (component instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) component;
            ImageIcon original = originalIconOf(component, button.getIcon());
            if (original != null) {
                button.setIcon(scaledIcon(original, scale));
            }
            ImageIcon rolloverOriginal = originalIconOf(component, button.getRolloverIcon());
            if (rolloverOriginal != null) {
                button.setRolloverIcon(scaledIcon(rolloverOriginal, scale));
            }
        } else if (component instanceof JLabel) {
            ImageIcon original = originalIconOf(component, ((JLabel) component).getIcon());
            if (original != null) {
                ((JLabel) component).setIcon(scaledIcon(original, scale));
            }
        }
        if (component instanceof java.awt.Container) {
            java.awt.Component[] children = ((java.awt.Container) component).getComponents();
            for (java.awt.Component child : children) {
                rescaleIcons(child);
            }
        }
    }
}