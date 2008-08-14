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
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.swingui.ContainerPane;
import ca.sqlpower.architect.swingui.ContainerPaneUI;
import ca.sqlpower.architect.swingui.PlayPenComponent;

/**
 * Does all of the generic painting and event handling that applies to
 * all "pane" type components in the OLAP play pen.
 * <p>
 * Our plan is to eventually move all the generic stuff up into ContainerPaneUI
 * so that BasicTablePaneUI can get simpler.
 */
public abstract class OLAPPaneUI extends ContainerPaneUI {
    
    private static final Logger logger = Logger.getLogger(OLAPPaneUI.class);
    
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
     * The width and height of the arc for a rounded rectangle container. 
     */
    private static final int ARC_LENGTH = 7;
    
    /**
     * Doesn't return a preferredSize with width less than this.
     */
    private static final int MINIMUM_WIDTH = 100;
    
    private Color selectedColor = new Color(204, 204, 255);
    
    protected ContainerPane<? extends OLAPObject, ? extends OLAPObject> containerPane;
    
    protected final List<PaneSection> paneSections = new ArrayList<PaneSection>();

    /**
     * Returns this pane's list of sections.
     */
    public List<PaneSection> getSections() {
        return paneSections;
    }
    
    @Override
    public int pointToItemIndex(Point p) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Dimension getPreferredSize() {
        return getPreferredSize(containerPane);
    }
    
    public Dimension getPreferredSize(ContainerPane<?, ?> cp) {

        int height = 0;
        int width = 0;

        Insets insets = cp.getInsets();

        Font font = cp.getFont();
        if (font == null) {
            logger.error("getPreferredSize(): CubePane is missing font."); //$NON-NLS-1$
            return null;
        }

        FontMetrics metrics = cp.getFontMetrics(font);
        int fontHeight = metrics.getHeight();

        // Height calculation
        height = insets.top + fontHeight;
        for (PaneSection ps : paneSections) {
            height += GAP + fontHeight + TABLE_GAP + cp.getMargin().top + ps.getItems().size() * fontHeight + BOX_LINE_THICKNESS*2;
        }
        height += cp.getMargin().bottom + insets.bottom;
        
        // Width calculation
        width = MINIMUM_WIDTH;
        width = Math.max(width, calculateTextWidth(cp, cp.getName()));
        for (PaneSection ps : paneSections) {
            width = calculateMaxSectionWidth(ps, cp, width);
        }
        width += insets.left + cp.getMargin().left + BOX_LINE_THICKNESS*2 + cp.getMargin().right + insets.right;

        // Pack width and height into a awt.Dimension object
        return new Dimension(width, height);
    }

    public <T extends OLAPObject, C extends OLAPObject> void installUI(ContainerPane<T, C> c) {
        containerPane = (ContainerPane<T, C>) c;
    }

    public void paint(Graphics2D g2) {
        paint(g2, containerPane);
        
    }
    
    /**
     * Paint the containerPane's UI
     */
    public void paint(Graphics2D g2, ContainerPane<?, ?> cp) {

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
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, cp.getWidth(), cp.getHeight());

        g2.translate(insets.left, insets.top);

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

        // print containerPane name
        g2.drawString(cp.getName(), 0, y += ascent);

        g2.setColor(Color.BLACK);
        
        // Draw each of the individual sections of this container pane.
        for(PaneSection ps : paneSections) {
            y = drawSection(ps, g2, cp, y);
        }
        
        // draw box around the component
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(0, fontHeight+GAP, width-BOX_LINE_THICKNESS, 
                height-(fontHeight+GAP+BOX_LINE_THICKNESS), ARC_LENGTH, ARC_LENGTH);

        g2.translate(-insets.left, -insets.top);
    }
    
    /**
     * This method returns the the name of the given OLAP Object. It's implementation
     * is dependent to individual containerpane objects. This method is needed because
     * OLAPObject doesn't have a default getName method.
     */
    protected abstract String getOLAPObjectName(OLAPObject oo);
 
    public boolean contains(Point p) {
        return containerPane.getBounds().contains(p);
    }

    public void revalidate() {
        containerPane.setSize(getPreferredSize());
    }
    
    /**
     * Draws the a particular section of a containerPane
     * @param ps The sectionPane representation of the section to be drawn
     * @param g The graphics object of the canvas
     * @param cp The owner container of the sectionPane
     * @param startingHeight Starting height of the drawing process, relative to the component
     * @return The finishing height after the drawing process, relative to the component
     */
    private int drawSection(PaneSection ps, Graphics2D g, ContainerPane<?, ?> cp, int startingHeight) {
        int width = cp.getWidth() - cp.getInsets().left - cp.getInsets().right;
        FontMetrics metrics = cp.getFontMetrics(cp.getFont());
        int fontHeight = metrics.getHeight();
        int ascent = metrics.getAscent();
        int maxDescent = metrics.getMaxDescent();
        
        startingHeight += GAP + BOX_LINE_THICKNESS;
        
        g.setColor(cp.getForegroundColor());
        g.drawString(ps.getTitle(), BOX_LINE_THICKNESS, startingHeight += fontHeight);

        startingHeight+= TABLE_GAP;
        g.setColor(Color.BLACK);
        g.drawLine(0, startingHeight+maxDescent-(TABLE_GAP/2), width-1, startingHeight+maxDescent-(TABLE_GAP/2));

        // print dimensions
        int i = 0;
        int hwidth = width - cp.getMargin().right - cp.getMargin().left - BOX_LINE_THICKNESS*2;
        for (OLAPObject dim : ps.getItems()) {
            // draws the dimensions
            if (cp.isItemSelected(i)) {
                if (logger.isDebugEnabled()) logger.debug("Item "+i+" is selected"); //$NON-NLS-1$ //$NON-NLS-2$
                g.setColor(selectedColor);
                g.fillRect(BOX_LINE_THICKNESS + cp.getMargin().left, startingHeight-ascent+fontHeight,
                        hwidth, fontHeight);
            }
            g.setColor(cp.getForegroundColor());
            g.drawString(getOLAPObjectName(dim), BOX_LINE_THICKNESS +
                    cp.getMargin().left, startingHeight += fontHeight);
            i++;
        }
        return startingHeight;
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
    
    /**
     * Calculates the width of a sectionPane, given an arbitrary initial value.
     */
    private int calculateMaxSectionWidth(PaneSection ps, ContainerPane<?, ?> cp, int initialVal) {
        int width = Math.max(initialVal, calculateTextWidth(cp, ps.getTitle()));
        for (OLAPObject oo : ps.getItems()) {
            if (oo == null) {
                logger.error("Found null column in dimension '"+cp.getName()+"'"); //$NON-NLS-1$ //$NON-NLS-2$
                throw new NullPointerException("Found null column in dimension '"+cp.getName()+"'"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            width = Math.max(width, calculateTextWidth(cp, getOLAPObjectName(oo)));
        }
        return width;
    }

}
