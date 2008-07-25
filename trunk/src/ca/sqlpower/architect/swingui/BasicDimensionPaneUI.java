/*
 * Copyright (c) 2008, SQL Power Group Inc.
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLColumn;

public class BasicDimensionPaneUI extends DimensionPaneUI {
    
    private static Logger logger = Logger.getLogger(BasicDimensionPaneUI.class);
    
    /**
     * The DimensionPane component that this UI delegate works for.
     */
    private DimensionPane dimensionPane;

    /**
     * Thickness (in Java2D units) of the surrounding box.
     */
    public static final int BOX_LINE_THICKNESS = 1;
    
    /**
     * Amount of space left between the surrounding box and the text it contains.
     */
    public static final int GAP = 1;
    
    /**
     * The amount of extra (vertical) space between the table name and the columns. 
     */
    public static final int TABLE_GAP = 10;
    
    /**
     * The width and height of the arc for a rounded rectangle table. 
     */
    private static final int ARC_LENGTH = 7;
    
    /**
     * Doesn't return a preferredSize with width less than this.
     */
    protected static final int MINIMUM_WIDTH = 100;
    
    protected Color selectedColor = new Color(204, 204, 255);
    
    public static PlayPenComponentUI createUI(PlayPenComponent c) {
        return new BasicDimensionPaneUI();
    }

    public void installUI(PlayPenComponent c) {
        dimensionPane = (DimensionPane) c;
        // TODO: add property change listener
    }

    public void uninstallUI(PlayPenComponent c) {
        // TODO: remove property change listener
    }

    public void paint(Graphics2D g) {
        paint(g, dimensionPane);
    }
    
    public void paint(Graphics g, PlayPenComponent ppc) {
        DimensionPane dp = (DimensionPane) ppc;
        Graphics2D g2 = (Graphics2D) g;

        if (logger.isDebugEnabled()) {
            Rectangle clip = g2.getClipBounds();
            if (clip != null) {
                g2.setColor(Color.RED);
                clip.width--;
                clip.height--;
                g2.draw(clip);
                g2.setColor(dp.getForegroundColor());
                logger.debug("Clipping region: "+g2.getClip()); //$NON-NLS-1$
            } else {
                logger.debug("Null clipping region"); //$NON-NLS-1$
            }
        }

        //  We don't want to paint inside the insets or borders.
        Insets insets = dp.getInsets();

        //builds a little buffer to reduce the clipping problem
        //this only seams to work at a non-zoomed level. This could 
        //use a little work (better fix)
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, dp.getWidth(), dp.getHeight());

        g.translate(insets.left, insets.top);

        int width = dp.getWidth() - insets.left - insets.right;
        int height = dp.getHeight() - insets.top - insets.bottom;

        Font font = dp.getFont();
        if (font == null) {
            // This happens when the dimension exists but has no visible ancestor.
            // Don't ask me why it's being asked to paint under those circumstances!
            //logger.error("paint(): Null font in DimensionPane "+c);
            return;
        }

        FontMetrics metrics = dp.getFontMetrics(font);
        int fontHeight = metrics.getHeight();
        int ascent = metrics.getAscent();
        int maxDescent = metrics.getMaxDescent();
        int indentWidth = calculateTextWidth(dp, " ");
        int y = 0;

        g2.setColor(dp.getPlayPen().getBackground());
        g2.fillRect(0, 0, width, height);
        // no need to reset to foreground: next operation always changes the colour

        if (dp.isSelected()) {
            g2.setColor(dp.getBackgroundColor().darker());
        } else {
            g2.setColor(dp.getBackgroundColor());
        }
        g2.fillRoundRect(0, 0, dp.getWidth(), fontHeight, ARC_LENGTH, ARC_LENGTH);

        g2.setColor(dp.getForegroundColor());

        // print dimension name
        g2.drawString(dp.getDimensionName(), 0, y += ascent);

        g2.setColor(Color.BLACK);
        
        y += GAP + BOX_LINE_THICKNESS;
        
        g2.setColor(dp.getForegroundColor());
        g2.drawString(dp.getDummyTable().getName(), BOX_LINE_THICKNESS, y += fontHeight);

        y+= TABLE_GAP;
        g2.setColor(Color.BLACK);
        g2.drawLine(0, y+maxDescent-(TABLE_GAP/2), width-1, y+maxDescent-(TABLE_GAP/2));

        // print columns
        int i = 0;
        int hwidth = width - dp.getMargin().right - dp.getMargin().left - BOX_LINE_THICKNESS*2;
        for (SQLColumn col : dp.getItems()) {
            // draws the column
            if (dp.isItemSelected(i)) {
                if (logger.isDebugEnabled()) logger.debug("Column "+i+" is selected"); //$NON-NLS-1$ //$NON-NLS-2$
                g2.setColor(selectedColor);
                g2.fillRect(BOX_LINE_THICKNESS + dp.getMargin().left, y-ascent+fontHeight,
                        hwidth, fontHeight);
            }
            g2.setColor(dp.getForegroundColor());
            g2.drawString(col.getShortDisplayName(), BOX_LINE_THICKNESS +
                    dp.getMargin().left + i * indentWidth, y += fontHeight);
            i++;
        }

        // draw box around columns
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(0, fontHeight+GAP, width-BOX_LINE_THICKNESS, 
                height-(fontHeight+GAP+BOX_LINE_THICKNESS), ARC_LENGTH, ARC_LENGTH);

        g.translate(-insets.left, -insets.top);
    }

    public Dimension getPreferredSize() {
        return getPreferredSize(dimensionPane);
    }
    
    public Dimension getPreferredSize(PlayPenComponent ppc) {
        DimensionPane c = (DimensionPane) ppc;

        int height = 0;
        int width = 0;

        Insets insets = c.getInsets();
        List<SQLColumn> columnList = c.getItems();
        int cols = columnList.size();

        Font font = c.getFont();
        if (font == null) {
            logger.error("getPreferredSize(): DimensionPane is missing font."); //$NON-NLS-1$
            return null;
        }

        FontMetrics metrics = c.getFontMetrics(font);
        int fontHeight = metrics.getHeight();
        int indentWidth = calculateTextWidth(c, " ");

        height = insets.top + fontHeight + GAP + fontHeight + TABLE_GAP + c.getMargin().top + cols * fontHeight +
                BOX_LINE_THICKNESS*2 + c.getMargin().bottom + insets.bottom;
        width = MINIMUM_WIDTH;

        width = Math.max(width, calculateTextWidth(c, c.getDimensionName()));
        width = Math.max(width, calculateTextWidth(c, c.getDummyTable().getName()));
        
        int i = 0;
        for (SQLColumn col : c.getItems()) {
            if (col == null) {
                logger.error("Found null column in dimension '"+c.getName()+"'"); //$NON-NLS-1$ //$NON-NLS-2$
                throw new NullPointerException("Found null column in dimension '"+c.getName()+"'"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            width = Math.max(width, calculateTextWidth(c, col.getShortDisplayName()) + i * indentWidth);
            i++;
        }

        width += insets.left + c.getMargin().left + BOX_LINE_THICKNESS*2 + c.getMargin().right + insets.right;

        return new Dimension(width, height);
    }
    
    /**
     * Calculates the width of a given string based on given component's font.
     */
    private int calculateTextWidth(PlayPenComponent ppc, String text) {
        Font font = ppc.getFont();
        FontRenderContext frc = ppc.getFontRenderContext();
        FontMetrics metrics = ppc.getFontMetrics(font);
        
        if (text == null) text = "(null!?)"; //$NON-NLS-1$
        if (frc == null) {
            return metrics.stringWidth(text);
        } else {
            return (int) font.getStringBounds(text, frc).getWidth();
        }
    }

    @Override
    public int pointToItemIndex(Point p) {
        Font font = dimensionPane.getFont();
        FontMetrics metrics = dimensionPane.getFontMetrics(font);
        int fontHeight = metrics.getHeight();

        int numCols = dimensionPane.getItems().size();
        int firstColStart = fontHeight * 2 + GAP + BOX_LINE_THICKNESS * 2 + TABLE_GAP + dimensionPane.getMargin().top;

        if (logger.isDebugEnabled()) logger.debug("p.y = "+p.y); //$NON-NLS-1$
        
        int returnVal;
        
        logger.debug("font height: " + fontHeight + ", firstColStart: " + firstColStart); //$NON-NLS-1$ //$NON-NLS-2$
        
        if (p.y < 0) {
            logger.debug("y<0"); //$NON-NLS-1$
            returnVal = ContainerPane.ITEM_INDEX_NONE;
        } else if (p.y <= fontHeight) {
            logger.debug("y<=fontHeight = "+fontHeight); //$NON-NLS-1$
            returnVal = ContainerPane.ITEM_INDEX_TITLE;
        } else if (p.y > firstColStart && p.y <= firstColStart + numCols * fontHeight) {
            returnVal = (p.y - firstColStart) / fontHeight;
        } else {
            returnVal = ContainerPane.ITEM_INDEX_NONE;
        }
        logger.debug("pointToColumnIndex return value is " + returnVal); //$NON-NLS-1$
        return returnVal;
    }
    
    public boolean contains(Point p) {
        return dimensionPane.getBounds().contains(p);
    }

    public void revalidate() {
    }
}
