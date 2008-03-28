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
import java.util.Iterator;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;
import ca.sqlpower.architect.SQLTable;

public class SQLTableListModel implements ListModel, SQLObjectListener {

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
			return table.getColumnsFolder().getChildCount();
		} catch (ArchitectException ex) {
			throw new RuntimeException("Couldn't get child count", ex);
		}
	}

	/**
	 * @throws RuntimeException if an ArchitectException occurs.
	 */
	public Object getElementAt(int index) {
		try {
			return table.getColumnsFolder().getChild(index);
		} catch (ArchitectException ex) {
			throw new RuntimeException("Couldn't get child "+index, ex);
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


	// -------------- SQLObjectListener INTERFACE -------------------
	public void dbChildrenInserted(SQLObjectEvent e) {
		int[] changedIndices = e.getChangedIndices();
		for (int i = 0; i < changedIndices.length; i++) {
			// XXX: should group contiguous regions into one event!
			fireIntervalAdded(changedIndices[i], changedIndices[i]);
		}
	}

	public void dbChildrenRemoved(SQLObjectEvent e) {
		int[] changedIndices = e.getChangedIndices();
		for (int i = 0; i < changedIndices.length; i++) {
			// XXX: should group contiguous regions into one event!
			fireIntervalRemoved(changedIndices[i], changedIndices[i]);
		}
	}

	public void dbObjectChanged(SQLObjectEvent e) {
		if (e.getSource() == table.getColumnsFolder()) {
			int[] changedIndices = e.getChangedIndices();
			for (int i = 0; i < changedIndices.length; i++) {
				// XXX: should group contiguous regions into one event!
				logger.debug("Firing contentsChanged event for index "+i);
				fireContentsChanged(changedIndices[i], changedIndices[i]);
			}
		} else if (e.getSource() instanceof SQLColumn) {
			// make sure this column was actually in the table
			try {
				int index = table.getColumns().indexOf(e.getSource());
				if (index >= 0) {
					fireContentsChanged(index, index);
				}
			} catch (ArchitectException ex) {
				logger.error("Exception in dbObjectChanged",ex);
			}
		} else {
			logger.warn("Unexpected SQLObjectEvent: "+e);
		}
	}

	public void dbStructureChanged(SQLObjectEvent e) {
		int[] changedIndices = e.getChangedIndices();
		for (int i = 0; i < changedIndices.length; i++) {
			// XXX: should group contiguous regions into one event!
			fireContentsChanged(changedIndices[i], changedIndices[i]);
		}
	}

}
