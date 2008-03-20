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
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
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
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.AscendDescend;
import ca.sqlpower.architect.SQLIndex.Column;

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

    /**
     * This is the table that will display the columns
     */
    private JTable table;

    /**
     * Scroll Panel that contains the JTable
     */
    private JScrollPane scrollPane;

    /**
     * The table model of the table
     */
    private DefaultTableModel model;

    /**
     * This is the SQL table that contains the indices
     */
    private SQLTable parent;

    /**
     * This is the SQL index that is being represented by this table.
     */
    private SQLIndex index;


    /**
     * Constructor of the Index Column table.
     */
    public IndexColumnTable(SQLTable parent, SQLIndex index){
        overrideTableModel();
        overrideTable();
        this.parent = parent;
        this.index = index;
        scrollPane = new JScrollPane(table);
        table.getTableHeader().setReorderingAllowed(false);
        prepareHeaders();
        addSQLColumnsToTable();
    }
    
    /**
     * This will re-add all the proper SQLColumns to the SQLIndex depending on the
     * ordering that the user provided in the JTable.
     */
    public void finalizeIndex(){
        try {
            for(int i=index.getChildCount()-1;i>=0;i--){
                index.removeChild(i);//remove all current children
            }
            
            for(int i=0;i<table.getRowCount();i++){
                if(isRowEnabled(i)){
                    String colName = table.getValueAt(i, table.getColumnModel().getColumnIndex(COL_NAME)).toString();
                    String order = table.getValueAt(i, table.getColumnModel().getColumnIndex(ORDER)).toString();
                    index.addIndexColumn((SQLColumn)table.getValueAt(i, 
                            table.getColumnModel().getColumnIndex(COL_NAME)), 
                            AscendDescend.valueOf(table.getValueAt(i, 
                                    table.getColumnModel().getColumnIndex(ORDER)).toString()));
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
        if(dest >=0 && dest<=(table.getRowCount()-count-1)){
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
    private void addSQLColumnsToTable(){
        addComboBox();
        try {
            for(int i = 0; i<parent.getColumns().size();i++){
                // add a new row to the table
                Vector v = new Vector();
                Column child = (Column)index.getChildByName(parent.getColumn(i).getName());

                if(child != null){ 
                    v.add(true); // enable it if the column is contained by the index
                }else{
                    v.add(false);
                }
                v.add(parent.getColumn(i));// add all the columns in the SQL table
                if(child != null){ 
                    v.add(child.getAscendingOrDescending()); // enable it if the column is contained by the index
                }else{
                    v.add(SQLIndex.AscendDescend.UNSPECIFIED);
                }
                model.addRow(v);
            }
            sortAllRowsBy(model,table.getColumnModel().getColumnIndex(IN_INDEX),false);
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        }


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
     * This will return the JScrollPane that contains the Table
     * @return
     */
    public JScrollPane getScrollPanel(){
        return this.scrollPane;
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
    private void overrideTable(){
        /* Override the table here */
        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row,
                    int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                String str = model.getValueAt(row, table.getColumnModel().getColumnIndex(IN_INDEX)).toString();
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
        table.addFocusListener(new FocusListener()
        {
            public void focusGained(FocusEvent e)
            {
                JTable table = (JTable)e.getComponent();
                int row = table.getSelectionModel().getAnchorSelectionIndex();
                int col = table.getColumnModel().getSelectionModel().getAnchorSelectionIndex();
                if ( col == table.getColumnModel().getColumnIndex(ORDER))
                {
                    boolean ok = table.editCellAt(row, col);
                    Component comp = table.getEditorComponent();
                    if (ok && comp instanceof JComboBox){
                        ((JComboBox)comp).setPopupVisible(false);
                        table.getCellEditor().stopCellEditing();
                    }
                }
            }

            public void focusLost(FocusEvent e){}
        });


    }


    /**
     * This method will be used to override the Table Model
     */
    private void overrideTableModel(){

        /* Override the DefaultTableModel */
        this.model = new DefaultTableModel() {
            /**
             * This method will return the class type of the cell such that the table will
             * know what kind of renderer to use for it.
             */
            public Class getColumnClass(int col) {
                if( col == table.getColumnModel().getColumnIndex(IN_INDEX)){
                    return Boolean.class;
                }
                return super.getColumnClass(col);
            }
            
            public boolean isCellEditable(int row, int col){
                if(col == table.getColumnModel().getColumnIndex(COL_NAME)){
                    return false;
                }
                return true;
            }
        };
        


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
     * This method will take in a TableModel, a column index and a boolean representing
     * weather the sorting should be done in ascending or descending order. It will
     * sort all the rows in the table based on the column that has been selected.s
     */
    public void sortAllRowsBy(DefaultTableModel model, int colIndex, boolean ascending) {
        Vector data = model.getDataVector();

        Collections.sort(data, new ColumnSorter(colIndex, ascending));
        model.fireTableDataChanged();
    }

    /**
     * This comparator is used to sort vectors of a table model.
     */
    public class ColumnSorter implements Comparator {
        int colIndex;
        boolean ascending;
        ColumnSorter(int colIndex, boolean ascending) {
            this.colIndex = colIndex;
            this.ascending = ascending;
        }
        public int compare(Object a, Object b) {
            Vector v1 = (Vector)a;
            Vector v2 = (Vector)b;
            Object o1 = v1.get(colIndex);
            Object o2 = v2.get(colIndex);

            // Treat empty strains like nulls
            if (o1 instanceof String && (o1.toString()).length() == 0) {
                o1 = null;
            }
            if (o2 instanceof String && (o2.toString()).length() == 0) {
                o2 = null;
            }
            Object backUpO1 = o1;
            Object backUpO2 = o2;
            try {
                o1= new Double(Double.parseDouble(o1.toString()));
                o2= new Double(Double.parseDouble(o2.toString()));
            } catch (Exception e) {
                o1 = backUpO1;
                o2 = backUpO2;
                //Do not do anything to the object if it cannot be parsed
                //This will be reached whenever the objects are null or they are
                //not numbers
            }

            /*
             * Sort nulls such that they appear last regardless of the sort order
             */
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            } else if (o1 instanceof Comparable) {
                if (ascending) {
                    return ((Comparable)o1).compareTo(o2);
                } else {
                    return ((Comparable)o2).compareTo(o1);
                }
            } else {
                if (ascending) {
                    return o1.toString().compareTo(o2.toString());
                } else {
                    return o2.toString().compareTo(o1.toString());
                }
            }
        }
    }

}
