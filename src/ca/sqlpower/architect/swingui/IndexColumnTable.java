/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.AscendDescend;
import ca.sqlpower.architect.SQLIndex.Column;
import ca.sqlpower.architect.SQLTable.Folder;
import ca.sqlpower.swingui.table.CleanupTableModel;
import ca.sqlpower.swingui.table.EditableJTable;

/**
 * This class will be used to display the columns that are contained in an 
 * index. This table will be used by the IndexEditPanel class.
 * @author Octavian
 */
public class IndexColumnTable {

    /**
     * Name for the In Index column
     */
    private final String IN_INDEX = "In Index";
    /**
     * Text for the Name column
     */
    private final String COL_NAME = "Column Name";
    /**
     * Text for the Order column
     */
    private final String ORDER = "Asc / Des";

    private class IndexColumnTableModel extends DefaultTableModel implements CleanupTableModel, SQLObjectListener {
        
        /**
         * The index whose columns this table model is editing.  This table model
         * listens to the index for child additions and removals, and adds and removes
         * corresponding rows as appropriate.
         */
        private final SQLIndex index;
        private final Folder<SQLColumn> columnsFolder;
        
        IndexColumnTableModel(SQLIndex index) {
            this.index = index;
            columnsFolder = index.getParentTable().getColumnsFolder();
            columnsFolder.addSQLObjectListener(this);
        }
        
        /**
         * This method will return the class type of the cell such that the table will
         * know what kind of renderer to use for it.
         */
        public Class getColumnClass(int col) {
            if (col == table.getColumnModel().getColumnIndex(IN_INDEX)) {
                return Boolean.class;
            }
            return super.getColumnClass(col);
        }

        public boolean isCellEditable(int row, int col) {
            if (col == table.getColumnModel().getColumnIndex(COL_NAME)) {
                return false;
            }
            return true;
        }

        public void cleanup() {
            columnsFolder.removeSQLObjectListener(this);
        }

        public void dbChildrenInserted(SQLObjectEvent e) {
            for (SQLObject so : e.getChildren()) {
                SQLColumn col = (SQLColumn) so;
                try {
                    addIndexColumnToTable(getIndexColumn(col));
                } catch (ArchitectException ex) {
                    throw new ArchitectRuntimeException(ex);
                }
            }
        }

        public void dbChildrenRemoved(SQLObjectEvent e) {
            for (SQLObject so : e.getChildren()) {
                int rowToRemove = -1;
                int i = 0;
                SQLColumn removedCol = (SQLColumn) so;
                for (Vector row : (Vector<Vector>) getDataVector()) {
                    Column indexCol = (Column) row.get(1);
                    if (indexCol.getColumn() == removedCol) {
                        rowToRemove = i;
                        break;
                    }
                    i++;
                }
                
                if (rowToRemove == -1) {
                    throw new IllegalStateException(
                            "SQLTable's column " + removedCol + " was just removed, but it " +
                            "wasn't in the index table model. Creepy!");
                }
                
                removeRow(rowToRemove);
            }
        }

        public void dbObjectChanged(SQLObjectEvent e) {
            // don't care about property changes
        }

        public void dbStructureChanged(SQLObjectEvent e) {
            throw new UnsupportedOperationException("Bad idea.. nothing can cope with dbstructurechanged events.");
        }
        
        /**
         * Obtains the proper SQLIndex.Column object given a SQLColumn. If the
         * requested column is not part of the index we're editing, returns a new
         * instance of SQLIndex.Column which has no parent but is attached to the
         * requested column.
         */
        private Column getIndexColumn(SQLColumn col) {
            try {
                for (int i = 0; i < index.getChildCount(); i++) {
                       if(index.getChild(i).getColumn().equals(col)){
                           return index.getChild(i);
                       }
                }
            } catch (ArchitectException e) {
                throw new ArchitectRuntimeException(e);
            }
            
            return index.new Column(col, AscendDescend.UNSPECIFIED);
        }
    }
    
    /**
     * This is the table that will display the columns
     */
    private final JTable table;

    /**
     * The table model of the table
     */
    private final IndexColumnTableModel model;

    /**
     * This is the SQL table that contains the indices
     */
    private final SQLTable parent;

    /**
     * This is the SQL index that is being represented by this table.
     */
    private final SQLIndex index;


    /**
     * Constructor of the Index Column table.
     */
    public IndexColumnTable(SQLTable parent, SQLIndex index){
        model = new IndexColumnTableModel(index);
        table = createCustomTable();
        this.parent = parent;
        this.index = index;
        table.getTableHeader().setReorderingAllowed(false);
        prepareHeaders();
        addSQLColumnsToTable();
    }
    
    /**
     * This will re-add all the proper SQLColumns to the SQLIndex depending on the
     * ordering that the user provided in the JTable.
     */
    public void finalizeIndex() {
        try {
            for (int i = index.getChildCount() - 1; i >= 0; i--) {
                index.removeChild(i);//remove all current children
            }
            
            for (int i = 0; i < table.getRowCount(); i++) {
                if (isRowEnabled(i)) {
                    Column col = (Column) table.getValueAt(i, table.getColumnModel().getColumnIndex(COL_NAME));
                    AscendDescend adSetting = AscendDescend.valueOf(table.getValueAt(i, 
                            table.getColumnModel().getColumnIndex(ORDER)).toString());
                    col.setAscendingOrDescending(adSetting);
                    index.addChild(col);
                }
            }

         } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        }
   }


    /**
     * This will move a row (or a set of rows) in the table from a start position index to the destinations index.
     * The direction boolean: up = true, down = false
     */
    public void moveRow(boolean direction) {
        if(table.getSelectedRows().length <=0){
            return; // return on no selected rows
        }

        int start = table.getSelectedRows()[0];
        int end = table.getSelectedRows()[table.getSelectedRows().length-1];
        int dest = start;
        if(direction){
            dest--; // move up
        }else{
            dest++;//move down
        }
        int count = end - start;
        if (count < 0) {
            return; 
        }
        if (dest >= 0 && dest<=(table.getRowCount()-count-1)) {
            model.moveRow(start, end, dest);
            table.getSelectionModel().removeSelectionInterval(0, table.getRowCount()-1);
            table.getSelectionModel().addSelectionInterval(dest, dest+count);
        }
    }

    /**
     * This returns true whenever the row that is provided is enabled in the JTable,
     * otherwise returns false
     */
    private boolean isRowEnabled(int row){
        if(table.getValueAt(row, table.getColumnModel().getColumnIndex(IN_INDEX)).toString().equals("true")){
            return true;
        }
        return false;
    }

    /**
     * This will add the columns contained by the SQLIndex to the JTable.
     */
    private void addSQLColumnsToTable() {
        addComboBox();
        try {
            
            Set<SQLColumn> addedCols = new HashSet<SQLColumn>();
            for (Column idxCol : index.getChildren()) {
                addIndexColumnToTable(idxCol);
                addedCols.add(idxCol.getColumn());
            }
            
            // These aren't in the index yet, so we make placeholder Column instances...
            for (SQLColumn col : parent.getColumns()) {
                if (addedCols.contains(col)) continue;
                Column idxCol = index.new Column(col, AscendDescend.UNSPECIFIED);
                addIndexColumnToTable(idxCol);
            }
            
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        }
    }

    /**
     * Adds the given index column to the table model. If the column you want
     * to add doesn't already belong to the index we're currently editing, its
     * getParent() should return null.
     * 
     * @param idxCol The index column to add
     */
    private void addIndexColumnToTable(Column idxCol) throws ArchitectException {
        if (idxCol.getParent() != index) {
            throw new IllegalArgumentException("The given index column doesn't belong to the current index");
        }
        
        Vector v = new Vector();

        // cell 0: in index?
        if (idxCol.getParent().getChildren().contains(idxCol)) { 
            v.add(true);
        } else {
            v.add(false);
        }
        
        // cell 1: which column
        v.add(idxCol);
        
        // cell 2: asc/desc
        v.add(idxCol.getAscendingOrDescending());
        
        model.addRow(v);
    }

    /**
     * This will add a combo box to the table that contains all order types
     */
    private void addComboBox(){
        TableColumn sportColumn = table.getColumn(ORDER);
        JComboBox comboBox = new JComboBox();
        comboBox.addItem(SQLIndex.AscendDescend.UNSPECIFIED.toString());
        comboBox.addItem(SQLIndex.AscendDescend.ASCENDING.toString());
        comboBox.addItem(SQLIndex.AscendDescend.DESCENDING.toString());
        sportColumn.setCellEditor(new DefaultCellEditor(comboBox));
    }


    /**
     * This will prepare all the headers in the table.
     */
    private void prepareHeaders(){
        addData(IN_INDEX);
        addData(COL_NAME);
        addData(ORDER); 
    }

    /**
     * This method will tell us if a specific row index is currently selected
     * in the table.
     */
    private boolean isRowInCurrentSelection(int row){
        for (int i: table.getSelectedRows()) {
            if (i == row)  return true;
        }
        return false;
    }

    /**
     * This will be used to Override the JTable
     */
    private JTable createCustomTable(){
        final JTable newTable;
        
        /* Override the table here */
        newTable = new EditableJTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row,
                    int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                String str = model.getValueAt(row, getColumnModel().getColumnIndex(IN_INDEX)).toString();
                UIDefaults def = UIManager.getLookAndFeelDefaults();
                
                boolean isSelected = isRowInCurrentSelection(row);
                if (str.equals("false") && isSelected) {
                    c.setBackground(def.getColor("TextField.selectionBackground"));
                    c.setForeground(def.getColor("TextField.inactiveForeground"));
                } else if (str.equals("false") && !isSelected) {
                    c.setForeground(def.getColor("TextField.inactiveForeground"));                    
                    c.setBackground(def.getColor("TextField.inactiveBackground"));
                } else if (isSelected){
                    c.setBackground(def.getColor("TextField.selectionBackground"));
                    c.setForeground(def.getColor("TextField.foreground"));
                } else {
                    c.setBackground(def.getColor("TextField.background"));
                    c.setForeground(def.getColor("TextField.foreground"));
                }
                return c;
            }

        };

        /*
         * This makes sure that when the user clicks on a combo box, that the
         * comboBox will gain focus right away, and will not have to be clicked again
         * so that it can get focus.
         */
        newTable.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                JTable table = (JTable)e.getComponent();
                int row = table.getSelectionModel().getAnchorSelectionIndex();
                int col = table.getColumnModel().getSelectionModel().getAnchorSelectionIndex();
                if (col == table.getColumnModel().getColumnIndex(ORDER)) {
                    boolean ok = table.editCellAt(row, col);
                    Component comp = table.getEditorComponent();
                    if (ok && comp instanceof JComboBox){
                        ((JComboBox)comp).setPopupVisible(false);
                        table.getCellEditor().stopCellEditing();
                    }
                }
            }

            public void focusLost(FocusEvent e) {}
        });

        return newTable;
    }

    /**
     * This will add a new Column to the table and an arbitrary set of 
     * Data that can be passed through a vector.
     */
    private void addData(Object columnName){
        model.addColumn(columnName);
        table.getColumnModel().getColumn(model.getColumnCount()-1).setIdentifier(columnName.toString());
    }

    /**
     * Returns the table this class is managing.
     */
    public JTable getTable() {
        return table;
    }

}
