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

import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.DataSourceCollection;
import ca.sqlpower.architect.DatabaseListChangeEvent;
import ca.sqlpower.architect.DatabaseListChangeListener;

public class ConnectionComboBoxModel implements ComboBoxModel, DatabaseListChangeListener {

    private static final Logger logger = Logger.getLogger(ConnectionComboBoxModel.class); 
    ArchitectDataSource selectedItem;

    List<ArchitectDataSource> connections;

    List<ListDataListener> listenerList;

    DataSourceCollection plini;

    /**
     * Setup a new connection combo box model with the conections found in the
     * PPLDotIni
     */
    public ConnectionComboBoxModel(DataSourceCollection plini) {
        this.plini = plini;
        listenerList = new ArrayList<ListDataListener>();
        connections = plini.getConnections();
        plini.addDatabaseListChangeListener(this);
    }

    public void cleanup() {
        plini.removeDatabaseListChangeListener(this);
    }

    public void setSelectedItem(Object anItem) {
        int selectedIndex = connections.indexOf(anItem);
        if (selectedIndex >= 0) {
            if (anItem instanceof ArchitectDataSource) {
                selectedItem = (ArchitectDataSource) anItem;
            } else if (anItem == null) {
                selectedItem = null;
            }
            fireContentChangedEvent(selectedIndex);
        }
    }

    public void setSelectedItem(String anItem) {
        for (ArchitectDataSource ds : connections) {
            if (ds.getName().equals(anItem)) {
                selectedItem = ds;
                setSelectedItem(selectedItem);
                return;
            }
        }
        logger.debug("warning: set selected item:" + anItem);
    }

    public Object getSelectedItem() {
        return selectedItem;
    }

    public int getSize() {
        return connections.size() + 1;
    }

    public Object getElementAt(int index) {
        if (index == 0) {
            return null;
        }
        return connections.get(index - 1);
    }

    public void addListDataListener(ListDataListener l) {
        listenerList.add(l);
    }

    public void removeListDataListener(ListDataListener l) {
        listenerList.remove(l);
    }

    private void fireContentChangedEvent(int index) {

        for (int i = listenerList.size() - 1; i >= 0; i--) {
            listenerList.get(i).contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index, index));
        }
    }

    public void databaseAdded(DatabaseListChangeEvent e) {
        connections = plini.getConnections();
        for (int i = listenerList.size() - 1; i >= 0; i--) {
            listenerList.get(i).contentsChanged(
                    new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, e.getListIndex(), e.getListIndex()));
        }

    }

    public void databaseRemoved(DatabaseListChangeEvent e) {
        connections = plini.getConnections();
        for (int i = listenerList.size() - 1; i >= 0; i--) {
            listenerList.get(i).contentsChanged(
                    new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, e.getListIndex(), e.getListIndex()));
        }
    }

}
