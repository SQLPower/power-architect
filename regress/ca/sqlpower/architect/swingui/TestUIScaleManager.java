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

import junit.framework.TestCase;

/**
 * Tests for the global UI scaling helper methods.
 */
public class TestUIScaleManager extends TestCase {

    public void testScaledIconProducesLargerIcon() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.RED);
        g2.fillRect(0, 0, 16, 16);
        g2.dispose();
        ImageIcon base = new ImageIcon(img);

        ImageIcon scaled = UIScaleManager.scaledIcon(base, 2.0);

        assertEquals("Width should be scaled by the factor",
                32, scaled.getIconWidth());
        assertEquals("Height should be scaled by the factor",
                32, scaled.getIconHeight());
    }

    public void testScaledIconWithUnityFactorReturnsSameSize() {
        BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        ImageIcon base = new ImageIcon(img);

        ImageIcon scaled = UIScaleManager.scaledIcon(base, 1.0);

        assertEquals(8, scaled.getIconWidth());
        assertEquals(8, scaled.getIconHeight());
    }
}