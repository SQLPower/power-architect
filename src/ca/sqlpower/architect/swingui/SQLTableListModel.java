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
