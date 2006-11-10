package ca.sqlpower.architect.swingui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.DatabaseListChangeEvent;
import ca.sqlpower.architect.DatabaseListChangeListener;
import ca.sqlpower.architect.PlDotIni;

public class ConnectionComboBoxModel implements ComboBoxModel, DatabaseListChangeListener {

    ArchitectDataSource selectedItem;

    List<ArchitectDataSource> connections;

    List<ListDataListener> listenerList;

    PlDotIni plini;

    /**
     * Setup a new connection combo box model with the conections found in the
     * PPLDotIni.
     */
    public ConnectionComboBoxModel() {
        this(ArchitectFrame.getMainInstance().getUserSettings().getPlDotIni());
    }

    /**
     * Setup a new connection combo box model with the conections found in the
     * PPLDotIni
     */
    public ConnectionComboBoxModel(PlDotIni plini) {
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
        System.out.println("warning: set selected item:" + anItem);
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
