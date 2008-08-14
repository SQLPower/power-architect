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

package ca.sqlpower.architect.swingui.olap;

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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.OLAPChildEvent;
import ca.sqlpower.architect.olap.OLAPChildListener;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.CubeDimension;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.architect.swingui.ContainerPane;
import ca.sqlpower.architect.swingui.ContainerPaneUI;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenComponentUI;

public class BasicCubePaneUI extends ContainerPaneUI {

 private static Logger logger = Logger.getLogger(BasicCubePaneUI.class);
    
    /**
     * The CubePane component that this UI delegate works for.
     */
    private CubePane cube;

    /**
     * Thickness (in Java2D units) of the surrounding box.
     */
    public static final int BOX_LINE_THICKNESS = 1;
    
    /**
     * Amount of space left between the surrounding box and the text it contains.
     */
    public static final int GAP = 1;
    
    /**
     * The amount of extra (vertical) space between the dimensions and the measures. 
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
    
    private final ModelEventHandler modelEventHandler = new ModelEventHandler();
    
    public static PlayPenComponentUI createUI(PlayPenComponent c) {
        return new BasicCubePaneUI();
    }

    public void installUI(PlayPenComponent c) {
        cube = (CubePane) c;
        OLAPUtil.listenToHierarchy(cube.getModel(), modelEventHandler, modelEventHandler);
    }

    public void uninstallUI(PlayPenComponent c) {
        OLAPUtil.unlistenToHierarchy(cube.getModel(), modelEventHandler, modelEventHandler);
        cube = null;
    }

    public void paint(Graphics2D g) {
        paint(g, cube);
    }
    
    public void paint(Graphics g, PlayPenComponent ppc) {
        CubePane cp = (CubePane) ppc;
        Graphics2D g2 = (Graphics2D) g;

        if (logger.isDebugEnabled()) {
            Rectangle clip = g2.getClipBounds();
            if (clip != null) {
                g2.setColor(Color.RED);
                clip.width--;
                clip.height--;
                g2.draw(clip);
                g2.setColor(cp.getForegroundColor());
                logger.debug("Clipping region: "+g2.getClip()); //$NON-NLS-1$
            } else {
                logger.debug("Null clipping region"); //$NON-NLS-1$
            }
        }

        //  We don't want to paint inside the insets or borders.
        Insets insets = cp.getInsets();

        //builds a little buffer to reduce the clipping problem
        //this only seams to work at a non-zoomed level. This could 
        //use a little work (better fix)
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, cp.getWidth(), cp.getHeight());

        g.translate(insets.left, insets.top);

        int width = cp.getWidth() - insets.left - insets.right;
        int height = cp.getHeight() - insets.top - insets.bottom;

        Font font = cp.getFont();
        if (font == null) {
            // This happens when the cube exists but has no visible ancestor.
            // Don't ask me why it's being asked to paint under those circumstances!
            //logger.error("paint(): Null font in CubePane "+c);
            return;
        }

        FontMetrics metrics = cp.getFontMetrics(font);
        int fontHeight = metrics.getHeight();
        int ascent = metrics.getAscent();
        int maxDescent = metrics.getMaxDescent();
        int indentWidth = calculateTextWidth(cp, " ");
        int y = 0;

        g2.setColor(cp.getPlayPen().getBackground());
        g2.fillRect(0, 0, width, height);
        // no need to reset to foreground: next operation always changes the colour

        if (cp.isSelected()) {
            g2.setColor(cp.getBackgroundColor().darker());
        } else {
            g2.setColor(cp.getBackgroundColor());
        }
        g2.fillRoundRect(0, 0, cp.getWidth(), fontHeight, ARC_LENGTH, ARC_LENGTH);

        g2.setColor(cp.getForegroundColor());

        // print cube name
        g2.drawString(cp.getCubeName(), 0, y += ascent);

        g2.setColor(Color.BLACK);
        
        y += GAP + BOX_LINE_THICKNESS;
        
        g2.setColor(cp.getForegroundColor());
        g2.drawString("Dimensions", BOX_LINE_THICKNESS, y += fontHeight);

        y+= TABLE_GAP;
        g2.setColor(Color.BLACK);
        g2.drawLine(0, y+maxDescent-(TABLE_GAP/2), width-1, y+maxDescent-(TABLE_GAP/2));

        // print dimensions
        int i = 0;
        int hwidth = width - cp.getMargin().right - cp.getMargin().left - BOX_LINE_THICKNESS*2;
        for (CubeDimension dim : cp.getCube().getDimensions()) {
            // draws the dimensions
            if (cp.isItemSelected(i)) {
                if (logger.isDebugEnabled()) logger.debug("Item "+i+" is selected"); //$NON-NLS-1$ //$NON-NLS-2$
                g2.setColor(selectedColor);
                g2.fillRect(BOX_LINE_THICKNESS + cp.getMargin().left, y-ascent+fontHeight,
                        hwidth, fontHeight);
            }
            g2.setColor(cp.getForegroundColor());
            g2.drawString(dim.getName(), BOX_LINE_THICKNESS +
                    cp.getMargin().left + i * indentWidth, y += fontHeight);
            i++;
        }
        
        
        y += GAP + BOX_LINE_THICKNESS;
        
        g2.setColor(cp.getForegroundColor());
        g2.drawString("Measures", BOX_LINE_THICKNESS, y += fontHeight);

        y+= TABLE_GAP;
        g2.setColor(Color.BLACK);
        g2.drawLine(0, y+maxDescent-(TABLE_GAP/2), width-1, y+maxDescent-(TABLE_GAP/2));

        // print measures
        for (Measure measure : cp.getCube().getMeasures()) {
            // draws the measures
            if (cp.isItemSelected(i)) {
                if (logger.isDebugEnabled()) logger.debug("Item "+i+" is selected"); //$NON-NLS-1$ //$NON-NLS-2$
                g2.setColor(selectedColor);
                g2.fillRect(BOX_LINE_THICKNESS + cp.getMargin().left, y-ascent+fontHeight,
                        hwidth, fontHeight);
            }
            g2.setColor(cp.getForegroundColor());
            g2.drawString(measure.getName(), BOX_LINE_THICKNESS +
                    cp.getMargin().left + i * indentWidth, y += fontHeight);
            i++;
        }

        // draw box around columns
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(0, fontHeight+GAP, width-BOX_LINE_THICKNESS, 
                height-(fontHeight+GAP+BOX_LINE_THICKNESS), ARC_LENGTH, ARC_LENGTH);

        g.translate(-insets.left, -insets.top);
    }

    public Dimension getPreferredSize() {
        return getPreferredSize(cube);
    }
    
    public Dimension getPreferredSize(PlayPenComponent ppc) {
        CubePane c = (CubePane) ppc;

        int height = 0;
        int width = 0;

        Insets insets = c.getInsets();
        int dims = c.getCube().getDimensions().size();
        int measures = c.getCube().getMeasures().size();

        Font font = c.getFont();
        if (font == null) {
            logger.error("getPreferredSize(): CubePane is missing font."); //$NON-NLS-1$
            return null;
        }

        FontMetrics metrics = c.getFontMetrics(font);
        int fontHeight = metrics.getHeight();
        int indentWidth = calculateTextWidth(c, " ");

        // XXX the following expression needs to be revised after cubepane becomes responsive to various events.
        height = insets.top + fontHeight + GAP + fontHeight + TABLE_GAP + c.getMargin().top + dims * fontHeight + 
        GAP + fontHeight + TABLE_GAP + c.getMargin().top + measures * fontHeight + BOX_LINE_THICKNESS*2 +     // This line is added after 
        BOX_LINE_THICKNESS*2 + c.getMargin().bottom + insets.bottom;
        
        width = MINIMUM_WIDTH;
        
        width = Math.max(width, calculateTextWidth(c, c.getCubeName()));
        width = Math.max(width, calculateTextWidth(c, "Dimensions"));
        width = Math.max(width, calculateTextWidth(c, "Measures"));
        
        int i = 0;
        for (CubeDimension dim : c.getCube().getDimensions()) {
            if (dim == null) {
                logger.error("Found null column in dimension '"+c.getName()+"'"); //$NON-NLS-1$ //$NON-NLS-2$
                throw new NullPointerException("Found null column in dimension '"+c.getName()+"'"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            width = Math.max(width, calculateTextWidth(c, dim.getName()) + i * indentWidth);
            i++;
        }
        for (Measure measure : c.getCube().getMeasures()) {
            if (measure == null) {
                logger.error("Found null column in measures '"+c.getName()+"'"); //$NON-NLS-1$ //$NON-NLS-2$
                throw new NullPointerException("Found null column in dimension '"+c.getName()+"'"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            width = Math.max(width, calculateTextWidth(c, measure.getName()) + i * indentWidth);
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
        Font font = cube.getFont();
        FontMetrics metrics = cube.getFontMetrics(font);
        int fontHeight = metrics.getHeight();

        int numDims = cube.getCube().getDimensions().size();
        int firstDimStart = fontHeight * 2 + GAP + BOX_LINE_THICKNESS * 2 + TABLE_GAP + cube.getMargin().top;
        
        int numMeasures = cube.getCube().getMeasures().size();
        int firstMeasureStart = fontHeight * 2 + GAP + fontHeight + TABLE_GAP + cube.getMargin().top + numDims * fontHeight + 
        GAP + fontHeight + TABLE_GAP + cube.getMargin().top;

        if (logger.isDebugEnabled()) logger.debug("p.y = "+p.y); //$NON-NLS-1$
        
        int returnVal;
        
        logger.debug("font height: " + fontHeight + ", firstColStart: " + firstDimStart); //$NON-NLS-1$ //$NON-NLS-2$
        
        if (p.y < 0) {
            logger.debug("y<0"); //$NON-NLS-1$
            returnVal = ContainerPane.ITEM_INDEX_NONE;
        } else if (p.y <= fontHeight) {
            logger.debug("y<=fontHeight = "+fontHeight); //$NON-NLS-1$
            returnVal = ContainerPane.ITEM_INDEX_TITLE;
        } else if (p.y > firstDimStart && p.y <= firstDimStart + numDims * fontHeight) {
            returnVal = (p.y - firstDimStart) / fontHeight;
        } else if (p.y > firstMeasureStart && p.y <= firstMeasureStart + numMeasures * fontHeight) {
            returnVal = (p.y - firstDimStart + p.y - firstMeasureStart) / fontHeight;
        } else {
            returnVal = ContainerPane.ITEM_INDEX_NONE;
        }
        logger.debug("pointToColumnIndex return value is " + returnVal); //$NON-NLS-1$
        return returnVal;
    }
    
    public boolean contains(Point p) {
        return cube.getBounds().contains(p);
    }

    public void revalidate() {
        cube.setSize(getPreferredSize());
    }

    private class ModelEventHandler implements PropertyChangeListener, OLAPChildListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if ("name".equals(evt.getPropertyName())) {
                // note this could be the name of the cube or any of its child objects,
                // since we have property change listeners on every object in the subtree under cube
                cube.revalidate();
            }
        }

        public void olapChildAdded(OLAPChildEvent e) {
            OLAPUtil.listenToHierarchy(e.getChild(), this, this);
            cube.revalidate();
        }

        public void olapChildRemoved(OLAPChildEvent e) {
            OLAPUtil.unlistenToHierarchy(e.getChild(), this, this);
            cube.revalidate();
        }
        
    }
}
