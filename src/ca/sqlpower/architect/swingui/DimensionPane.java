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
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;

public class DimensionPane extends PlayPenComponent implements Serializable, Selectable {
    
    /**
     * A special column index that represents the titlebar area.
     */
    public static final int COLUMN_INDEX_TITLE = -1;

    /**
     * A special column index that means "no location."
     */
    public static final int COLUMN_INDEX_NONE = -2;
    
    private String dimensionName;
    
    private SQLTable dummyTable;
    
    private List<SQLColumn> columns;
    
    protected Set<SQLColumn> selectedColumns;

    private boolean selected;

    public DimensionPane(String dimensionName, SQLTable m, PlayPenContentPane parent) {
        super(parent);
        this.backgroundColor = new Color(240, 240, 240);
        this.foregroundColor = Color.BLACK;
        this.selectedColumns = new HashSet<SQLColumn>();
        setOpaque(true);
        
        this.dimensionName = dimensionName;
        dummyTable = m;
        try {
            columns = m.getColumns();
        } catch (ArchitectException e) {
        }
        
        updateUI();
    }

    @Override
    public Object getModel() {
        return null;
    }
    
    /**
     * See {@link #selected}.
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * See {@link #selected}.
     */
    public void setSelected(boolean isSelected, int multiSelectType) {
        if (isSelected == false) {
            selectNone();
        }
        if (selected != isSelected) {
            selected = isSelected;
            fireSelectionEvent(new SelectionEvent(this, selected ? SelectionEvent.SELECTION_EVENT : SelectionEvent.DESELECTION_EVENT, multiSelectType));
            repaint();
        }
    }

    public void selectNone() {
        selectedColumns.clear();
        repaint();
    }

    /**
     * De-selects all columns in this dimensionPane.
     */
    public void deselectColumn(int i) {
        if (i < 0) {
            selectNone();
            return;
        }
        selectedColumns.remove(columns.get(i));
        repaint();
    }

    /**
     * @param i The column to deselect.  If less than 0, {@link
     * #selectNone()} is called.
     */
    public void selectColumn(int i) {
        if (i < 0) {
            selectNone();
            return;
        }
        selectedColumns.add(columns.get(i));
        repaint();
    }

    /**
     * return true if the column in dimensionPane is selected
     * @param i column index
     * @return true if the column in dimensionPane is selected
     */
    public boolean isColumnSelected(int i) {
        return selectedColumns.contains(columns.get(i));
    }
    
    /**
     * Returns the list of selected column(s).
     */
    public List<SQLColumn> getSelectedColumns() {
        List<SQLColumn> selectedColumns = new ArrayList<SQLColumn>();
        for (int i=0; i < columns.size(); i++) {
            if (isColumnSelected(i)) {
                selectedColumns.add(columns.get(i));
            }
        }
        return selectedColumns;
    }
    
    /**
     * Returns the index of the column that point p is on top of.  If
     * p is on top of the table name, returns COLUMN_INDEX_TITLE.
     * Otherwise, p is not over a column or title and the returned
     * index is COLUMN_INDEX_NONE.
     */
    public int pointToColumnIndex(Point p) {
        return ((DimensionPaneUI) getUI()).pointToColumnIndex(p);
    }
    
    // --------------------- SELECTION EVENT SUPPORT ---------------------

    protected List<SelectionListener> selectionListeners = new LinkedList<SelectionListener>();

    public void addSelectionListener(SelectionListener l) {
        selectionListeners.add(l);
    }

    public void removeSelectionListener(SelectionListener l) {
        selectionListeners.remove(l);
    }

    protected void fireSelectionEvent(SelectionEvent e) {
        Iterator<SelectionListener> it = selectionListeners.iterator();
        if (e.getType() == SelectionEvent.SELECTION_EVENT) {
            while (it.hasNext()) {
                it.next().itemSelected(e);
            }
        } else if (e.getType() == SelectionEvent.DESELECTION_EVENT) {
            while (it.hasNext()) {
                it.next().itemDeselected(e);
            }
        } else {
            throw new IllegalStateException("Unknown selection event type "+e.getType());
        }
    }
    
    // ---------------------- PlayPenComponent Overrides ----------------------
    // see also PlayPenComponent

    public void updateUI() {
        DimensionPaneUI ui = (DimensionPaneUI) BasicDimensionPaneUI.createUI(this);
        ui.installUI(this);
        setUI(ui);
    }

    public String getUIClassID() {
        return DimensionPaneUI.UI_CLASS_ID;
    }

    public SQLTable getDummyTable() {
        return dummyTable;
    }

    public List<SQLColumn> getColumns() {
        return columns;
    }

    public String getDimensionName() {
        return dimensionName;
    }

}
