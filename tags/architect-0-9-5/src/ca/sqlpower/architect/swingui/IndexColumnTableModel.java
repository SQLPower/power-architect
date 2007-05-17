package ca.sqlpower.architect.swingui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.Column;
import ca.sqlpower.swingui.table.CleanupTableModel;

public class IndexColumnTableModel extends AbstractTableModel implements CleanupTableModel {

    private class IndexListener implements SQLObjectListener{

        public void dbChildrenInserted(SQLObjectEvent e) {
            if (e.getChangedIndices().length > 1) {
                fireTableStructureChanged();
            } else {
                fireTableRowsInserted(e.getChangedIndices()[0], e.getChangedIndices()[0]);
            }
        }

        public void dbChildrenRemoved(SQLObjectEvent e) {
            if (e.getChangedIndices().length > 1) {
                fireTableStructureChanged();
            } else {
                fireTableRowsDeleted(e.getChangedIndices()[0], e.getChangedIndices()[0]);
            }
        }

        public void dbObjectChanged(SQLObjectEvent e) {
            if (e.getChangedIndices().length > 1) {
                fireTableStructureChanged();
            } else {
                fireTableRowsUpdated(e.getChangedIndices()[0], e.getChangedIndices()[0]);
            }
        }

        public void dbStructureChanged(SQLObjectEvent e) {
            fireTableStructureChanged();
        }
        
    }
    public enum IndexColumnTableColumns  {
        NAME("Expression",String.class),COLUMN("Index Column",SQLColumn.class),ASC("ASC",Boolean.class),DESC("DESC",Boolean.class);
        
        String name;
        Class columnClass;

        IndexColumnTableColumns(String name, Class t)  {
            this.name = name;
            this.columnClass = t;
        }
        

        public String getName() {
            return name;
        }
    }
    
    private final SQLIndex index;
    private final SQLTable table;
    private List<SQLColumn> usedColumns = null;
    private IndexListener listener = new IndexListener();
    
    public IndexColumnTableModel(SQLIndex index,SQLTable table) {
        if (table == null) throw new NullPointerException();
        index.addSQLObjectListener(listener);
        this.index = index;
        this.table = table;
    }
    
    
    
    public SQLIndex getIndex() {
        return index;
    }


    public List<SQLColumn> getUsedColumns() throws ArchitectException {
        usedColumns = new ArrayList<SQLColumn>();
        for (Column c : (List<Column>) index.getChildren()){
            if (c.getColumn() != null ){
                usedColumns.add(c.getColumn());
            }
        }
        return usedColumns;
    }

    public void cleanup() {
        index.removeSQLObjectListener(listener);
    }

    public Class<?> getColumnClass(int columnIndex) {
        return IndexColumnTableColumns.values()[columnIndex].columnClass;
    }



    public int getColumnCount() {
        return IndexColumnTableColumns.values().length;
    }



    public String getColumnName(int columnIndex) {
        return IndexColumnTableColumns.values()[columnIndex].getName();
    }


    private IndexColumnTableColumns getColumnInfo(int columnIndex) {
        return IndexColumnTableColumns.values()[columnIndex];
    }

    public int getRowCount() {
        try {
            return index.getChildCount();
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Column c;
        try {
            c = index.getChild(rowIndex);
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        }
        IndexColumnTableColumns tableColumnInfo = getColumnInfo(columnIndex);
        if ( tableColumnInfo == IndexColumnTableColumns.COLUMN) {
            return c.getColumn();
        } else if (tableColumnInfo == IndexColumnTableColumns.ASC) {
            return c.isAscending();
        } else if (tableColumnInfo == IndexColumnTableColumns.DESC) {
            return c.isDescending();
        } else if  (tableColumnInfo == IndexColumnTableColumns.NAME){ 
            return c.getName();
        }else {
            throw new ArchitectRuntimeException(new ArchitectException("Someone forgot to implement a getValue for "+tableColumnInfo));
        }
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Column c;
        try {
            c = index.getChild(rowIndex);
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        }
        IndexColumnTableColumns tableColumnInfo = getColumnInfo(columnIndex);
        if ( tableColumnInfo == IndexColumnTableColumns.COLUMN) {
            c.setColumn((SQLColumn)aValue);
        } else if (tableColumnInfo == IndexColumnTableColumns.ASC) {
            c.setAscending((Boolean)aValue);
        } else if (tableColumnInfo == IndexColumnTableColumns.DESC) {
            c.setDescending((Boolean)aValue);
        } else if (tableColumnInfo == IndexColumnTableColumns.NAME){
            c.setName((String)aValue);
        } else {
            throw new ArchitectRuntimeException(new ArchitectException("Someone forgot to implement a setValue for "+tableColumnInfo));
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }



    public SQLTable getTable() {
        return table;
    }
}
