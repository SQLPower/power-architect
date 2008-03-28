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
import javax.swing.event.ListDataListener;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;

public class SQLColumnComboBoxModel implements ComboBoxModel {
    private SQLTable table;

    private List<SQLColumn> usedColumns;
    private SQLColumn selected;

    public SQLColumnComboBoxModel(SQLTable table, List<SQLColumn> usedColumns)
            throws ArchitectException {
        super();
        SQLColumnComboBoxModelImpl(table, usedColumns);

    }

    public SQLColumnComboBoxModel(SQLTable table) throws ArchitectException {
        super();
        SQLColumnComboBoxModelImpl(table, null);

    }

    private void SQLColumnComboBoxModelImpl(SQLTable table,List<SQLColumn> usedColumns)
            throws ArchitectException {
        if (table == null) throw new NullPointerException();
        this.usedColumns = new ArrayList<SQLColumn>(usedColumns); 
        this.table = table;
    }

    public String getTableName() {
        return table.getName();
    }

    public Object getElementAt(int index) {
        List<SQLColumn> curElements;
        try {
            curElements = getAllUsableElement();
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        }
        return curElements.get(index);
    }

    public int getSize() {
        try {
            List<SQLColumn> curElements = getAllUsableElement();
            return curElements.size();
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        }
    }
    
    private List<SQLColumn> getAllUsableElement() throws ArchitectException {
        if (table == null) {
            System.err.println("table is null");
        }
        List<SQLColumn> curElements = new ArrayList<SQLColumn>(table.getColumns());
        curElements.removeAll(usedColumns);
        curElements.add(0,selected);
        if (selected != null) {
            curElements.add(null);
        }
    
        return curElements;
    }

    public Object getSelectedItem() {
        return selected;
    }

    public void setSelectedItem(Object anItem) {
        // remove all instances of the selected item
        while (usedColumns.remove(selected));
        selected = (SQLColumn) anItem;
        usedColumns.add(selected);
    }

    public void addListDataListener(ListDataListener l) {
        // nothing for now

    }

    public void removeListDataListener(ListDataListener l) {
        // nothing for now

    }

    public SQLTable getTable() {
        return table;
    }

}
