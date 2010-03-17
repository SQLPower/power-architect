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

import ca.sqlpower.object.AbstractPoolingSPListener;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.sqlobject.SQLObject;


/**
 * Combo box model that presents the list of a SQLObject's children as the
 * items. It is "live" in that its contents track the object's children in
 * real time.
 */
public class SQLObjectComboBoxModel implements ComboBoxModel {

    private final SQLObject parent;
    
    private final Class<? extends SQLObject> childType;

    private SQLObject selectedItem;
    
    private final List<ListDataListener> listDataListeners = new ArrayList<ListDataListener>();
    
    public SQLObjectComboBoxModel(SQLObject parent, Class<? extends SQLObject> childType) {
        this.parent = parent;
        this.childType = childType;
        if (!parent.allowsChildren()) {
            throw new IllegalArgumentException("That parent object doesn't allow children");
        }
        parent.addSPListener(childEventHandler);
    }

    public Object getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(Object anItem) {
        selectedItem = (SQLObject) anItem;
    }

    public Object getElementAt(int index) {
        // swing wants us to shrug and return null if the index is out of bounds.
        if (index < 0 || index > parent.getChildren(childType).size()) {
            return null;
        } else {
            return parent.getChildren(childType).get(index);
        }
    }

    public int getSize() {
        return parent.getChildren(childType).size();
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
    
    private SPListener childEventHandler = new AbstractPoolingSPListener() {

        public void childAddedImpl(SPChildEvent e) {
            fireListDataEvent(ListDataEvent.INTERVAL_ADDED, e.getIndex(), e.getIndex());
        }

        public void childRemovedImpl(SPChildEvent e) {
            int changedIndex = e.getIndex();
            if (selectedItem == e.getChild()) {
                setSelectedItem(null);
            }
            fireListDataEvent(ListDataEvent.INTERVAL_REMOVED, changedIndex, changedIndex);
        }

    };
}
