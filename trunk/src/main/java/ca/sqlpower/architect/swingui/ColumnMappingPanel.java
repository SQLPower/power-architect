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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLRelationship.ColumnMapping;
import ca.sqlpower.sqlobject.SQLRelationship.SQLImportedKey;
import ca.sqlpower.swingui.ChangeListeningDataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelChangeUtil;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;

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
public class ColumnMappingPanel extends ChangeListeningDataEntryPanel implements SPListener {

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

        /**
         * This is the place where the user has most recently dragged the dragging
         * handle to.  The handle may not be painted at exactly this location, because
         * it visually snaps into place when releasing the mouse button would result in
         * a new mapping being formed.
         */
        private Point draggingPoint;
        
        CustomPanel() {
            setBackground(Color.WHITE);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            boolean antialias = session.getContext().getUserSettings().getSwingSettings().getBoolean(ArchitectSwingUserSettings.PLAYPEN_RENDER_ANTIALIASED, false);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
            
            super.paintComponent(g2);
            
            lhsTable.paint(g2);

            g2.translate(rhsTable.getX(), rhsTable.getY());
            rhsTable.paint(g2);
            g2.translate(-rhsTable.getX(), -rhsTable.getY());
            
            // Now the connecting lines
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
                            logger.debug("Drawing Line" + //$NON-NLS-1$
                                    " from " + pkTable.getColumn(colidx).getName() + " (y=" + pky + ")" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    " to " + fkCol.getName() + " (y=" + fky + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        }
                        if (fkCol == draggingHandle) {
                            Point offsetDragPoint = new Point(draggingPoint.x - handleLength / 2, draggingPoint.y);
                            g2.setColor(Color.RED);
                            g2.drawLine(lhsTable.getX() + lhsTable.getWidth(), lhsTable.getY() + pky,
                                        offsetDragPoint.x, offsetDragPoint.y);
                            g2.fillRect(offsetDragPoint.x, offsetDragPoint.y - 1, handleLength, 3);
                            g2.setColor(getForeground());
                        } else {
                            g2.drawLine(lhsTable.getX() + lhsTable.getWidth(), lhsTable.getY() + pky,
                                        rhsTable.getX() - handleLength, rhsTable.getY() + fky);
                            g2.fillRect(rhsTable.getX() - handleLength, rhsTable.getY() + fky - 1,
                                        handleLength, 3);
                        }
                    }
                }
            } catch (SQLObjectException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        /**
         * Returns the SQLColumn from the FK table 
         * @param p
         * @return
         */
        public Map.Entry<SQLColumn, SQLColumn> mappingForFkHandleAt(Point p) throws SQLObjectException {
            SQLTable fkTable = rhsTable.getModel();
            List<SQLColumn> fkCols = fkTable.getColumns();
            if ( (p.x < rhsTable.getX()) && (p.x > rhsTable.getX() - handleLength) ) {
                int colIdx = rhsTable.pointToItemIndex(new Point(0, p.y - rhsTable.getY()));
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
                logger.debug("DraggingHandle changed to " + draggingHandle); //$NON-NLS-1$
                repaint();
            }
        }
        
        public SQLColumn getDraggingHandle() {
            return draggingHandle;
        }

        public void setDraggingPoint(Point point) {
            draggingPoint = point;
            repaint();
        }
    }
    
    /**
     * Reacts to mouse events on the custom panel.
     */
    private class MouseHandler implements MouseListener, MouseMotionListener {

        public void mouseClicked(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }

        public void mousePressed(MouseEvent e) {
            try {
                Map.Entry<SQLColumn, SQLColumn> entry = panel.mappingForFkHandleAt(e.getPoint());
                if (entry != null) {
                    panel.setDraggingHandle(entry.getValue());
                    panel.setDraggingPoint(e.getPoint());
                } else {
                    panel.setDraggingHandle(null);
                    panel.setDraggingPoint(null);
                }
            } catch (SQLObjectException ex) {
                throw new RuntimeException(ex);
            }
        }

        public void mouseReleased(MouseEvent e) {
            try {
                if (panel.getDraggingHandle() == null) return;
                SQLColumn oldFkCol = panel.getDraggingHandle();
                Point p = e.getPoint();
                int newFkColIdx = rhsTable.pointToItemIndex(new Point(0, p.y));
                if (newFkColIdx >= 0 && newFkColIdx < rhsTable.getModel().getColumns().size()) {
                    SQLColumn newFkCol = rhsTable.getModel().getColumn(newFkColIdx);
                    
                    if (mappings.containsValue(newFkCol)) {
                        return;
                    }
                    
                    // XXX should hang onto pkcol too so we don't need this reverse lookup
                    Map.Entry<SQLColumn, SQLColumn> oldEntry = null;
                    for (Map.Entry<SQLColumn, SQLColumn> entry : mappings.entrySet()) {
                        if (entry.getValue() == oldFkCol) {
                            oldEntry = entry;
                            break;
                        }
                    }
                    
                    if (oldEntry == null) {
                        throw new IllegalStateException("Couldn't find existing mapping at end of drag operation!"); //$NON-NLS-1$
                    }
                    SQLColumn pkCol = oldEntry.getKey();
                    mappings.remove(pkCol);
                    mappings.put(pkCol, newFkCol);
                    modified = true;
                }
            } catch (SQLObjectException ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.setDraggingHandle(null);
                panel.setDraggingPoint(null);
            }
        }

        public void mouseDragged(MouseEvent e) {
            if (panel.getDraggingHandle() != null) {
                panel.setDraggingPoint(e.getPoint());
            }
        }

        public void mouseMoved(MouseEvent e) { }
    }
    
    /**
     * The session this panel belongs to.
     */
    private final ArchitectSwingSession session;

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
    
    private final JScrollPane scrollPane;

    /**
     * Tracks whether or not any edits have been made on this panel.
     * True means there have been changes made to the mappings.
     */
    private boolean modified = false;
    
    /**
     * The current mappings within the editor.  This map can be populated from
     * the SQLRelationship with {@link #updateMappingsFromRelationship()}, and
     * the SQLRelationship's mappings can be repopulated with
     * {@link #updateRelationshipFromMappings()}.
     */
    private Map<SQLColumn, SQLColumn> mappings = new HashMap<SQLColumn, SQLColumn>();

    /**
     * The colour with which to highlight the columns of the RHS table
     * that are involved in relationships other than the one we're currently
     * editing.
     */
    private Color otherRelColour = Color.RED;

    public ColumnMappingPanel(ArchitectSwingSession session, SQLRelationship r) {
        this.session = session;
        this.r = r;
        PlayPen pp = new PlayPen(session);
        lhsTable = new TablePane(r.getPkTable(), pp.getContentPane());
        rhsTable = new TablePane(r.getFkTable(), pp.getContentPane());
        pp.getContentPane().addChild(lhsTable, 0);
        pp.getContentPane().addChild(rhsTable, 0);
        SQLPowerUtils.listenToHierarchy(lhsTable.getModel().getParent(), this);
        
        // The playpen constructor hooks the playpen in as a hierarchy listener
        // on the entire SQLObject tree.  Since we're not even using the playpen,
        // we'll destroy it now instead of waiting until cleanup().
        pp.destroy();
        
        lhsTable.setLocation(1, 1);
        rhsTable.setLocation(lhsTable.getWidth() + gap, 1);
        updateMappingsFromRelationship();        
        MouseHandler mouseHandler = new MouseHandler();
        panel.addMouseListener(mouseHandler);
        panel.addMouseMotionListener(mouseHandler);
        scrollPane = new JScrollPane(panel);
        
        colourOtherRelationships();
    }
    
    /**
     * Colours columns of the RHS table's table pane if they are involved
     * in other relationships.  This should help people when deciding if
     * they should use a particular column in the current relationship mapping.
     */
    private void colourOtherRelationships() {
        try {
            SQLTable t = rhsTable.getModel();
            for (SQLImportedKey k : t.getImportedKeys()) {
                SQLRelationship r = k.getRelationship();
                if (r == this.r) continue;
                for (SQLRelationship.ColumnMapping cm : r.getChildren(
                        SQLRelationship.ColumnMapping.class)) {
                    rhsTable.addColumnHighlight(cm.getFkColumn(), otherRelColour);
                }
            }
        } catch (SQLObjectException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Updates the ColumnMapping children in {@link #r} to match those in this
     * panel's internal representation of the mappings.
     */
    private void updateRelationshipFromMappings() throws SQLObjectException {
        try {
            r.begin("Modify Column Mappings"); //$NON-NLS-1$
            logger.debug("Removing all mappings from relationship..."); //$NON-NLS-1$
            for (int i = r.getChildren(ColumnMapping.class).size() - 1; i >= 0; i--) {
                r.removeChild(r.getChildren(ColumnMapping.class).get(i));
            }
            for (Map.Entry<SQLColumn, SQLColumn> entry : mappings.entrySet()) {
                logger.debug("Adding mapping " + entry.getKey() + " -> " + entry.getValue()); //$NON-NLS-1$ //$NON-NLS-2$
                r.addMapping(entry.getKey(), entry.getValue());
            }
            r.commit();
        } catch (IllegalArgumentException e) {
            r.rollback("Could not remove mappings: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (ObjectDependentException e) {
            r.rollback("Could not remove mappings: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Updates this panel's internal representation of the mappings to
     * match those in {@link #r}.
     */
    private void updateMappingsFromRelationship() {
        mappings = new HashMap<SQLColumn, SQLColumn>();
        for (SQLRelationship.ColumnMapping cm : r.getChildren(
                SQLRelationship.ColumnMapping.class)) {
            mappings.put(cm.getPkColumn(), cm.getFkColumn());
        }
        panel.repaint();
    }
    
    public boolean applyChanges() {
        try {
            cleanup();
            updateRelationshipFromMappings();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public void discardChanges() {
        try {
            cleanup();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public JComponent getPanel() {
        return scrollPane;
    }

    public boolean hasUnsavedChanges() {
        return modified;
    }

    /**
     * Detaches listeners from the SQLTable and SQLRelationship that were added
     * during the creation of this instance.
     * @throws ObjectDependentException 
     * @throws IllegalArgumentException 
     */
    private void cleanup() throws IllegalArgumentException, ObjectDependentException {
        SQLPowerUtils.unlistenToHierarchy(lhsTable.getModel().getParent(), this);
        lhsTable.getParent().removeChild(lhsTable);
        rhsTable.getParent().removeChild(rhsTable);
    }

    public void childAdded(SPChildEvent e) {
        panel.repaint();
        if (e.getSource() instanceof SQLTable && 
                (e.getChild() instanceof SQLRelationship || e.getChild() instanceof SQLImportedKey)) {
            TablePane tp;
            if (lhsTable.getModel() == e.getSource()) tp = rhsTable;
            else if (rhsTable.getModel() == e.getSource()) tp = lhsTable;
            else return;
            updateMappingsFromRelationship();
            tp.setBackgroundColor(DataEntryPanelChangeUtil.NONCONFLICTING_COLOR);
            setErrorText(DataEntryPanelChangeUtil.ERROR_MESSAGE);
        } else if (e.getChild() instanceof ColumnMapping) {
            ColumnMapping cm = (ColumnMapping) e.getChild();
            for (TablePane tp : new TablePane[] {lhsTable, rhsTable}) {                    
                for (SQLColumn c : new SQLColumn[] {cm.getPkColumn(), cm.getFkColumn()}) {
                    try {
                        if (tp.getModel().getColumns().contains(c)) {
                            updateMappingsFromRelationship();
                            tp.addColumnHighlight(c, DataEntryPanelChangeUtil.DARK_NONCONFLICTING_COLOR);
                            setErrorText(DataEntryPanelChangeUtil.ERROR_MESSAGE);
                        }
                    } catch (SQLObjectException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
    }

    public void childRemoved(SPChildEvent e) {
        panel.repaint();
        if (e.getSource() instanceof SQLTable && 
                (e.getChild() instanceof SQLRelationship || e.getChild() instanceof SQLImportedKey)) {
            TablePane tp;
            if (lhsTable.getModel() == e.getSource()) tp = rhsTable;
            else if (rhsTable.getModel() == e.getSource()) tp = lhsTable;
            else return;
            updateMappingsFromRelationship();
            tp.setBackgroundColor(DataEntryPanelChangeUtil.NONCONFLICTING_COLOR);
            setErrorText(DataEntryPanelChangeUtil.ERROR_MESSAGE);
        } else if (e.getChild() instanceof ColumnMapping) {
            ColumnMapping cm = (ColumnMapping) e.getChild();
            for (TablePane tp : new TablePane[] {lhsTable, rhsTable}) {                    
                for (SQLColumn c : new SQLColumn[] {cm.getPkColumn(), cm.getFkColumn()}) {
                    try {
                        if (tp.getModel().getColumns().contains(c)) {
                            updateMappingsFromRelationship();
                            tp.addColumnHighlight(c, DataEntryPanelChangeUtil.DARK_NONCONFLICTING_COLOR);
                            setErrorText(DataEntryPanelChangeUtil.ERROR_MESSAGE);
                        }
                    } catch (SQLObjectException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
    }

    public void propertyChanged(PropertyChangeEvent e) {
        panel.repaint();      
    }

    public void transactionEnded(TransactionEvent e) {
        // no-op
    }

    public void transactionRollback(TransactionEvent e) {
        // no-op
    }

    public void transactionStarted(TransactionEvent e) {
        // no-op
    }
}
