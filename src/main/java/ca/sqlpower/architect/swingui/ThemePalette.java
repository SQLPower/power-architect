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

import java.awt.Color;

/**
 * Colors used by the custom-painted play pen components (canvas, table panes
 * and relationships). FlatLaf themes standard Swing widgets, but these
 * components paint themselves, so they need to read their colors from here to
 * follow the light/dark theme.
 */
public final class ThemePalette {

    private static final ThemePalette LIGHT = new ThemePalette(
            new Color(0xFA, 0xFA, 0xFA), // canvasBackground
            new Color(0x20, 0x20, 0x20), // canvasForeground
            new Color(0xE8, 0xE8, 0xE8), // tableBodyBackground
            new Color(0xB0, 0xB0, 0xB0), // tableBorder
            new Color(0xCC, 0xCC, 0xFF), // selection
            new Color(0xF3, 0xF3, 0xF3), // alternatingRow
            new Color(0xBF, 0xD6, 0xFF)); // defaultTableColour (celeste)

    private static final ThemePalette DARK = new ThemePalette(
            new Color(0x2B, 0x2B, 0x2B), // canvasBackground
            new Color(0xE6, 0xE6, 0xE6), // canvasForeground
            new Color(0x3C, 0x3F, 0x41), // tableBodyBackground
            new Color(0x5A, 0x5A, 0x5A), // tableBorder
            new Color(0x21, 0x4F, 0x78), // selection
            new Color(0x34, 0x37, 0x39), // alternatingRow
            new Color(0x2A, 0x4A, 0x6E)); // defaultTableColour (dark celeste)

    private final Color canvasBackground;
    private final Color canvasForeground;
    private final Color tableBodyBackground;
    private final Color tableBorder;
    private final Color selection;
    private final Color alternatingRow;
    private final Color defaultTableColour;

    private ThemePalette(Color canvasBackground, Color canvasForeground,
            Color tableBodyBackground, Color tableBorder, Color selection,
            Color alternatingRow, Color defaultTableColour) {
        this.canvasBackground = canvasBackground;
        this.canvasForeground = canvasForeground;
        this.tableBodyBackground = tableBodyBackground;
        this.tableBorder = tableBorder;
        this.selection = selection;
        this.alternatingRow = alternatingRow;
        this.defaultTableColour = defaultTableColour;
    }

    public static ThemePalette light() {
        return LIGHT;
    }

    public static ThemePalette dark() {
        return DARK;
    }

    /**
     * The palette for the currently active theme.
     */
    public static ThemePalette current() {
        return ThemeManager.isDark() ? DARK : LIGHT;
    }

    public Color getCanvasBackground() {
        return canvasBackground;
    }

    public Color getCanvasForeground() {
        return canvasForeground;
    }

    public Color getTableBodyBackground() {
        return tableBodyBackground;
    }

    public Color getTableBorder() {
        return tableBorder;
    }

    public Color getSelection() {
        return selection;
    }

    public Color getAlternatingRow() {
        return alternatingRow;
    }

    /**
     * The colour used for newly created tables by default. Light blue
     * (celeste) in the light theme, a matching muted blue in the dark theme.
     */
    public Color getDefaultTableColour() {
        return defaultTableColour;
    }
}