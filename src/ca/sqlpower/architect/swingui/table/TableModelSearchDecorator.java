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
        CharSequence searchSubSequence = searchText == null ? null : searchText.toLowerCase().subSequence(0,searchText.length());
        for ( int row = 0; row < tableModel.getRowCount(); row++ ) {
            boolean match = false;
            if ( searchSubSequence == null ) {
                match = true;
            } else {
                for ( int column = 0; column < tableModel.getColumnCount(); column++ ) {
                    String value = tableTextConverter.getTextForCell(row, column);
                    if (value.toLowerCase().contains(searchSubSequence)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Match: "+value.toLowerCase()+" contains "+searchSubSequence);
                        }
                        match = true;
                        break;
                    }
                }
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
