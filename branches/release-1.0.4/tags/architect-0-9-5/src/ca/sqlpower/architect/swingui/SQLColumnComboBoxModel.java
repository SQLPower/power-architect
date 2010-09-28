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
