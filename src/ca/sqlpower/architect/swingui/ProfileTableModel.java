package ca.sqlpower.architect.swingui;

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
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfileResult;
import ca.sqlpower.architect.profile.TableProfileResult;

public class ProfileTableModel extends AbstractTableModel {

    static Logger logger = Logger.getLogger(ProfileTableModel.class);

    private ProfileManager profileManager;

    private List<ColumnProfileResult> resultList;

    private List<SQLObject> filters;

    private String[] columnNames = {"Database",
                                    "Catalog",
                                    "Schema",
                                    "Table",
                                    "Column",
                                    "Run Date",
                                    "Record Count",
                                    "Data Type",
                                    "# Null",
                                    "% Null",
                                    "# Unique",
                                    "% Unique",
                                    "Min Length",
                                    "Max Length",
                                    "Avg. Length",
                                    "Min Value",
                                    "Max Value",
                                    "Avg. Value" };
    public ProfileTableModel() {
        filters = new ArrayList<SQLObject>();
    }
    /**
     * removes all filters this will show all the columns
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
        return columnNames[col];
    }

    public int getRowCount() {
        return resultList.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        ColumnProfileResult columnProfile = resultList.get(rowIndex);
        return getColumnValueFromProfile(columnIndex, columnProfile, profileManager);
    }

    private static Object getColumnValueFromProfile(int columnIndex, ColumnProfileResult columnProfile, ProfileManager profileManager) {
        SQLColumn col = columnProfile.getProfiledObject();
        int rowCount = ((TableProfileResult) profileManager.getResult(col.getParentTable())).getRowCount();

        if (columnIndex == 0) {
            return ArchitectUtils.getAncestor(col,SQLDatabase.class);
        } else if (columnIndex == 1) {
            return ArchitectUtils.getAncestor(col,SQLCatalog.class);
        } else if (columnIndex == 2) {
            return ArchitectUtils.getAncestor(col,SQLSchema.class);
        } else if (columnIndex == 3) {
            return ArchitectUtils.getAncestor(col,SQLTable.class);
        } else if (columnIndex == 4) {
            return col;
        } else if (columnIndex == 5) {
            // Run date
            return columnProfile.getCreateStartTime();
        } else if (columnIndex == 6) {
            // Row Count
            return rowCount;
        } else if (columnIndex == 7) {
            // data type
            DDLGenerator gddl;
            try {
                gddl = DDLUtils.createDDLGenerator(col.getParentTable().getParentDatabase().getDataSource());
                return gddl.columnType(col);
            } catch (Exception e) {
                throw new ArchitectRuntimeException(new ArchitectException(
                        "Unable to get DDL information.  Do we have a valid data source?", e));
            }
        } else if (columnIndex == 8) {
            //  Number of null records
            return columnProfile.getNullCount();
        } else if (columnIndex == 9) {
            //  Percent null records
            return rowCount == 0 ? null :  (double)columnProfile.getNullCount() / rowCount ;
        } else if (columnIndex == 10) {
            //  Number of unique records
            return columnProfile.getDistinctValueCount();
        } else if (columnIndex == 11) {
            //  percent of unique records
            return rowCount == 0 ? null : (double)columnProfile.getDistinctValueCount() / rowCount;
        } else if (columnIndex == 12) {
            //  min Length
            return columnProfile.getMinLength();
        } else if (columnIndex == 13) {
            //  Max Length
            return columnProfile.getMaxLength();
        } else if (columnIndex == 14) {
            //  Avg Length
            return columnProfile.getAvgLength();
        } else if (columnIndex == 15) {
            //  min Value
            return columnProfile.getMinValue();
        } else if (columnIndex == 16) {
            //  Max value
            return columnProfile.getMaxValue();
        } else if (columnIndex == 17) {
            //  Avg Value
            return columnProfile.getAvgValue();
        } else {
            throw new IllegalArgumentException("Column Index out of bounds");
        }
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public void refresh(){
        resultList = new ArrayList<ColumnProfileResult>();
        for (ProfileResult pr : profileManager.getResults().values()) {
            if (pr instanceof ColumnProfileResult) {
                if (filters.size() > 0) {

                    if (shouldNotBeFilteredOut((ColumnProfileResult) pr)) {
                        resultList.add((ColumnProfileResult) pr);
                    }
                } else {
                    resultList.add((ColumnProfileResult) pr);
                }
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
            int objectColumn;
            if (sqo instanceof SQLDatabase){
                 objectColumn=0;
            } else if(sqo instanceof SQLCatalog) {
                objectColumn=1;
            } else if(sqo instanceof SQLSchema) {
                objectColumn=2;
            } else if(sqo instanceof SQLTable) {
                objectColumn=3;
            } else if(sqo instanceof SQLColumn) {
                objectColumn=4;
            } else {
                continue;

            }
            if (sqo.equals(getColumnValueFromProfile(objectColumn,result,profileManager))) {
                return true;
            }
        }
        return false;
    }

    public void setProfileManager(ProfileManager profileManager) {
        this.profileManager = profileManager;
        refresh();
    }


}
