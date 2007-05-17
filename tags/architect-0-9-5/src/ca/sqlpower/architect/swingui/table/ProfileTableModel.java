package ca.sqlpower.architect.swingui.table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ColumnValueCount;
import ca.sqlpower.architect.profile.ProfileChangeEvent;
import ca.sqlpower.architect.profile.ProfileChangeListener;
import ca.sqlpower.architect.profile.ProfileColumn;
import ca.sqlpower.architect.profile.TableProfileManager;
import ca.sqlpower.architect.profile.TableProfileResult;

public class ProfileTableModel extends AbstractTableModel {

    /**
     * Requesting the value at this column index will give back the
     * entire ColumnProfileResult object that provides the data for
     * the specified row.  This constant is package private because
     * no users outside the table package should be using it.
     *
     * <p>See {@link ProfileJTable#getColumnProfileResultForRow(int)}.
     */
    static final int CPR_PSEUDO_COLUMN_INDEX = -1;

    static Logger logger = Logger.getLogger(ProfileTableModel.class);

    private TableProfileManager profileManager;

    /**
     * A list of profile results to show in the ProfileResultsViewer
     */
    private List<ColumnProfileResult> resultList;

    /**
     * Only tables in this list will have the results of their columns shown.
     */
    private List<TableProfileResult> tableResultsToScan = new ArrayList<TableProfileResult>();
    
    private List<SQLObject> filters;

    public ProfileTableModel(TableProfileManager profileManager) {
        filters = new ArrayList<SQLObject>();
        setProfileManager(profileManager);
    }
    
    /**
     * removes all filters; this will show all the columns
     *
     */
    public void removeAllFilters(){
        filters.clear();
    }

    public void removeFiltersByRegex(SQLObject sqo) {
        filters.remove(sqo);
    }
    /**
     *  Adds a new object that passes the filter
     */
    public void addFilter(SQLObject sqo){
        filters.add(sqo);
    }

    @Override
    public String getColumnName(int col) {
        return ProfileColumn.values()[col].getName();
    }

    public int getRowCount() {
        return resultList.size();
    }

    public int getColumnCount() {
        return ProfileColumn.values().length;
    }

    /**
     * Get value at table cell(row,column), return the most top value on
     * column "TOP_VALUE", not the whole list!
     * @param rowIndex
     * @param columnIndex
     * @return
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        ColumnProfileResult cpr = resultList.get(rowIndex);
        if (columnIndex == CPR_PSEUDO_COLUMN_INDEX) {
            return cpr;
        } else {
            return getColumnValueFromProfile(ProfileColumn.values()[columnIndex], cpr);
        }
    }

    private static Object getColumnValueFromProfile(ProfileColumn column,
             ColumnProfileResult columnProfile) {
        SQLColumn col = columnProfile.getProfiledObject();
        int rowCount = columnProfile.getParentResult().getRowCount();

        switch(column) {
        case DATABASE:
            return ArchitectUtils.getAncestor(col,SQLDatabase.class);
        case CATALOG:
            return ArchitectUtils.getAncestor(col,SQLCatalog.class);
        case  SCHEMA:
            return ArchitectUtils.getAncestor(col,SQLSchema.class);
        case TABLE:
            return ArchitectUtils.getAncestor(col,SQLTable.class);
        case COLUMN:
            return col;
        case RUNDATE:
            return columnProfile.getCreateStartTime();
        case RECORD_COUNT:
            return rowCount;
        case DATA_TYPE:
            try {
                DDLGenerator gddl = DDLUtils.createDDLGenerator(col.getParentTable().getParentDatabase().getDataSource());
                return gddl.columnType(col);
            } catch (Exception e) {
                throw new ArchitectRuntimeException(new ArchitectException(
                        "Unable to get DDL information.  Do we have a valid data source?", e));
            }
        case NULL_COUNT:
            return columnProfile.getNullCount();
        case PERCENT_NULL:
            return rowCount == 0 ? null :  (double)columnProfile.getNullCount() / rowCount ;
        case UNIQUE_COUNT:
            return columnProfile.getDistinctValueCount();
        case  PERCENT_UNIQUE:
            return rowCount == 0 ? null : (double)columnProfile.getDistinctValueCount() / rowCount;
        case  MIN_LENGTH:
            return columnProfile.getMinLength();
        case  MAX_LENGTH:
            return columnProfile.getMaxLength();
        case  AVERAGE_LENGTH:
            return columnProfile.getAvgLength();
        case  MIN_VALUE:            //  min Value
            return columnProfile.getMinValue();
        case  MAX_VALUE:
            return columnProfile.getMaxValue();
        case  AVERAGE_VALUE:
            return columnProfile.getAvgValue();
        case  TOP_VALUE:
            return columnProfile.getValueCount();
        default:
            throw new IllegalArgumentException(
                    String.format("ProfileColumn enum value %s not handled", column));
        }
    }

     /**
      * Get top N value at table cell(row), on column "TOP_VALUE"
      * @param rowIndex
      * @return List of top N Value
      */
     public List<ColumnValueCount> getTopNValueAt(int rowIndex) {
         return resultList.get(rowIndex).getValueCount();
     }

    public boolean isErrorColumnProfile(int row) {
        ColumnProfileResult columnProfile = resultList.get(row);
        return columnProfile.getException() != null;

    }
    public TableProfileManager getProfileManager() {
        return profileManager;
    }

    public void refresh(){
        resultList = new ArrayList<ColumnProfileResult>();
        for (TableProfileResult tpr : tableResultsToScan) {
            for (ColumnProfileResult cpr : tpr.getColumnProfileResults()) {
                resultList.add(cpr);
            }
        }
        Collections.sort(resultList);
        fireTableDataChanged();
    }

    /**
     * If one of the SQLObjects in filters matches with a sqlobject in the
     * profile results return true else return false
     *
     */
    private boolean shouldNotBeFilteredOut(ColumnProfileResult result) {
        for (SQLObject sqo : filters){

            ProfileColumn column;
            if (sqo instanceof SQLDatabase){
                 column = ProfileColumn.DATABASE;
            } else if(sqo instanceof SQLCatalog) {
                column = ProfileColumn.CATALOG;
            } else if(sqo instanceof SQLSchema) {
                column = ProfileColumn.SCHEMA;
            } else if(sqo instanceof SQLTable) {
                column = ProfileColumn.TABLE;
            } else if(sqo instanceof SQLColumn) {
                column = ProfileColumn.COLUMN;
            } else {
                continue;

            }
            if (sqo.equals(getColumnValueFromProfile(column, result))) {
                return true;
            }
        }
        return false;
    }

    public void setProfileManager(TableProfileManager profileManager) {
        this.profileManager = profileManager;
        profileManager.addProfileChangeListener(new ProfileChangeListener(){

            public void profilesRemoved(ProfileChangeEvent e) {
                refresh();
            }

            public void profilesAdded(ProfileChangeEvent e) {
                refresh();
            }

            public void profileListChanged(ProfileChangeEvent event) {
                refresh();
            }});
        refresh();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        ProfileColumn pc = ProfileColumn.values()[columnIndex];
        switch(pc) {
        case DATABASE:
            return SQLDatabase.class;
        case CATALOG:
            return SQLCatalog.class;
        case SCHEMA:
            return SQLSchema.class;
        case TABLE:
            return SQLTable.class;
        case COLUMN:
            return SQLColumn.class;
        case RUNDATE:
            return Long.class;
        case RECORD_COUNT:
            return Integer.class;
        case DATA_TYPE:
            return String.class;
        case NULL_COUNT:
            return Integer.class;
        case PERCENT_NULL:
            return BigDecimal.class;
        case UNIQUE_COUNT:
            return Integer.class;
        case PERCENT_UNIQUE:
            return BigDecimal.class;
        case MIN_LENGTH:
            return Integer.class;
        case MAX_LENGTH:
            return Integer.class;
        case AVERAGE_LENGTH:
            return BigDecimal.class;
        case MIN_VALUE:
            return Object.class;
        case MAX_VALUE:
            return Object.class;
        case AVERAGE_VALUE:
            return Object.class;
        case TOP_VALUE:
            return Object.class;
        default:
            throw new IllegalArgumentException(
                    String.format("ProfileColumn value %s unknown", pc));
        }
    }

    public List<ColumnProfileResult> getResultList() {
        return resultList;
    }
    
    public void addTableResultToScan(TableProfileResult tpr) {
        tableResultsToScan.add(tpr);
    }
    
    public List<TableProfileResult> getTableResultsToScan() {
        return tableResultsToScan;
    }

    public void clearScanList() {
        tableResultsToScan.clear();
    }

    public void removeTableResultToScan(TableProfileResult result) {
        tableResultsToScan.remove(result);
    }
}
