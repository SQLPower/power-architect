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
package ca.sqlpower.architect.swingui.table;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.swingui.table.TableTextConverter;
/**
 * Searches through a table model using a table text converter.  It reduces the visible table 
 * rows as rows stop matching.
 * 
 * XXX: This model eats tableChanged events that get thrown from below this should be fixed! 
 */
public class TableModelSearchDecorator extends AbstractTableModel {

    private static final Logger logger = Logger.getLogger(TableModelSearchDecorator.class);

    /**
     * We need a way of getting the String value of any cell in the table
     * because we need to reliably search for the same text the user sees!
     * The Object.toString() often won't match what the table's cell renderers
     * put on the screen.
     */
    private TableTextConverter tableTextConverter;

    protected TableModel tableModel;
    private List<Integer> rowMapping = null;  // null means identity mapping
    protected Document doc;
    private String searchText = null;

    private DocumentListener docListener = new DocumentListener(){

        private String getSearchText(DocumentEvent e) {
            String searchText = null;
            try {
                searchText = e.getDocument().getText(0,e.getDocument().getLength());
            } catch (BadLocationException e1) {
                ASUtils.showExceptionDialog("Internal Error (search profile)!", e1);
            }
            return searchText;
        }
        public void insertUpdate(DocumentEvent e) {
            search(getSearchText(e));
        }

        public void removeUpdate(DocumentEvent e) {
            search(getSearchText(e));
        }

        public void changedUpdate(DocumentEvent e) {
            search(getSearchText(e));
        }
    };
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return tableModel.isCellEditable(rowIndex, columnIndex);
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
     
       tableModel.setValueAt(aValue, rowIndex, columnIndex);
    }

    public TableModelSearchDecorator(TableModel model) {
        super();
        this.tableModel = model;
        setDoc(new DefaultStyledDocument());

        model.addTableModelListener(new TableModelListener(){

            public void tableChanged(TableModelEvent e) {
                search(searchText);
            }});
    }

    private void search(String searchText) {

        rowMapping = null;
        fireTableDataChanged();

        List<Integer> newRowMap = new ArrayList<Integer>();
        String[] searchWords = (searchText == null ? null : searchText.split(" "));

        for ( int row = 0; row < tableModel.getRowCount(); row++ ) {
            boolean match = false;
            if ( searchWords == null ) {
                match = true;
            } else {
                
                int i;
                for ( i=0; i<searchWords.length; i++ ) {
                    
                    match = false;
                    for ( int column = 0; column < tableModel.getColumnCount(); column++ ) {
                        String value = tableTextConverter.getTextForCell(row, column);
                        if ( value.toLowerCase().indexOf(searchWords[i].toLowerCase()) >= 0 ) {
                            match = true;
                            if (logger.isDebugEnabled()) {
                                logger.debug("Match: "+value.toLowerCase()+" contains "+searchWords[i]+ "     "+value.toLowerCase().indexOf(searchWords[i].toLowerCase()));
                            }
                            break;
                        }
                    }
                    if ( !match )
                        break;
                        
                }
                if ( i < searchWords.length )
                    match = false;
            }
            if ( match ) {
                newRowMap.add(tableTextConverter.modelIndex(row));
            }
        }
        setSearchText(searchText);
        rowMapping = newRowMap;
        if (logger.isDebugEnabled()) {
            logger.debug("new row mapping after search: "+rowMapping);
        }
        fireTableDataChanged();
    }

    public int getRowCount() {
        if (rowMapping == null) {
            return tableModel.getRowCount();
        } else {
            return rowMapping.size();
        }
    }

    public int getColumnCount() {
        return tableModel.getColumnCount();
    }


    public Object getValueAt(int rowIndex, int columnIndex) {
        return tableModel.getValueAt(rowToModel(rowIndex),columnIndex);
    }

    private int rowToModel(int rowIndex) {
        int modelRow = (rowMapping == null ? rowIndex : rowMapping.get(rowIndex));
        return modelRow;
    }

    @Override
    public String getColumnName(int column) {
        return tableModel.getColumnName(column);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return tableModel.getColumnClass(columnIndex);
    }


    public TableModel getTableModel() {
        return tableModel;
    }

    public void setTableModel(TableModel tableModel) {
        this.tableModel = tableModel;
        fireTableStructureChanged();
    }

    public Document getDoc() {
        return doc;
    }

    public void setDoc(Document doc) {
        if ( this.doc != null ) {
            doc.removeDocumentListener(docListener);
        }

        this.doc = doc;

        if (doc != null) {
            doc.addDocumentListener(docListener);
        }
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public TableTextConverter getTableTextConverter() {
        return tableTextConverter;
    }

    public void setTableTextConverter(TableTextConverter tableTextConverter) {
        this.tableTextConverter = tableTextConverter;
    }
}
