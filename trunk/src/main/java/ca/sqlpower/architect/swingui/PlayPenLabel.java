/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of SQL Power Architect.
 *
 * SQL Power Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Architect is distributed in the hope that it will be useful,
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
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPLabel;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.ConstructorParameter.ParameterType;
import ca.sqlpower.object.annotation.NonBound;
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.swingui.ColourScheme;

public class PlayPenLabel extends DraggablePlayPenComponent {

    public static final List<Class<? extends SPObject>> allowedChildTypes = 
        Collections.<Class<? extends SPObject>>singletonList(SPLabel.class);
    
    private final class LabelUI implements PlayPenComponentUI {
        
        Insets insets = new Insets(1, 3, 3, 3);
        
        @Override
        public boolean contains(Point p) {
            return getBounds().contains(p);
        }

        @Override
        public Point getPointForModelObject(Object modelObject) {
            return getLocation();
        }

        @Override
        public Dimension getPreferredSize() {
            if (label.getText() == null) {
                return new Dimension(0, 0);
            }
            String[] textToRender = label.getText().split("\n");
            FontMetrics fm = getPlayPen().getFontMetrics(getPlayPen().getFont());
            int textHeight = fm.getHeight() * textToRender.length;
            int textWidth = 0;
            for (String line : textToRender) {
                int lineWidth = fm.stringWidth(line);
                if (lineWidth > textWidth) textWidth = lineWidth;
            }
            return new Dimension(textWidth + insets.left + insets.right, textHeight + insets.top + insets.bottom);
        }

        @Override
        public void installUI(PlayPenComponent c) {
            // no-op
        }

        @Override
        public void paint(Graphics2D g2) {
            if (label.getBackgroundColour() != null) {
                g2.setColor(label.getBackgroundColour());
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            
            if (isSelected()) {
                g2.setColor(ColourScheme.SQLPOWER_ORANGE);
            } else {
                g2.setColor(label.getBorderColour());
            }
            g2.drawRect(0, 0, getWidth(), getHeight());
            g2.translate(insets.left, insets.top);
            g2.setColor(Color.BLACK);
            
            if (label.getText() == null) {
                return;
            }
            String[] textToRender = label.getText().split("\n");
            g2.setFont(getPlayPen().getFont());
            FontMetrics fm = g2.getFontMetrics();
            int textHeight = fm.getHeight() * textToRender.length;
            
            if (label.getForegroundColour() == null) {
                g2.setColor(Color.BLACK);
            } else {
                g2.setColor(label.getForegroundColour());
            }
            
            double y = label.getVerticalAlignment().calculateStartY(getHeight(), textHeight, fm);
            for (String text : textToRender) {
                int textWidth = (int) fm.getStringBounds(text, g2).getWidth();
                double x = label.getHorizontalAlignment().computeStartX(getWidth() - (insets.left+insets.right), textWidth);
                g2.drawString(text, (int)x, (int)y);
                y += fm.getHeight();
            }
        }

        @Override
        public void revalidate() {
            // no-op
        }

        @Override
        public void uninstallUI(PlayPenComponent c) {
            // no-op
        }
    }

    private final SPLabel label;
    
    @Constructor
    public PlayPenLabel(
            @ConstructorParameter(parameterType = ParameterType.CHILD, propertyName = "label") SPLabel label,
            @ConstructorParameter(propertyName = "name") String name) {
        super(name);
        label.setParent(this);
        this.label = label;
        label.addSPListener(new AbstractSPListener() {
            @Override
            public void propertyChanged(PropertyChangeEvent evt) {
                revalidate();
            }
        });
    }
    
    @Override
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }
    
    @Override
    public List<SPLabel> getChildren() {
        return Collections.singletonList(label);
    }
    
    @Override
    public Object getModel() {
        return null;
    }

    @Override
    public String getModelName() {
        return null;
    }

    @Override
    public void handleMouseEvent(MouseEvent evt) {
        PlayPen pp = getPlayPen();

        Point p = evt.getPoint();
        pp.unzoomPoint(p);
        p.translate(-getX(), -getY());
        if (evt.getID() == MouseEvent.MOUSE_CLICKED) {
            if (evt.getClickCount() == 1 && evt.getButton() == MouseEvent.BUTTON1) {
               if (!evt.isControlDown()) {
                   pp.selectNone();
               }
            }
            
            if ((evt.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
                if (evt.getClickCount() == 2) { // double click
                    if (isSelected()) {
                        ArchitectFrame af = pp.getSession().getArchitectFrame();
                        af.getEditLabelAction().actionPerformed
                        (new ActionEvent(this, ActionEvent.ACTION_PERFORMED, PlayPen.ACTION_COMMAND_SRC_PLAYPEN));
                    }
                } else {
                    setSelected(true, SelectionEvent.SINGLE_SELECT);
                }
            }
        } else if (evt.getID() == MouseEvent.MOUSE_PRESSED) {
            if (isSelected()){
                componentPreviouslySelected = true;
            } else {
                if (!evt.isControlDown()) {
                    pp.selectNone();
                }
                
                componentPreviouslySelected = false;
                setSelected(true, SelectionEvent.SINGLE_SELECT);
            }
            
            if (!pp.getSession().getArchitectFrame().createRelationshipIsActive()) {
                setupDrag(p);
            }
        } else if (evt.getID() == MouseEvent.MOUSE_MOVED || evt.getID() == MouseEvent.MOUSE_DRAGGED) {
            setSelected(pp.rubberBand.intersects(getBounds(new Rectangle())),SelectionEvent.SINGLE_SELECT);
        }
    }

    @Override
    public PlayPenComponentUI getUI() {
        return new LabelUI();
    }
    
    @NonBound
    public Font getFont() {
        Font font = label.getFont();
        if (font != null) {
            return font;
        } else {
            return getPlayPen().getFont();
        }
    }
    
    @NonProperty
    public SPLabel getLabel() {
        return label;
    }
    
    

}
