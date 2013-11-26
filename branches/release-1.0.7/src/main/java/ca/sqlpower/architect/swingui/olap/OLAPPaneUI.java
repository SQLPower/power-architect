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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.beans.PropertyChangeEvent;

import javax.swing.Icon;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.swingui.ContainerPane;
import ca.sqlpower.architect.swingui.ContainerPaneUI;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenCoordinate;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;

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
    private static final int ARC_LENGTH = 10;
    
    /**
     * Dashed and normal strokes for different line styles.
     */
    private static final BasicStroke DASHED_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_BEVEL, 1.0f, new float[] { 15.0f, 4.0f }, 0.0f);

    private static final BasicStroke NORMAL_STROKE = new BasicStroke(1.0f);
    
    /**
     * Doesn't return a preferredSize with width less than this.
     */
    private static final int MINIMUM_WIDTH = 100;

    /**
     * Amount of extra space after the section header before the first item in the section.
     */
    private static final int SECTION_HEADER_VGAP = 5;

    private static final int ICON_TITLE_HGAP = 5;
    
    private Color selectedColor = new Color(204, 204, 255);
    
    protected OLAPPane<T, C> olapPane;
    
    protected final ModelEventHandler modelEventHandler = new ModelEventHandler();

    protected final PaneEventHandler paneEventHandler = new PaneEventHandler();

    /**
     * Calculates and returns the ideal size for this OLAPPane. If you override
     * or change the {@link #paint(Graphics2D)} or
     * {@link #drawSection(PaneSection, Graphics2D, ContainerPane, int)}
     * methods, you will have to override this method as well to compensate.
     */
    public Dimension getPreferredSize() {
        ContainerPane<?, ?> cp = olapPane;

        int height = 0;
        int width = 0;

        Font font = cp.getFont();
        if (font == null) {
            logger.error("getPreferredSize(): ContainerPane is missing font."); //$NON-NLS-1$
            return null;
        }

        FontMetrics metrics = cp.getFontMetrics(font);
        int fontHeight = metrics.getHeight();

        height += fontHeight + GAP + BOX_LINE_THICKNESS;
        for (PaneSection<? extends C> ps : olapPane.getSections()) {
            if (ps.getTitle() != null) {
                height += fontHeight + SECTION_HEADER_VGAP; 
            }
            height += ps.getItems().size() * fontHeight;
        }
        height += INTER_SECTION_GAP * (olapPane.getSections().size() - 1);
        height += BOX_LINE_THICKNESS;

        /*
         * If there are no named sections, the box will get squished down too
         * much. This establishes a reasonable minimum height based on font size
         * (so unfortunately there can't be a MINUMUM_HEIGHT constant)
         */
        if (height < fontHeight * 3) {
            height = fontHeight * 3;
        }
        
        // Width calculation
        width = MINIMUM_WIDTH;
        width = Math.max(
                width,
                calculateTextWidth(cp, cp.getModelName()) +
                    OSUtils.iconFor(olapPane.getModel()).getIconWidth() + ICON_TITLE_HGAP);
        for (PaneSection<? extends C> ps : olapPane.getSections()) {
            width = Math.max(width, calculateMaxSectionWidth(ps, cp));
        }
        Insets insets = cp.getMargin();
        width += insets.left + cp.getMargin().left + BOX_LINE_THICKNESS*2 + cp.getMargin().right + insets.right;

        // Pack width and height into a awt.Dimension object
        return new Dimension(width, height);
    }

    public void installUI(PlayPenComponent c) {
        olapPane = (OLAPPane<T, C>) c;
        SQLPowerUtils.listenToHierarchy(olapPane.getModel(), modelEventHandler);
        olapPane.addSPListener(paneEventHandler);
    }

    public void uninstallUI(PlayPenComponent c) {
        SQLPowerUtils.unlistenToHierarchy(olapPane.getModel(), modelEventHandler);
        olapPane.removeSPListener(paneEventHandler);
    }
    
    public void paint(Graphics2D g2) {
        g2 = (Graphics2D) g2.create();
        OLAPPane<T, C> op = olapPane;
        Stroke oldStroke = g2.getStroke();
        
        if (op.isDashed()) {
            g2.setStroke(DASHED_STROKE);
        } else {
            g2.setStroke(NORMAL_STROKE);
        }

        if (logger.isDebugEnabled()) {
            Rectangle clip = g2.getClipBounds();
            if (clip != null) {
                g2.setColor(Color.RED);
                clip.width--;
                clip.height--;
                g2.draw(clip);
                g2.setColor(op.getForegroundColor());
                logger.debug("Clipping region: "+g2.getClip()); //$NON-NLS-1$
            } else {
                logger.debug("Null clipping region"); //$NON-NLS-1$
            }
        }
        

        //builds a little buffer to reduce the clipping problem
        //this only seams to work at a non-zoomed level. This could 
        //use a little work (better fix)
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, op.getWidth(), op.getHeight());

        int width = op.getWidth() - 1;
        int height = op.getHeight();

        Font font = op.getFont();
        if (font == null) {
            // This happens when the containerPane exists but has no visible ancestor.
            // Don't ask me why it's being asked to paint under those circumstances!
            //logger.error("paint(): Null font in ContainerPane "+c);
            return;
        }

        FontMetrics metrics = op.getFontMetrics(font);
        int fontHeight = metrics.getHeight();
        int ascent = metrics.getAscent();
        int y = 0;

        g2.setColor(op.getPlayPen().getBackground());
        g2.fillRect(0, 0, width, height);
        // no need to reset to foreground: next operation always changes the colour

        if (op.isSelected()) {
            g2.setColor(op.getBackgroundColor().darker());
        } else {
            g2.setColor(op.getBackgroundColor());
        }
        if (op.isRounded()) {
            g2.fillRoundRect(0, 0, width, fontHeight, ARC_LENGTH, ARC_LENGTH);
        } else {
            g2.fillRect(0, 0, width, fontHeight);
        }

        g2.setColor(op.getForegroundColor());

        Icon icon = OSUtils.iconFor(op.getModel());
        icon.paintIcon(null, g2, 0, 0);
        
        // print containerPane name
        g2.drawString(op.getModel().getName(), icon.getIconWidth() + ICON_TITLE_HGAP, y += ascent); // XXX should add font height, no?

        g2.setColor(Color.BLACK);
        
        y += GAP + BOX_LINE_THICKNESS;
        
        // Draw each of the individual sections of this container pane.
        boolean firstSection = true;
        for(PaneSection<? extends C> ps : olapPane.getSections()) {
            if (!firstSection) {
                g2.drawLine(
                        0,     y + (INTER_SECTION_GAP + fontHeight - ascent) / 2,
                        width, y + (INTER_SECTION_GAP + fontHeight - ascent) / 2);
                y += INTER_SECTION_GAP;
            }
            y = drawSection(ps, g2, op, y);
            firstSection = false;
        }
        
        // draw box around the component
        g2.setColor(Color.BLACK);
        if (op.isRounded()) {
            g2.drawRoundRect(0, fontHeight+GAP, width, 
                    height-(fontHeight+GAP+BOX_LINE_THICKNESS), ARC_LENGTH, ARC_LENGTH);
        } else {
            g2.drawRect(0, fontHeight+GAP, width,
                    height-(fontHeight+GAP+BOX_LINE_THICKNESS));
        }
        
        g2.setStroke(oldStroke);
        
    }

    public boolean contains(Point p) {
        return olapPane.getBounds().contains(p);
    }

    public void revalidate() {
        olapPane.setSize(getPreferredSize());
    }

    /**
     * Looks up the section that the given point resides in, and may translate
     * the given point so that it is relative to the top left corner of the
     * returned section. If the given point is not in any section, the return
     * value will be null and the <code>p</code> will not have been modified.
     * 
     * @param point
     *            The point in overall component coordinates. <b>This point will
     *            be modified</b> if this method returns non-null and editPoint
     *            is true.
     * @param editPoint
     *            If the given point should be edited or not.
     * @return The section the given point is located in, plus the passed-in
     *         point may have been translated.
     */
    public PaneSection<? extends C> toSectionLocation(Point point, boolean editPoint) {
        Point p = editPoint ? point : new Point(point);
        
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
        for (PaneSection<? extends C> sect : olapPane.getSections()) {
            int sectionHeight = fontHeight * sect.getItems().size();
            if (sect.getTitle() != null) {
                sectionHeight += fontHeight + SECTION_HEADER_VGAP;
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
    @Deprecated
    public int pointToItemIndex(Point p) {
        p = new Point(p);
        Font font = olapPane.getFont();
        FontMetrics metrics = olapPane.getFontMetrics(font);
        int fontHeight = metrics.getHeight();
        int descent = metrics.getDescent();

        if (logger.isDebugEnabled()) {
            logger.debug("p.y = "+p.y +
                    "; fontHeight = " + fontHeight +
                    "; descent = " + descent); //$NON-NLS-1$
        }
        
        PaneSection<? extends C> sect;
        int returnVal;
        if (p.y < 0) {
            logger.debug("y<0"); //$NON-NLS-1$
            returnVal = PlayPenCoordinate.ITEM_INDEX_NONE;
        } else if (p.y <= fontHeight) {
            logger.debug("y<=fontHeight = "+fontHeight); //$NON-NLS-1$
            returnVal = PlayPenCoordinate.ITEM_INDEX_TITLE;
        } else if ( (sect = toSectionLocation(p, true)) != null ) {
            // p is now a coordinate within sect
            returnVal = firstItemIndex(sect);

            logger.debug("Y is: " + p.y + " In section: " + sect.getTitle() + " and right now returnVal is: " + returnVal);

            // Adjustment for all cases: we're selecting over font area, not from the baseline
            int adjustment = -descent;

            // sections after the first one have some extra space on top
            if (sect != olapPane.getSections().get(0)) {
                adjustment -= INTER_SECTION_GAP / 2;
            }
            
            // if there is a title, we have to adjust for that
            if (sect.getTitle() != null) {
                if (p.y <= fontHeight + SECTION_HEADER_VGAP) {
                    // TODO we need a system for specifying a click on a section title
                    return PlayPenCoordinate.ITEM_INDEX_SECTION_TITLE;
                } else {
                    int sectTitleHeight = fontHeight + SECTION_HEADER_VGAP;
                    adjustment -= sectTitleHeight;
                }
            }
            
            returnVal += (p.y + adjustment) / fontHeight;

        } else {
            returnVal = PlayPenCoordinate.ITEM_INDEX_NONE;
        }
        logger.debug("pointToItemIndex return value is " + returnVal); //$NON-NLS-1$
        return returnVal;
    }

    /**
     * Translates the given point into a {@link PlayPenCoordinate}.
     * 
     * @param p The point to be translated.
     * 
     * @return The PlayPenCoordinate that represents the point.
     */
    public PlayPenCoordinate<T, C> pointToPPCoordinate(Point p) {
        p = new Point(p);
        Font font = olapPane.getFont();
        FontMetrics metrics = olapPane.getFontMetrics(font);
        int fontHeight = metrics.getHeight();
        int descent = metrics.getDescent();

        if (logger.isDebugEnabled()) {
            logger.debug("p.y = "+p.y +
                    "; fontHeight = " + fontHeight +
                    "; descent = " + descent); //$NON-NLS-1$
        }
        
        PaneSection<? extends C> sect;
        C item;
        int index;
        if (p.y < 0) {
            logger.debug("y<0"); //$NON-NLS-1$
            index = PlayPenCoordinate.ITEM_INDEX_NONE;
            sect = null;
            item = null;
        } else if (p.y <= fontHeight) {
            logger.debug("y<=fontHeight = "+fontHeight); //$NON-NLS-1$
            index = PlayPenCoordinate.ITEM_INDEX_TITLE;
            sect = null;
            item = null;
        } else if (toSectionLocation(p, false) != null) {
            sect = toSectionLocation(p, true);
            logger.debug("Y is: " + p.y + " In section: " + sect.getTitle());

            // Adjustment for all cases: we're selecting over font area, not from the baseline
            int adjustment = -descent;

            // sections after the first one have some extra space on top
            if (sect != olapPane.getSections().get(0)) {
                adjustment -= INTER_SECTION_GAP / 2;
            }
            
            // if there is a title, we have to adjust for that
            if (sect.getTitle() != null) {
                if (p.y <= fontHeight + SECTION_HEADER_VGAP) {
                    PlayPenCoordinate<T, C> returnVal = new PlayPenCoordinate<T, C>(olapPane, sect, PlayPenCoordinate.ITEM_INDEX_SECTION_TITLE, null);
                    logger.debug("pointToPPCoordinate returnVal is: " + returnVal);
                    return returnVal;
                } else {
                    int sectTitleHeight = fontHeight + SECTION_HEADER_VGAP;
                    adjustment -= sectTitleHeight;
                }
            }
            
            index = (p.y + adjustment) / fontHeight;
            if (index >= sect.getItems().size()) {
                index = PlayPenCoordinate.ITEM_INDEX_NONE;
                item = null;
            } else {
                item = sect.getItems().get(index);
            }

        } else {
            index = PlayPenCoordinate.ITEM_INDEX_NONE;
            sect = null;
            item = null;
        }
        PlayPenCoordinate<T, C> returnVal = new PlayPenCoordinate<T, C>(olapPane, sect, index, item);
        logger.debug("pointToPPCoordinate returnVal is: " + returnVal);
        return returnVal;
    }
    
    /**
     * Returns the index of the first item in the given section.
     * 
     * @param sect A section in this pane.
     * @return
     */
    public int firstItemIndex(PaneSection<? extends C> sect) {
        int index = 0;
        for (PaneSection<? extends C> s : olapPane.getSections()) {
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
    private int drawSection(PaneSection<? extends C> ps, Graphics2D g, OLAPPane<T, C> cp, final int startY) {
        PlayPenCoordinate<T, C> insertionPoint = olapPane.getInsertionPoint();
        if (insertionPoint == null || insertionPoint.getSection() != ps) {
            insertionPoint = null;
        }
        int width = cp.getWidth() - cp.getInsets().left - cp.getInsets().right;
        FontMetrics metrics = cp.getFontMetrics(cp.getFont());
        int fontHeight = metrics.getHeight();
        int ascent = metrics.getAscent();
        int insertionPointAdjustment = metrics.getDescent();
        
        int y = startY;
        
        int hwidth = width - cp.getMargin().right - cp.getMargin().left - BOX_LINE_THICKNESS*2;
        g.setColor(cp.getForegroundColor());
        
        if (ps.getTitle() != null) {
            if (cp.isSectionSelected(ps)) {
                if (logger.isDebugEnabled()) logger.debug("Section " + ps.getTitle() + " is selected"); //$NON-NLS-1$ //$NON-NLS-2$
                g.setColor(selectedColor);
                g.fillRect(
                        BOX_LINE_THICKNESS + cp.getMargin().left, y-ascent+fontHeight,
                        hwidth, fontHeight);
                g.setColor(cp.getForegroundColor());
            }

            g.drawString(ps.getTitle(), BOX_LINE_THICKNESS, y += fontHeight);
            y += SECTION_HEADER_VGAP;
        }

        // putting the insertion point above the section title looks dumb, so we do it here instead
        if (insertionPoint != null &&
                insertionPoint.getIndex() == PlayPenCoordinate.ITEM_INDEX_SECTION_TITLE) {
            paintInsertionPoint(g, y + insertionPointAdjustment, hwidth);
        }
        
        // print items
        int i = 0;
        for (C item : ps.getItems()) {
            if (cp.isItemSelected(item)) {
                if (logger.isDebugEnabled()) logger.debug("Item "+i+" is selected"); //$NON-NLS-1$ //$NON-NLS-2$
                g.setColor(selectedColor);
                g.fillRect(
                        BOX_LINE_THICKNESS + cp.getMargin().left, y-ascent+fontHeight,
                        hwidth, fontHeight);
                g.setColor(cp.getForegroundColor());
            }
            
            if (insertionPoint != null &&
                    insertionPoint.getIndex() == i) {
                paintInsertionPoint(g, y + insertionPointAdjustment, hwidth);
            }

            String itemName = OLAPUtil.nameFor(item);
            g.drawString(itemName == null ? "(null)" : itemName, BOX_LINE_THICKNESS +
                    cp.getMargin().left, y += fontHeight);
            i++;
        }
        
        // in case insertion point is after last item
        if (insertionPoint != null &&
                insertionPoint.getIndex() == i) {
            paintInsertionPoint(g, y + insertionPointAdjustment, hwidth);
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
    private int calculateMaxSectionWidth(PaneSection<? extends C> ps, ContainerPane<?, ?> cp) {
        int width = calculateTextWidth(cp, ps.getTitle());
        for (C oo : ps.getItems()) {
            if (oo == null) {
                logger.error("Found null column in dimension '"+cp.getName()+"'"); //$NON-NLS-1$ //$NON-NLS-2$
                throw new NullPointerException("Found null column in dimension '"+cp.getName()+"'"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            width = Math.max(width, calculateTextWidth(cp, OLAPUtil.nameFor(oo)));
        }
        return width;
    }

    private class ModelEventHandler implements SPListener {

        public void propertyChanged(PropertyChangeEvent evt) {
            logger.debug("Property Change: " +
                    evt.getPropertyName() + ": " +
                    evt.getOldValue() + " -> " + evt.getNewValue());
            if ("name".equals(evt.getPropertyName()) || "cubeName".equals(evt.getPropertyName())) {
                // note this could be the name of the cube or any of its child objects,
                // since we have property change listeners on every object in the subtree under cube
                olapPane.revalidate();
            }
        }

        public void childAdded(SPChildEvent e) {
            SQLPowerUtils.listenToHierarchy(e.getChild(), this);
            olapPane.revalidate();
        }

        public void childRemoved(SPChildEvent e) {
            SQLPowerUtils.unlistenToHierarchy(e.getChild(), this);
            olapPane.revalidate();
        }

        public void transactionEnded(TransactionEvent e) {
            //no-op            
        }

        public void transactionRollback(TransactionEvent e) {
            //no-op            
        }

        public void transactionStarted(TransactionEvent e) {
            //no-op            
        }
        
    }
    
    private class PaneEventHandler extends AbstractSPListener {

        public void propertyChanged(PropertyChangeEvent evt) {
            if ("insertionPoint".equals(evt.getPropertyName())) {
                olapPane.repaint();
            }
        }
        
    }
    
    public Point getPointForModelObject(Object modelObject) {
        return olapPane.getLocation();
    }
}
