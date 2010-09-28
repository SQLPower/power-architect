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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
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
 */
public class IndexColumnTable {

    private class IndexColumnTableModel extends AbstractTableModel implements CleanupTableModel, SQLObjectListener {

        /**
         * This List contains all of the Row objects in the table. Refer to the
         * Row object for more details
         */
        private List<Row> rowList;

        /**
         * The index whose columns this table model is editing. This table model
         * listens to the index for child additions and removals, and adds and
         * removes corresponding rows as appropriate.
         */
        private final SQLIndex index;

        /**
         * The folder that contains all of the SQLColumns represented
         * by this table.
         */
        private final Folder<SQLColumn> columnsFolder;

        /**
         * A listener that will listen for changes to the actual SQLIndex
         * and not the copy that we are modifing here.
         */
        private ActualIndexListener indexListener;

        /**
         * This is the actual SQLIndex in the SQLIndex folder contained by the
         * parent SQLTable
         */
        private final SQLIndex actualIndex;

        /**
         * This constructor will take in a SQLIndex (as a copy of the actual index
         * such that modifications to the index are applied all at once), a SQLTable
         * and the actual index in the SQLIndex folder of the table. The reason for this
         * is because we would like to sync changes between the index copy and the
         * actual index when the user makes changes in the PP.
         */
        IndexColumnTableModel(SQLIndex index, SQLTable parent, SQLIndex actualIndex) {
            this.index = index;
            this.actualIndex = actualIndex;
            this.rowList = new ArrayList<Row>();
            columnsFolder = parent.getColumnsFolder();
            indexListener = new ActualIndexListener();
            setUpListeners();
            populateModel();
        }
        
        /**
         * This method will set up all the listeners to the table model.
         * This includes a listener to the actual SQLIndex, the SQLColumn 
         * folder as well as all the SQLColumns in that folder
         */
        private void setUpListeners (){
            columnsFolder.addSQLObjectListener(this);
            this.actualIndex.addSQLObjectListener(indexListener);
            try {
                for (int i = 0; i < columnsFolder.getChildCount(); i++) {
                    columnsFolder.getChild(i).addSQLObjectListener(this);
                }
            } catch (ArchitectException e) {
                throw new ArchitectRuntimeException(e);
            }
        }

        /**
         * This listener will listen to the actual index (not the copy), such
         * that if the user modifies the SQLIndex from the pp, the table will
         * also be updated with the proper changes.
         */
        private class ActualIndexListener implements SQLObjectListener {

            public void dbChildrenInserted(SQLObjectEvent e) {
                repopulateModel();
            }

            public void dbChildrenRemoved(SQLObjectEvent e) {
                repopulateModel();
            }

            public void dbObjectChanged(SQLObjectEvent e) {
            }

            public void dbStructureChanged(SQLObjectEvent e) {
            }
        }
        
        /**
         * This will move a set of Row objects between the start and end
         * indexes to the destination index
         * @param start The start index
         * @param end The end index
         * @param dest The destination index
         */
        public void moveRow(int start, int end, int dest) {
            List<Row> removed = new ArrayList<Row>();
            for (int i = end; i >= start; i--) {
                removed.add(rowList.remove(i));
            }

            for (int i = 0; i <= end - start; i++) {
                rowList.add(dest, removed.get(i));
            }
            int firstRow = Math.min(start, dest);
            int lastRow = Math.max(end, dest + (end - start));
            fireTableRowsUpdated(firstRow, lastRow);
        }

        /**
         * This method is used to re-populate all of the rows in the table
         * model. Normally used to sync changed between the actual SQLIndex and
         * the columns in this table.
         */
        private void repopulateModel() {
            try {
                index.makeColumnsLike(actualIndex);
            } catch (ArchitectException ex) {
                throw new ArchitectRuntimeException(ex);
            }
            for (int i = rowList.size() - 1; i >= 0; i--) {
                rowList.remove(i);
            }
            populateModel();
            fireTableDataChanged();
        }
        
        /**
         * This populates the model will all the SQLColumns
         */
        private void populateModel() {
            try {
                for (Column indexCol : index.getChildren()) {
                    rowList.add(new Row(true, indexCol.getColumn(), indexCol.getAscendingOrDescending()));
                }
                for (Object so : columnsFolder.getChildren()) {
                    SQLColumn col = (SQLColumn) so;
                    if (!containsColumn(col)) {
                        rowList.add(new Row(false, col, AscendDescend.UNSPECIFIED));
                    }
                }
            } catch (ArchitectException e) {
                throw new ArchitectRuntimeException(e);
            }
        }

        /**
         * This method will indicate whether or not a SQLColumn is already 
         * contained in our table model.
         */
        private boolean containsColumn(SQLColumn col) {
            for (Row r : rowList) {
                if (r.getSQLColumn() == col) {
                    return true;
                }
            }
            return false;
        }
        
        public List<Row> getRowList() {
            return this.rowList;
        }

        /**
         * This method will return the class type of the cell such that the
         * table will know what kind of renderer to use for it.
         */
        public Class getColumnClass(int col) {
            // First column is the checkBox
            if (col == 0)
                return Boolean.class;
            return super.getColumnClass(col);
        }

        /**
         * Returns the column names of this table
         */
        public String getColumnName(int col) {
            if (col == 0) return IN_INDEX; //$NON-NLS-1$
            else if (col == 1) return COL_NAME; //$NON-NLS-1$
            else if (col == 2) return ORDER; //$NON-NLS-1$
            else throw new ArchitectRuntimeException(new ArchitectException("This table only has 3 columns.")); //$NON-NLS-1$
        }


        /**
         * Indicates if a cell in the table is editable or not.
         * In our case, we would want to stop editing on the column that contains
         * the SQLColumn objects.
         */
        public boolean isCellEditable(int row, int col) {
            // do not edit the column that contains the SQLColumn
            if (col == 1) return false;
            return true;
        }

        /**
         * This method is usually called when the Model is detached from the 
         * actual JTable, such that all the listeners can be removed.
         */
        public void cleanup() {
            try {
                for (int i = 0; i < columnsFolder.getChildCount(); i++) {
                    columnsFolder.getChild(i).removeSQLObjectListener(this);
                }
            } catch (ArchitectException e) {
                throw new ArchitectRuntimeException(e);
            }
            columnsFolder.removeSQLObjectListener(this);
            this.actualIndex.removeSQLObjectListener(indexListener);
        }

        public int getColumnCount() {
            return 3;
        }

        public int getRowCount() {
            return rowList.size();
        }

        public Object getValueAt(int row, int col) {
            if (col == 0) return Boolean.valueOf(rowList.get(row).isEnabled());
            else if (col == 1) return rowList.get(row).getSQLColumn();
            else if (col == 2) return rowList.get(row).getOrder();
            else throw new ArchitectRuntimeException(new ArchitectException("This table only has 3 columns.")); //$NON-NLS-1$
        }

        public void setValueAt(Object value, int row, int col) {
            if (col == 0) rowList.get(row).setEnabled(((Boolean) value).booleanValue());
            else if (col == 1) rowList.get(row).setSQLColumn((SQLColumn) value);
            else if (col == 2) rowList.get(row).setOrder((AscendDescend) value);
            else throw new ArchitectRuntimeException(new ArchitectException("This table only has 3 columns.")); //$NON-NLS-1$
            fireTableCellUpdated(row, col);
        }

        public void dbChildrenInserted(SQLObjectEvent e) {
            for (SQLObject so : e.getChildren()) {
                SQLColumn col = (SQLColumn) so;
                // Do not forget to add a listener to the newly added column.
                col.addSQLObjectListener(this);
                rowList.add(new Row(false, col, AscendDescend.UNSPECIFIED));
            }
            fireTableDataChanged();
        }

        public void dbChildrenRemoved(SQLObjectEvent e) {
            for (SQLObject so : e.getChildren()) {
                int rowToRemove = -1;
                int i = 0;
                SQLColumn removedCol = (SQLColumn) so;
                // also removed the listner off the removed object.
                removedCol.removeSQLObjectListener(this);
                for (Row row : rowList) {
                    SQLColumn currentCol = row.getSQLColumn();
                    if (currentCol == removedCol) {
                        rowToRemove = i;
                        break;
                    }
                    i++;
                }

                if (rowToRemove == -1) {
                    throw new IllegalStateException("SQLTable's column " + removedCol + " was just removed, but it " + //$NON-NLS-1$ //$NON-NLS-2$
                            "wasn't in the index table model. Creepy!"); //$NON-NLS-1$
                }
                rowList.remove(rowToRemove);
            }
            fireTableDataChanged();
        }

        public void dbObjectChanged(SQLObjectEvent e) {
            fireTableDataChanged();
        }

        public void dbStructureChanged(SQLObjectEvent e) {
            throw new UnsupportedOperationException("Bad idea.. nothing can cope with dbstructurechanged events."); //$NON-NLS-1$
        }

    }

    /**
     * This is a row object that is contained by the table model of this table.
     */
    private class Row {

        /**
         * Indicates if a column is included in an index or not.
         */
        private boolean enabled;

        /**
         * This is the SQLCOlumn in the row
         */
        private SQLColumn column;

        /**
         * This is the order of the SQLColumn
         */
        private AscendDescend order;

        public Row(boolean enabled, SQLColumn column, AscendDescend order) {
            this.enabled = enabled;
            this.column = column;
            this.order = order;
        }
        
        public String toString() {
            return new String ("Enabled: " +enabled + " Col:  " + column.getName()); //$NON-NLS-1$ //$NON-NLS-2$
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void setSQLColumn(SQLColumn column) {
            this.column = column;
        }

        public void setOrder(AscendDescend order) {
            this.order = order;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public SQLColumn getSQLColumn() {
            return this.column;
        }

        public AscendDescend getOrder() {
            return this.order;
        }

    }

    public static String IN_INDEX = Messages.getString("IndexColumnTable.indexTableColumnName"); //$NON-NLS-1$

    public static String COL_NAME = Messages.getString("IndexColumnTable.columnTableColumnName"); //$NON-NLS-1$

    public static String ORDER = Messages.getString("IndexColumnTable.ascendingDescendingTableColumnName"); //$NON-NLS-1$

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
    public IndexColumnTable(SQLTable parent, SQLIndex index, SQLIndex actualIndex) {
        model = new IndexColumnTableModel(index, parent, actualIndex);
        table = createCustomTable();
        this.parent = parent;
        this.index = index;
        table.getTableHeader().setReorderingAllowed(false);
    }
    
    public void cleanUp(){
        model.cleanup();
    }

    /**
     * This will re-add all the proper SQLColumns to the SQLIndex depending on
     * the ordering that the user provided in the JTable.
     */
    public void finalizeIndex() {
        try {
            //First remove all the children of the index
            for (int i = index.getChildCount() - 1; i >= 0; i--) {
                index.removeChild(i);// remove all current children
            }
            for(Row r : model.getRowList()) {
                if (r.isEnabled()){
                    index.addIndexColumn(r.getSQLColumn(), r.getOrder());
                }
            }

        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        }
    }

    /**
     * This will move a row (or a set of rows) in the table from a start
     * position index to the destinations index. The direction boolean: up =
     * true, down = false
     */
    
      public void moveRow(boolean direction) {
        if (table.getSelectedRows().length <= 0) {
            return;
        }

        int start = table.getSelectedRows()[0];
        int end = table.getSelectedRows()[table.getSelectedRows().length - 1];
        int dest = start;
        if (direction) {
            dest--; // move up
        } else {
            dest++;// move down
        }

        int count = end - start;
        if (count < 0) {
            return;
        }
        if (dest >= 0 && dest <= (table.getRowCount() - count - 1)) {
            model.moveRow(start, end, dest);
            table.getSelectionModel().removeSelectionInterval(0, table.getRowCount() - 1);
            table.getSelectionModel().addSelectionInterval(dest, dest + count);
        }
    }
     

    /**
     * This returns true whenever the row that is provided is enabled in the
     * JTable, otherwise returns false
     */
    private boolean isRowEnabled(int row) {
        if (table.getValueAt(row, table.getColumnModel().getColumnIndex(IN_INDEX)).toString().equals("true")) { //$NON-NLS-1$
            return true;
        }
        return false;
    }

    /**
     * This method will tell us if a specific row index is currently selected in
     * the table.
     */
    private boolean isRowInCurrentSelection(int row) {
        for (int i : table.getSelectedRows()) {
            if (i == row)
                return true;
        }
        return false;
    }

    /**
     * This will be used to Override the JTable
     */
    private JTable createCustomTable() {
        final JTable newTable;

        newTable = new EditableJTable(model);

        //Forwards the mouse listener to the table component
        //if the table gets the mouse click rather than the component
        //(ie the combo box).
        newTable.addMouseListener(new MouseListener() {
        
            public void mouseReleased(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseClicked(MouseEvent e) {
                if (e.getComponent() != e.getComponent().getComponentAt(e.getPoint())) {
                    for (MouseListener listener : e.getComponent().getComponentAt(e.getPoint()).getMouseListeners()) {
                        listener.mouseClicked(e);
                    }
                }
            }
        });
        AscendDescend[] values = new AscendDescend[] { AscendDescend.ASCENDING, AscendDescend.DESCENDING,
                AscendDescend.UNSPECIFIED };
        TableColumn col = newTable.getColumnModel().getColumn(2);
        col.setCellEditor(new ComboBoxEditor(values));

        col.setCellRenderer(new ComboBoxRenderer(values));

        return newTable;
    }

    /**
     * This is a renderer for the JComboBox column used to pick a ascending 
     * or descending order for a SQLColumn in the table
     * @author octavian
     *
     */
    private class ComboBoxRenderer extends JComboBox implements TableCellRenderer {
        public ComboBoxRenderer(AscendDescend[] items) {
            super(items);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }

            // Select the current value
            setSelectedItem(value);
            return this;
        }
    }

    private class ComboBoxEditor extends DefaultCellEditor {
        public ComboBoxEditor(AscendDescend[] items) {
            super(new JComboBox(items));
        }
    }

    /**
     * Returns the table this class is managing.
     */
    public JTable getTable() {
        return table;
    }

}