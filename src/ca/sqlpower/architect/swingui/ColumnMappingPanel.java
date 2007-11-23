/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ca.sqlpower.architect.swingui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.swingui.DataEntryPanel;

/**
 * The ColumnMappingPanel presents a GUI for viewing and modifying
 * the column mappings between two tables.  The data model for this
 * panel is a SQLRelationship, and its ColumnMapping children represent
 * the individual mappings from one column to the other.  Don't let
 * this fool you into thinking the ColumnMappingPanel can only be
 * used to edit FK relationship mappings, though! You can make a
 * new Relationship, throw it at this editor, then examine its mappings
 * to build up any kind of mapping you want.
 */
public class ColumnMappingPanel implements DataEntryPanel {

    private static final Logger logger = Logger.getLogger(ColumnMappingPanel.class);
    
    /**
     * A JPanel subclass that draws the RHS and LHS table panes and the column
     * mapping lines between them.
     * <p>
     * XXX Hiding this panel as an inner class is probably a bit of overengineering.
     * It would probably be better to just let the ColumnMappingPanel itself extend JPanel.
     */
    private class CustomPanel extends JPanel {
        
        /**
         * If there is a drag operation currently in progress, this field
         * will be non-null.  The SQLColumn it points to is the column that
         * the handle was attached to before the user started dragging it.
         * While the drag is in progress, the {@link ColumnMappingPanel#mappings}
         * still contains the original entry as it existed before the drag started.
         * The panel's paintComponent method knows about this arrangement, and
         * draws the line attached to the dragging handle separately.
         */
        private SQLColumn draggingHandle;

        CustomPanel() {
            setBackground(Color.WHITE);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            
            lhsTable.paint(g2);

            g2.translate(rhsTable.getX(), rhsTable.getY());
            rhsTable.paint(g2);
            g2.translate(-rhsTable.getX(), -rhsTable.getY());
            
            // Now the connecting lines
            g2.translate(lhsTable.getWidth(), 0);
            try {
                SQLTable pkTable = lhsTable.getModel();
                SQLTable fkTable = rhsTable.getModel();
                int ncols = pkTable.getColumns().size();
                for (int colidx = 0; colidx < ncols; colidx++) {
                    SQLColumn pkCol = pkTable.getColumn(colidx);
                    SQLColumn fkCol = mappings.get(pkCol);
                    if (fkCol != null) {
                        int pky = lhsTable.columnIndexToCentreY(colidx);
                        int fky = rhsTable.columnIndexToCentreY(fkTable.getColumnIndex(fkCol));
                        if (logger.isDebugEnabled()) {
                            logger.debug("Drawing Line" +
                                    " from " + pkTable.getColumn(colidx).getName() + " (y=" + pky + ")" +
                                    " to " + fkCol.getName() + " (y=" + fky + ")");
                        }
                        if (fkCol == draggingHandle) {
                            g2.setColor(Color.RED);
                        } else {
                            g2.setColor(getForeground());
                        }
                        g2.drawLine(0, pky, gap - handleLength, fky);
                        g2.fillRect(gap - handleLength, fky - 1, handleLength, 3);
                    }
                }
            } catch (ArchitectException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        /**
         * Returns the SQLColumn from the FK table 
         * @param p
         * @return
         */
        public Map.Entry<SQLColumn, SQLColumn> mappingForFkHandleAt(Point p) throws ArchitectException {
            SQLTable fkTable = rhsTable.getModel();
            List<SQLColumn> fkCols = fkTable.getColumns();
            if ( (p.x < rhsTable.getX()) && (p.x > rhsTable.getX() - handleLength) ) {
                int colIdx = rhsTable.pointToColumnIndex(new Point(0, p.y - rhsTable.getY()));
                if (colIdx >= 0 && colIdx < fkCols.size()) {
                    for (Map.Entry<SQLColumn, SQLColumn> entry : mappings.entrySet()) {
                        if (entry.getValue() == fkCols.get(colIdx)) {
                            return entry;
                        }
                    }
                }
            }
            return null;
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(
                    rhsTable.getX() + rhsTable.getWidth(),
                    Math.max(lhsTable.getHeight(), rhsTable.getHeight()));
        }
        
        public void setDraggingHandle(SQLColumn newHandle) {
            if (newHandle != draggingHandle) {
                draggingHandle = newHandle;
                logger.debug("DraggingHandle changed to " + draggingHandle);
                repaint();
            }
        }
    }
    
    /**
     * Reacts to mouse events on the custom panel.
     */
    private class MouseHandler implements MouseListener, MouseMotionListener {

        public void mouseClicked(MouseEvent e) {
            // TODO Auto-generated method stub
            
        }

        public void mouseEntered(MouseEvent e) {
            // TODO Auto-generated method stub
            
        }

        public void mouseExited(MouseEvent e) {
            panel.setDraggingHandle(null);
        }

        public void mousePressed(MouseEvent e) {
            // TODO Auto-generated method stub
            
        }

        public void mouseReleased(MouseEvent e) {
            // TODO Auto-generated method stub
            
        }

        public void mouseDragged(MouseEvent e) {
            // TODO Auto-generated method stub
            
        }

        public void mouseMoved(MouseEvent e) {
            try {
                Map.Entry<SQLColumn, SQLColumn> entry = panel.mappingForFkHandleAt(e.getPoint());
                if (entry != null) {
                    panel.setDraggingHandle(entry.getValue());
                } else {
                    panel.setDraggingHandle(null);
                }
            } catch (ArchitectException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    /**
     * The relationship we are editing.
     */
    private final SQLRelationship r;

    /**
     * The table on the left-hand side of the mapping panel.
     */
    private final TablePane lhsTable;
    
    /**
     * The table on the right-hand side of the mapping panel.
     */
    private final TablePane rhsTable;
    
    /**
     * The number of pixels of gap to leave between the two tables (this is
     * where the connecting lines will be drawn).
     */
    private int gap = 100;
    
    /**
     * The length of the handle, in pixels, that can be picked up and moved to
     * modify a column mapping.
     */
    private int handleLength = 20;
    
    /**
     * The panel that contains the GUI.
     */
    private final CustomPanel panel = new CustomPanel();

    /**
     * The current mappings within the editor.  This map can be populated from
     * the SQLRelationship with {@link #updateMappingsFromRelationship()}, and
     * the SQLRelationship's mappings can be repopulated with
     * {@link #updateRelationshipFromMappings()}.
     */
    private Map<SQLColumn, SQLColumn> mappings = new HashMap<SQLColumn, SQLColumn>();
    
    public ColumnMappingPanel(ArchitectSwingSession session, SQLRelationship r) {
        this.r = r;
        PlayPen pp = new PlayPen(session);
        lhsTable = new TablePane(r.getPkTable(), pp);
        rhsTable = new TablePane(r.getFkTable(), pp);
        lhsTable.setLocation(0, 0);
        rhsTable.setLocation(lhsTable.getWidth() + gap, 0);
        updateMappingsFromRelationship();
        MouseHandler mouseHandler = new MouseHandler();
        panel.addMouseListener(mouseHandler);
        panel.addMouseMotionListener(mouseHandler);
    }
    
    /**
     * Updates the ColumnMapping children in {@link #r} to match those in this
     * panel's internal representation of the mappings.
     */
    public void updateRelationshipFromMappings() throws ArchitectException {
        while (r.getChildren().size() > 0) {
            r.removeChild(r.getChildren().size() - 1);
        }
        for (Map.Entry<SQLColumn, SQLColumn> entry : mappings.entrySet()) {
            r.addMapping(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Updates this panel's internal representation of the mappings to
     * match those in {@link #r}.
     */
    public void updateMappingsFromRelationship() {
        mappings = new HashMap<SQLColumn, SQLColumn>();
        for (SQLRelationship.ColumnMapping cm : r.getMappings()) {
            mappings.put(cm.getPkColumn(), cm.getFkColumn());
        }
        panel.repaint();
    }
    
    public boolean applyChanges() {
        try {
            updateRelationshipFromMappings();
        } catch (ArchitectException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public void discardChanges() {
        // do nothing
    }

    public JComponent getPanel() {
        return panel;
    }

    public boolean hasUnsavedChanges() {
        // TODO implement this
        return false;
    }

}
