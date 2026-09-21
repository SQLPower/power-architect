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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import junit.framework.TestCase;

/**
 * Tests for the global UI scaling helper methods.
 */
public class TestUIScaleManager extends TestCase {

    private static ImageIcon icon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.RED);
        g2.fillRect(0, 0, size, size);
        g2.dispose();
        return new ImageIcon(img);
    }

    public void testScaledIconProducesLargerIcon() {
        ImageIcon base = icon(16);

        ImageIcon scaled = UIScaleManager.scaledIcon(base, 2.0);

        assertEquals("Width should be scaled by the factor",
                32, scaled.getIconWidth());
        assertEquals("Height should be scaled by the factor",
                32, scaled.getIconHeight());
    }

    public void testScaledIconWithUnityFactorReturnsSameSize() {
        ImageIcon base = icon(8);

        ImageIcon scaled = UIScaleManager.scaledIcon(base, 1.0);

        assertEquals(8, scaled.getIconWidth());
        assertEquals(8, scaled.getIconHeight());
    }

    /**
     * The global UI zoom must be able to zoom in and then reset to 100% without
     * permanently growing the toolbar/menu icons. Scaling must always start
     * from the ORIGINAL icon, not from the currently-scaled one. This was the
     * bug that broke the UI zoom after several +/- clicks (icons compounded).
     */
    public void testRescaleButtonIconIsIdempotentFromOriginal() {
        JButton button = new JButton();
        button.setIcon(icon(16));

        // Click + (factor 1.5): accumulated scale becomes 1.5.
        UIScaleManager.rescaleButtonIcon(button, 1.5);
        assertEquals(24, button.getIcon().getIconWidth());

        // Click + again (accumulated scale 1.5*1.5 = 2.25).
        UIScaleManager.rescaleButtonIcon(button, 2.25);
        assertEquals(36, button.getIcon().getIconWidth());

        // Reset to 100%: must restore the ORIGINAL 16px icon, not 36px.
        UIScaleManager.rescaleButtonIcon(button, 1.0);
        assertEquals("Reset must restore the ORIGINAL icon size",
                16, button.getIcon().getIconWidth());
    }
}