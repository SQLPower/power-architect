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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.OLAPChildEvent;
import ca.sqlpower.architect.olap.OLAPChildListener;
import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPUtil;
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
public abstract class OLAPPaneUI<T extends OLAPObject, C extends OLAPObject> extends ContainerPaneUI {
    
    private static final Logger logger = Logger.getLogger(OLAPPaneUI.class);
    
    /**
     * Thickness (in Java2D units) of the surrounding box.
     */
    private static final int BOX_LINE_THICKNESS = 1;
    
    /**
     * Amount of space left between the surrounding box and the text it contains.
     */
    private static final int GAP = 1;
    
    /**
     * The amount of extra (vertical) space between one section and the next.
     * The line dividing the sections is within this gap. 
     * <p>
     * By "inter-section" we mean "between sections," not "where two lines meet."
     */
    private static final int INTER_SECTION_GAP = 9;
    
    /**
     * The width and height of the arc for a rounded rectangle container. 
     */
    private static final int ARC_LENGTH = 7;
    
    /**
     * Doesn't return a preferredSize with width less than this.
     */
    private static final int MINIMUM_WIDTH = 100;

    /**
     * Amount of extra space after the section header before the first item in the section.
     */
    private static final int SECTION_HEADER_GAP = 5;
    
    private Color selectedColor = new Color(204, 204, 255);
    
    protected OLAPPane<T, C> olapPane;
    
    protected final List<PaneSection<C>> paneSections = new ArrayList<PaneSection<C>>();

    private final ModelEventHandler modelEventHandler = new ModelEventHandler();
    
    /**
     * Returns this pane's list of sections.
     */
    public List<PaneSection<C>> getSections() {
        return paneSections;
    }
    
    /**
     * Calculates and returns the size of the given ContainerPane object
     */
    public Dimension getPreferredSize() {
        ContainerPane<?, ?> cp = olapPane;

        int height = 0;
        int width = 0;

        Insets insets = cp.getInsets();

        Font font = cp.getFont();
        if (font == null) {
            logger.error("getPreferredSize(): ContainerPane is missing font."); //$NON-NLS-1$
            return null;
        }

        FontMetrics metrics = cp.getFontMetrics(font);
        int fontHeight = metrics.getHeight();

        // Height calculation
        height = insets.top + fontHeight;
        for (PaneSection<C> ps : paneSections) {
            height += GAP + fontHeight + INTER_SECTION_GAP + cp.getMargin().top + ps.getItems().size() * fontHeight + BOX_LINE_THICKNESS*2;
        }
        height += cp.getMargin().bottom + insets.bottom;
        
        // Width calculation
        width = MINIMUM_WIDTH;
        width = Math.max(width, calculateTextWidth(cp, cp.getName()));
        for (PaneSection<C> ps : paneSections) {
            width = Math.max(width, calculateMaxSectionWidth(ps, cp));
        }
        width += insets.left + cp.getMargin().left + BOX_LINE_THICKNESS*2 + cp.getMargin().right + insets.right;

        // Pack width and height into a awt.Dimension object
        return new Dimension(width, height);
    }

    public void installUI(PlayPenComponent c) {
        olapPane = (OLAPPane<T, C>) c;
        OLAPUtil.listenToHierarchy(olapPane.getModel(), modelEventHandler, modelEventHandler);
    }

    public void uninstallUI(PlayPenComponent c) {
        OLAPUtil.unlistenToHierarchy(olapPane.getModel(), modelEventHandler, modelEventHandler);
    }
    
    public void paint(Graphics2D g2) {
        ContainerPane<T, C> cp = olapPane;
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

        //builds a little buffer to reduce the clipping problem
        //this only seams to work at a non-zoomed level. This could 
        //use a little work (better fix)
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, cp.getWidth(), cp.getHeight());

        int width = cp.getWidth() - 1;
        int height = cp.getHeight();

        Font font = cp.getFont();
        if (font == null) {
            // This happens when the containerPane exists but has no visible ancestor.
            // Don't ask me why it's being asked to paint under those circumstances!
            //logger.error("paint(): Null font in ContainerPane "+c);
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
        g2.fillRoundRect(0, 0, width, fontHeight, ARC_LENGTH, ARC_LENGTH);

        g2.setColor(cp.getForegroundColor());

        // print containerPane name
        g2.drawString(cp.getModel().getName(), 0, y += ascent);

        g2.setColor(Color.BLACK);
        
        y += GAP + BOX_LINE_THICKNESS;
        
        // Draw each of the individual sections of this container pane.
        boolean firstSection = true;
        for(PaneSection<C> ps : paneSections) {
            if (!firstSection) {
                g2.drawLine(
                        0,     y + (INTER_SECTION_GAP + fontHeight - ascent) / 2,
                        width, y + (INTER_SECTION_GAP + fontHeight - ascent) / 2);
                y += INTER_SECTION_GAP;
            }
            y = drawSection(ps, g2, cp, y);
            firstSection = false;
        }
        
        // draw box around the component
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(0, fontHeight+GAP, width, 
                height-(fontHeight+GAP+BOX_LINE_THICKNESS), ARC_LENGTH, ARC_LENGTH);

    }

    public boolean contains(Point p) {
        return olapPane.getBounds().contains(p);
    }

    public void revalidate() {
        olapPane.setSize(getPreferredSize());
    }

    /**
     * Looks up the section that the given point resides in, and translates the
     * given point so that it is relative to the top left corner of the returned
     * section. If the given point is not in any section, the return value will
     * be null and the <code>p</code> will not have been modified.
     * 
     * @param p
     *            The point in overall component coordinates. <b>This point will
     *            be modified</b> if this method returns non-null.
     * @return The section the given point is located in, plus the passed-in
     *         point will have been translated.
     */
    public PaneSection<C> toSectionLocation(Point p) {
        Font font = olapPane.getFont();
        FontMetrics metrics = olapPane.getFontMetrics(font);
        int fontHeight = metrics.getHeight();
        int ascent = metrics.getAscent();

        logger.debug("SECLOC: searching for section location of y="+p.y);
        
        if (p.y <= fontHeight) {
            logger.debug("y<=fontHeight = "+fontHeight); //$NON-NLS-1$
            logger.debug("SECLOC: it's in the title");
            return null;
        }
        
        int translatedY = p.y - fontHeight;
        logger.debug("SECLOC: looking through sections; translatedY="+translatedY);
        
        boolean firstSection = true;
        for (PaneSection<C> sect : getSections()) {
            int sectionHeight = fontHeight * sect.getItems().size();
            if (sect.getTitle() != null) {
                sectionHeight += fontHeight + SECTION_HEADER_GAP;
            }
            if (firstSection) {
                sectionHeight += (INTER_SECTION_GAP + fontHeight - ascent) / 2;
                firstSection = false;
            } else {
                sectionHeight += INTER_SECTION_GAP;
            }
            
            if (translatedY < sectionHeight) {
                p.y = translatedY;
                return sect;
            }
            
            translatedY -= sectionHeight;
        }
        
        return null;
    }
    
    @Override
    public int pointToItemIndex(Point p) {
        p = new Point(p);
        ContainerPane<T, C> cube = olapPane;
        Font font = cube.getFont();
        FontMetrics metrics = cube.getFontMetrics(font);
        int fontHeight = metrics.getHeight();
        int descent = metrics.getDescent();

        if (logger.isDebugEnabled()) {
            logger.debug("p.y = "+p.y +
                    "; fontHeight = " + fontHeight +
                    "; descent = " + descent); //$NON-NLS-1$
        }
        
        PaneSection<C> sect;
        int returnVal;
        if (p.y < 0) {
            logger.debug("y<0"); //$NON-NLS-1$
            returnVal = ContainerPane.ITEM_INDEX_NONE;
        } else if (p.y <= fontHeight) {
            logger.debug("y<=fontHeight = "+fontHeight); //$NON-NLS-1$
            returnVal = ContainerPane.ITEM_INDEX_TITLE;
        } else if ( (sect = toSectionLocation(p)) != null ) {
            // p is now a coordinate within sect
            returnVal = firstItemIndex(sect);

            logger.debug("Y is: " + p.y + " In section: " + sect.getTitle() + " and right now returnVal is: " + returnVal);

            // Adjustment for all cases: we're selecting over font area, not from the baseline
            int adjustment = -descent;

            // sections after the first one have some extra space on top
            if (sect != getSections().get(0)) {
                adjustment -= INTER_SECTION_GAP / 2;
            }
            
            // if there is a title, we have to adjust for that
            if (sect.getTitle() != null) {
                if (p.y <= fontHeight + SECTION_HEADER_GAP) {
                    // TODO we need a system for specifying a click on a section title
                    return ContainerPane.ITEM_INDEX_NONE;
                } else {
                    int sectTitleHeight = fontHeight + SECTION_HEADER_GAP;
                    adjustment -= sectTitleHeight;
                }
            }
            
            returnVal += (p.y + adjustment) / fontHeight;

        } else {
            returnVal = ContainerPane.ITEM_INDEX_NONE;
        }
        logger.debug("pointToColumnIndex return value is " + returnVal); //$NON-NLS-1$
        return returnVal;
    }
    
    /**
     * Returns the index of the first item in the given section.
     * 
     * @param sect A section in this pane.
     * @return
     */
    public int firstItemIndex(PaneSection<C> sect) {
        int index = 0;
        for (PaneSection<C> s : getSections()) {
            if (sect == s) {
                return index;
            }
            index += s.getItems().size();
        }
        throw new IllegalArgumentException("Given section is not part of this OLAP Pane");
    }

    
    /**
     * Draws the a particular section of a containerPane
     * 
     * @param ps
     *            The SectionPane representation of the section to be drawn
     * @param g
     *            The graphics object of the canvas
     * @param cp
     *            The owner container of the sectionPane
     * @param startY
     *            Starting Y coordinate for text baseline, relative to the
     *            component
     * @return The Y coordinate of the baseline of the last item drawn, relative to the
     *         component
     */
    private int drawSection(PaneSection<C> ps, Graphics2D g, ContainerPane<T, C> cp, final int startY) {
        int width = cp.getWidth() - cp.getInsets().left - cp.getInsets().right;
        FontMetrics metrics = cp.getFontMetrics(cp.getFont());
        int fontHeight = metrics.getHeight();
        int ascent = metrics.getAscent();
        
        int y = startY;
        
        g.setColor(cp.getForegroundColor());
        
        if (ps.getTitle() != null) {
            g.drawString(ps.getTitle(), BOX_LINE_THICKNESS, y += fontHeight);
            y += SECTION_HEADER_GAP;
        }
        
        // print items
        int i = 0;
        int hwidth = width - cp.getMargin().right - cp.getMargin().left - BOX_LINE_THICKNESS*2;
        for (C item : ps.getItems()) {
            if (cp.isItemSelected(item)) {
                if (logger.isDebugEnabled()) logger.debug("Item "+i+" is selected"); //$NON-NLS-1$ //$NON-NLS-2$
                g.setColor(selectedColor);
                g.fillRect(
                        BOX_LINE_THICKNESS + cp.getMargin().left, y-ascent+fontHeight,
                        hwidth, fontHeight);
                g.setColor(cp.getForegroundColor());
            }
            g.drawString(item.getName(), BOX_LINE_THICKNESS +
                    cp.getMargin().left, y += fontHeight);
            i++;
        }
        return y;
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
     * Calculates the width of a sectionPane.
     */
    private int calculateMaxSectionWidth(PaneSection<C> ps, ContainerPane<?, ?> cp) {
        int width = calculateTextWidth(cp, ps.getTitle());
        for (C oo : ps.getItems()) {
            if (oo == null) {
                logger.error("Found null column in dimension '"+cp.getName()+"'"); //$NON-NLS-1$ //$NON-NLS-2$
                throw new NullPointerException("Found null column in dimension '"+cp.getName()+"'"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            width = Math.max(width, calculateTextWidth(cp, oo.getName()));
        }
        return width;
    }

    private class ModelEventHandler implements PropertyChangeListener, OLAPChildListener {

        public void propertyChange(PropertyChangeEvent evt) {
            logger.debug("Property Change: " +
                    evt.getPropertyName() + ": " +
                    evt.getOldValue() + " -> " + evt.getNewValue());
            if ("name".equals(evt.getPropertyName())) {
                // note this could be the name of the cube or any of its child objects,
                // since we have property change listeners on every object in the subtree under cube
                olapPane.revalidate();
            }
        }

        public void olapChildAdded(OLAPChildEvent e) {
            OLAPUtil.listenToHierarchy(e.getChild(), this, this);
            olapPane.revalidate();
        }

        public void olapChildRemoved(OLAPChildEvent e) {
            OLAPUtil.unlistenToHierarchy(e.getChild(), this, this);
            olapPane.revalidate();
        }
        
    }
}
