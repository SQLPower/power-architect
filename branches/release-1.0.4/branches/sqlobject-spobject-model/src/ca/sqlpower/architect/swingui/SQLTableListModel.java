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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Logger;

import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;

public class SQLTableListModel extends AbstractSPListener implements ListModel {

	private static final Logger logger = Logger.getLogger(SQLTableListModel.class);

	protected SQLTable table;
	protected ArrayList listeners;

	public SQLTableListModel(SQLTable table) {
		this.table = table;
		this.listeners = new ArrayList();
	}


	// --------------- ListModel INTERFACE --------------------

	/**
	 * @throws RuntimeException if an ArchitectException occurs.
	 */
	public int getSize() {
		try {
			return table.getColumns().size();
		} catch (SQLObjectException ex) {
			throw new RuntimeException("Couldn't get child count", ex); //$NON-NLS-1$
		}
	}

	/**
	 * @throws RuntimeException if an ArchitectException occurs.
	 */
	public Object getElementAt(int index) {
		try {
			return table.getColumns().get(index);
		} catch (SQLObjectException ex) {
			throw new RuntimeException("Couldn't get child "+index, ex); //$NON-NLS-1$
		}
	}

	public void addListDataListener(ListDataListener l) {
		listeners.add(l);
	}

	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);
	}


	// -------------- UTILITY METHODS ------------------

	protected void fireContentsChanged(int index0, int index1) {
		ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index0, index1);
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			((ListDataListener) it.next()).contentsChanged(e);
		}
	}

	protected void fireIntervalAdded(int index0, int index1) {
		ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index0, index1);
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			((ListDataListener) it.next()).intervalAdded(e);
		}
	}

	protected void fireIntervalRemoved(int index0, int index1) {
		ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index0, index1);
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			((ListDataListener) it.next()).intervalRemoved(e);
		}
	}


	// -------------- SPListener INTERFACE -------------------
	@Override
	public void childAddedImpl(SPChildEvent e) {
	    // XXX: should group contiguous regions into one event!
	    fireIntervalAdded(e.getIndex(), e.getIndex());
	}

	@Override
	public void childRemovedImpl(SPChildEvent e) {
	    // XXX: should group contiguous regions into one event!
	    fireIntervalRemoved(e.getIndex(), e.getIndex());
	}

	@Override
	public void propertyChangeImpl(PropertyChangeEvent e) {
		if (e.getSource() == table.getColumnsFolder()) {
		    // XXX: should group contiguous regions into one event!
		    int index = ((SPObject) e.getSource()).getChildren(((SPObject) e.getSource()).getClass()).indexOf(e.getSource());
		    logger.debug("Firing contentsChanged event for index "+index); //$NON-NLS-1$
		    fireContentsChanged(index, index);
		} else if (e.getSource() instanceof SQLColumn) {
			// make sure this column was actually in the table
			try {
				int index = table.getColumns().indexOf(e.getSource());
				if (index >= 0) {
					fireContentsChanged(index, index);
				}
			} catch (SQLObjectException ex) {
				logger.error("Exception in dbObjectChanged",ex); //$NON-NLS-1$
			}
		} else {
			logger.warn("Unexpected SQLObjectEvent: "+e); //$NON-NLS-1$
		}
	}

}
