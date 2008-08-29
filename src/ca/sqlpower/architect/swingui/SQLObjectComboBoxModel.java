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

import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;

/**
 * Combo box model that presents the list of a SQLObject's children as the
 * items. It is "live" in that its contents track the object's children in
 * real time.
 */
public class SQLObjectComboBoxModel implements ComboBoxModel {

    private final SQLObject parent;

    private SQLObject selectedItem;
    
    private final List<ListDataListener> listDataListeners = new ArrayList<ListDataListener>();
    
    public SQLObjectComboBoxModel(SQLObject parent) {
        this.parent = parent;
        if (!parent.allowsChildren()) {
            throw new IllegalArgumentException("That parent object doesn't allow children");
        }
        parent.addSQLObjectListener(childEventHandler);
    }

    public Object getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(Object anItem) {
        selectedItem = (SQLObject) anItem;
    }

    public Object getElementAt(int index) {
        try {
            // swing wants us to shrug and return null if the index is out of bounds.
            if (index < 0 || index > parent.getChildCount()) {
                return null;
            } else {
                return parent.getChild(index);
            }
        } catch (ArchitectException ex) {
            throw new ArchitectRuntimeException(ex);
        }
    }

    public int getSize() {
        try {
            return parent.getChildCount();
        } catch (ArchitectException ex) {
            throw new ArchitectRuntimeException(ex);
        }
    }

    public void addListDataListener(ListDataListener l) {
        listDataListeners.add(l);
    }
    
    public void removeListDataListener(ListDataListener l) {
        listDataListeners.remove(l);
    }
    
    private void fireListDataEvent(int type, int index0, int index1) {
        ListDataEvent e = new ListDataEvent(this, type, index0, index1);
        for (int i = listDataListeners.size() - 1; i >= 0; i--) {
            listDataListeners.get(i).contentsChanged(e);
        }
    }
    
    private SQLObjectListener childEventHandler = new SQLObjectListener() {

        public void dbChildrenInserted(SQLObjectEvent e) {
            for (int i : e.getChangedIndices()) {
                fireListDataEvent(ListDataEvent.INTERVAL_ADDED, i, i);
            }
        }

        public void dbChildrenRemoved(SQLObjectEvent e) {
            for (int i : e.getChangedIndices()) {
                fireListDataEvent(ListDataEvent.INTERVAL_REMOVED, i, i);
            }
        }

        public void dbObjectChanged(SQLObjectEvent e) {
            // doesn't matter
        }

        public void dbStructureChanged(SQLObjectEvent e) {
            // I don't think these happen
            fireListDataEvent(ListDataEvent.CONTENTS_CHANGED, 0, 0);
        }
        
    };
}
